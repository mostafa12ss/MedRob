package com.learn.test_exel.ui.viewmodel


import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.learn.test_exel.Backend.data.AppDatabase
import com.learn.test_exel.Backend.data.ExcelExporter
import com.learn.test_exel.Backend.data.ExcelImporter
import com.learn.test_exel.Backend.data.FirebaseSync
import com.learn.test_exel.Backend.data.HospitalRepository
import com.learn.test_exel.Backend.data.ImportRow
import com.learn.test_exel.Backend.data.PatientEntity
import com.learn.test_exel.Backend.data.QrKind
import com.learn.test_exel.Backend.data.QrPayload
import com.learn.test_exel.Backend.data.TreatmentOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import java.time.LocalTime
data class ImportPreview(val rows: List<ImportRow>, val errors: List<String>)
sealed interface ScanInfo {
    data class P(val patient: PatientEntity) : ScanInfo
    data class T(val option: TreatmentOption) : ScanInfo
    data object Missing : ScanInfo
}
data class EditData(
    val treatmentId: String,
    val patient: String,
    val bed: String,
    val drug: String,
    val startDay: Long,
    val endDay: Long,
    val times: String
)
class HospitalViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val repo = HospitalRepository(db)
    private val sync = FirebaseSync(db.dao())
    private var listener: ListenerRegistration? = null
    val treatmentOptions = db.dao().observeTreatmentOptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val patients = repo.patients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** بيدور على السجل اللي الكود بيشاور عليه في داتا الجهاز */
    suspend fun resolveScan(p: QrPayload): ScanInfo = when (p.kind) {
        QrKind.PATIENT -> db.dao().findPatientById(p.id)?.let { ScanInfo.P(it) } ?: ScanInfo.Missing
        QrKind.TREATMENT -> db.dao().findTreatmentOption(p.id)?.let { ScanInfo.T(it) } ?: ScanInfo.Missing
    }
    val summary = repo.summary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    init { viewModelScope.launch { runCatching { ensureListener() } } }

    private suspend fun uid(): String {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser?.uid ?: auth.signInAnonymously().await().user!!.uid
    }

    /** الاستماع لحالة الجرعات (taken) من Firebase */
    private suspend fun ensureListener() {
        if (listener == null) listener = sync.listenToDoseStatus(uid(), viewModelScope)
    }

    /** حفظ على الجهاز ثم رفع لـ Firebase */
    fun saveAndUpload(
        patient: String, bed: String, drug: String,
        start: LocalDate, end: LocalDate, times: List<LocalTime>,
        onSaved: () -> Unit
    ) = viewModelScope.launch {
        _busy.value = true
        try {
            repo.addTreatment(patient, bed, drug, start, end, times)
            onSaved()
            try {
                // withTimeout لأن Firestore بيفضل مستني لو مفيش نت
                withTimeout(20_000) { sync.push(uid()) }
                runCatching { ensureListener() }
                _messages.emit("تم الحفظ والرفع إلى Firebase")
            } catch (e: Exception) {
                _messages.emit("اتحفظ على الجهاز، لكن الرفع فشل. اضغط حفظ تاني عند توفر النت")
            }
        } catch (e: IllegalArgumentException) {
            _messages.emit(e.message ?: "بيانات غير صحيحة")
        } finally {
            _busy.value = false
        }
    }
    private val _preview = MutableStateFlow<ImportPreview?>(null)
    val preview = _preview.asStateFlow()

    /** الخطوة 1: قراءة الملف وعرض المعاينة (من غير حفظ) */
    fun readExcel(uri: Uri) = viewModelScope.launch {
        _busy.value = true
        try {
            val result = withContext(Dispatchers.IO) {
                getApplication<Application>().contentResolver.openInputStream(uri)
                    ?.use { ExcelImporter.parse(it) }
                    ?: throw IllegalArgumentException("تعذر فتح الملف")
            }
            if (result.rows.isEmpty() && result.errors.isEmpty())
                _messages.emit("الملف مفيهوش بيانات")
            else
                _preview.value = ImportPreview(result.rows, result.errors)
        } catch (e: IllegalArgumentException) {
            _messages.emit(e.message ?: "ملف غير صالح")
        } catch (e: Exception) {
            android.util.Log.e("HospitalSync", "excel read failed", e)
            _messages.emit("فشل قراءة الملف: ${e.javaClass.simpleName} - ${e.message}")
        } finally {
            _busy.value = false
        }
    }

    /** الخطوة 2: حفظ على الجهاز ثم رفع لـ Firebase */
    fun confirmImport() {
        val p = _preview.value ?: return
        _preview.value = null
        viewModelScope.launch {
            _busy.value = true
            try {
                val (added, skipped) = repo.importRows(p.rows)
                _messages.emit(
                    "تم حفظ $added علاج على الجهاز" + if (skipped > 0) " وتخطي $skipped مكرر" else ""
                )
                try {
                    withTimeout(120_000) { sync.push(uid()) }
                    runCatching { ensureListener() }
                    _messages.emit("تم الرفع إلى Firebase")
                } catch (e: Exception) {
                    android.util.Log.e("HospitalSync", "push failed", e)
                    _messages.emit("اتحفظ على الجهاز لكن الرفع فشل: ${e.javaClass.simpleName} - ${e.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("HospitalSync", "import failed", e)
                _messages.emit("فشل الاستيراد: ${e.message}")
            } finally {
                _busy.value = false
            }
        }
    }

    fun cancelImport() { _preview.value = null }

    fun saveTemplate(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        try {
            getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                ExcelExporter.writeTemplate(it)
            }
            _messages.emit("تم حفظ النموذج")
        } catch (e: Exception) {
            _messages.emit("فشل حفظ النموذج: ${e.message}")
        }
    }
    /** سحب كل الداتا من Firebase إلى الجهاز */
    fun pullFromFirebase() = viewModelScope.launch {
        _busy.value = true
        try {
            withTimeout(30_000) { sync.pull(uid()) }
            runCatching { ensureListener() }
            _messages.emit("تم سحب البيانات من Firebase")
        } catch (e: Exception) {
            _messages.emit("فشل السحب: تأكد من الاتصال بالإنترنت")
        } finally {
            _busy.value = false
        }
    }

    /** تصدير الملخص الحالي كملف Excel في المكان اللي اختاره المستخدم */
    fun exportExcel(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val rows = repo.summary.first()
            getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                ExcelExporter.write(it, rows)
            }
            _messages.emit("تم تصدير ملف Excel")
        } catch (e: Exception) {
            _messages.emit("فشل التصدير: ${e.message}")
        }
    }

    fun toast(text: String) { _messages.tryEmit(text) }

    override fun onCleared() { listener?.remove() }
    private val _editing = MutableStateFlow<EditData?>(null)
    val editing = _editing.asStateFlow()

    fun startEdit(treatmentId: String) = viewModelScope.launch {
        val t = db.dao().findTreatmentById(treatmentId) ?: return@launch
        val p = db.dao().findPatientById(t.patientId) ?: return@launch
        _editing.value = EditData(
            t.id, p.name, p.bedNumber, t.drugName, t.startDay, t.endDay,
            t.doseTimes.replace(",", ", ")
        )
    }

    fun cancelEdit() { _editing.value = null }

    fun saveEdit(
        treatmentId: String, patient: String, bed: String, drug: String,
        start: LocalDate, end: LocalDate, times: List<LocalTime>
    ) = viewModelScope.launch {
        _busy.value = true
        try {
            val removed = repo.updateTreatment(treatmentId, patient, bed, drug, start, end, times)
            _editing.value = null
            _messages.emit("تم التعديل على الجهاز")
            try {
                withTimeout(60_000) {
                    val u = uid()
                    if (removed.isNotEmpty()) sync.deleteDoses(u, removed)
                    sync.push(u)
                }
                _messages.emit("تم رفع التعديل إلى Firebase")
            } catch (e: Exception) {
                android.util.Log.e("HospitalSync", "edit push failed", e)
                _messages.emit("اتعدّل على الجهاز لكن الرفع فشل: ${e.javaClass.simpleName} - ${e.message}")
            }
        } catch (e: IllegalArgumentException) {
            _messages.emit(e.message ?: "بيانات غير صحيحة")
        } finally {
            _busy.value = false
        }
    }

    /** مسح كل بيانات Room (المرضى والعلاجات والجرعات) من الجهاز فقط */
    fun clearLocalData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            db.clearAllTables()
            _messages.emit("تم مسح البيانات من الجهاز")
        } catch (e: Exception) {
            _messages.emit("فشل المسح: ${e.message}")
        }
    }
    /** إعادة رفع كل الداتا الموجودة على الجهاز */
    fun reuploadAll() = viewModelScope.launch {
        _busy.value = true
        try {
            db.dao().resetPatientsSync()
            db.dao().resetTreatmentsSync()
            db.dao().resetDosesSync()
            withTimeout(180_000) { sync.push(uid()) }
            runCatching { ensureListener() }
            _messages.emit("تمت إعادة رفع كل البيانات")
        } catch (e: Exception) {
            android.util.Log.e("HospitalSync", "reupload failed", e)
            _messages.emit("فشل: ${e.javaClass.simpleName} - ${e.message}")
        } finally {
            _busy.value = false
        }
    }
}