package com.learn.robmed.tools

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

data class PatientInfo(val name: String, val bedNumber: String)

// العودة للمسار الصحيح تحت مجلد users والـ UID الخاص بك
private fun userDoc() =
    Firebase.firestore.collection("users").document("XJHF4C4UfuQySg5LAQySWnuBswJ3")
// (ملاحظة: لو كنت تستخدم تسجيل الدخول الحقيقي في التطبيق ويطابق هذا الحساب،
// يمكنك استبداله بـ: Firebase.auth.currentUser!!.uid)

fun loadPatient(
    id: String,
    onDone: (PatientInfo?) -> Unit
) {
    val patientId = id.trim()
    val uid = Firebase.auth.currentUser?.uid

    if (patientId.isBlank()) {
        android.util.Log.e("RobMedQR", "Patient ID is empty")
        onDone(null)
        return
    }

    // استخدام UID الثابت مؤقتًا للتأكد من الحساب الصحيح
    val userId = "XJHF4C4UfuQySg5LAQySWnuBswJ3"

    val ref = Firebase.firestore
        .collection("users")
        .document(userId)
        .collection("patients")
        .document(patientId)

    android.util.Log.d("RobMedQR", "Logged UID = $uid")
    android.util.Log.d("RobMedQR", "Requested ID = [$patientId]")
    android.util.Log.d("RobMedQR", "Document path = ${ref.path}")

    ref.get()
        .addOnSuccessListener { document ->
            android.util.Log.d(
                "RobMedQR",
                "Document exists = ${document.exists()}"
            )

            if (document.exists()) {
                android.util.Log.d(
                    "RobMedQR",
                    "Patient data = ${document.data?.keys}"
                )

                onDone(
                    PatientInfo(
                        name = document.getString("name").orEmpty(),
                        bedNumber = document.getString("bedNumber").orEmpty()
                    )
                )
            } else {
                android.util.Log.e(
                    "RobMedQR",
                    "Patient document not found at ${ref.path}"
                )
                onDone(null)
            }
        }
        .addOnFailureListener { error ->
            android.util.Log.e(
                "RobMedQR",
                "Firebase read failed",
                error
            )
            onDone(null)
        }
}

data class Treatment(
    val id: String,
    val drugName: String,
    val doseTimes: List<String>,
    val startDay: Long,
    val endDay: Long
)

@RequiresApi(Build.VERSION_CODES.O)
fun loadTreatments(patientId: String, onDone: (List<Treatment>) -> Unit) {
    val today = java.time.LocalDate.now().toEpochDay()
    userDoc().collection("treatments")
        .whereEqualTo("patientId", patientId)
        .get()
        .addOnSuccessListener { snap ->
            val list = snap.documents.mapNotNull { d ->
                val start = d.getLong("startDay") ?: return@mapNotNull null
                val end = d.getLong("endDay") ?: return@mapNotNull null
                if (today < start || today > end) return@mapNotNull null  // العلاج الساري فقط
                Treatment(
                    id = d.id,
                    drugName = d.getString("drugName").orEmpty(),
                    doseTimes = d.getString("doseTimes").orEmpty()
                        .split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    startDay = start,
                    endDay = end
                )
            }
            onDone(list)
        }
        .addOnFailureListener { onDone(emptyList()) }
}

fun logDose(treatmentId: String, scheduledAt: Long, onDone: (Boolean) -> Unit) {
    userDoc().collection("doseLogs").add(
        mapOf(
            "treatmentId" to treatmentId,
            "scheduledAt" to scheduledAt,
            "givenAt" to System.currentTimeMillis(),
            "status" to "given"
        )
    )
        .addOnSuccessListener { onDone(true) }
        .addOnFailureListener { onDone(false) }
}

// يحول "08:00" لميلي ثانية النهاردة
@RequiresApi(Build.VERSION_CODES.O)
fun scheduledMillis(time: String): Long {
    val (h, m) = time.split(":").map { it.trim().toInt() }
    return java.time.LocalDate.now()
        .atTime(h, m)
        .atZone(java.time.ZoneId.systemDefault())
        .toInstant().toEpochMilli()
}