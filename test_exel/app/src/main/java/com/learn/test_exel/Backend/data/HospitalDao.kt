package com.learn.test_exel.Backend.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalDao {
    @Query("UPDATE patients SET synced = 0") suspend fun resetPatientsSync()
    @Query("UPDATE treatments SET synced = 0") suspend fun resetTreatmentsSync()
    @Query("UPDATE dose_logs SET synced = 0") suspend fun resetDosesSync()
    @Query("SELECT * FROM treatments WHERE id = :id LIMIT 1")
    suspend fun findTreatmentById(id: String): TreatmentEntity?

    @Query("SELECT * FROM dose_logs WHERE treatmentId = :treatmentId")
    suspend fun dosesOf(treatmentId: String): List<DoseLogEntity>

    @Query("DELETE FROM dose_logs WHERE id IN (:ids)")
    suspend fun deleteDoses(ids: List<String>)

    // بيعلّم جرعات كل علاجات المريض "لم تُرفع" عشان الاسم والسرير والدواء الجداد يتبعتوا
    @Query("""
    UPDATE dose_logs SET synced = 0
    WHERE treatmentId IN (SELECT id FROM treatments WHERE patientId = :patientId)
""")
    suspend fun markPatientDosesUnsynced(patientId: String)
    @Query("""
    SELECT t.id AS treatmentId, p.id AS patientId, p.name AS patientName,
           p.bedNumber AS bedNumber, t.drugName AS drugName
    FROM treatments t JOIN patients p ON p.id = t.patientId
    ORDER BY p.bedNumber, p.name, t.drugName
""")
    fun observeTreatmentOptions(): Flow<List<TreatmentOption>>

    @Query("""
    SELECT t.id AS treatmentId, p.id AS patientId, p.name AS patientName,
           p.bedNumber AS bedNumber, t.drugName AS drugName
    FROM treatments t JOIN patients p ON p.id = t.patientId
    WHERE t.id = :id LIMIT 1
""")
    suspend fun findTreatmentOption(id: String): TreatmentOption?

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    suspend fun findPatientById(id: String): PatientEntity?
    @Query("""
    SELECT COUNT(*) FROM treatments
    WHERE patientId = :patientId AND drugName = :drug AND startDay = :start AND endDay = :end
""")
    suspend fun countTreatment(patientId: String, drug: String, start: Long, end: Long): Int
    // ---- للمزامنة ----
    @Query("SELECT * FROM patients WHERE synced = 0")
    suspend fun unsyncedPatients(): List<PatientEntity>

    @Query("SELECT * FROM treatments WHERE synced = 0")
    suspend fun unsyncedTreatments(): List<TreatmentEntity>

    @Query("SELECT * FROM dose_logs WHERE synced = 0")
    suspend fun unsyncedDoses(): List<DoseLogEntity>

    @Query("UPDATE dose_logs SET synced = 1 WHERE id = :id")
    suspend fun markDoseSynced(id: String)

    // تطبيق الحالة القادمة من Firebase
    @Query("""
    UPDATE dose_logs
    SET status = :status, recordedAt = :recordedAt, updatedAt = :now, synced = 1
    WHERE id = :id
""")
    suspend fun applyRemoteStatus(id: String, status: DoseStatus, recordedAt: Long?, now: Long)

    // ---- المرضى ----
    @Upsert suspend fun upsertPatient(p: PatientEntity)

    @Query("SELECT * FROM patients WHERE name = :name AND bedNumber = :bed LIMIT 1")
    suspend fun findPatient(name: String, bed: String): PatientEntity?

    @Query("SELECT * FROM patients ORDER BY bedNumber, name")
    fun observePatients(): Flow<List<PatientEntity>>

    // ---- خطط العلاج (الجدول التاني) ----
    @Upsert suspend fun upsertTreatment(t: TreatmentEntity)

    @Query("SELECT * FROM treatments WHERE patientId = :patientId ORDER BY startDay")
    fun observeTreatments(patientId: String): Flow<List<TreatmentEntity>>

    @Query("DELETE FROM treatments WHERE id = :id")
    suspend fun deleteTreatment(id: String)

    // ---- الجرعات ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLogs(logs: List<DoseLogEntity>)

    @Query("SELECT * FROM dose_logs WHERE treatmentId = :treatmentId ORDER BY scheduledAt")
    fun observeDoses(treatmentId: String): Flow<List<DoseLogEntity>>

    @Query("""
        UPDATE dose_logs
        SET status = :status, recordedAt = :now, updatedAt = :now, synced = 0
        WHERE id = :doseId
    """)
    suspend fun markDose(doseId: String, status: DoseStatus, now: Long)

    // ---- الملخص (الجدول الأول) ----
    @Query("""
        SELECT t.id AS treatmentId,
               p.name AS patientName,
               p.bedNumber AS bedNumber,
               t.drugName AS drugName,
               COALESCE(SUM(CASE WHEN d.status = 'TAKEN'   THEN 1 ELSE 0 END), 0) AS takenCount,
               COALESCE(SUM(CASE WHEN d.status = 'MISSED'  THEN 1 ELSE 0 END), 0) AS missedCount,
               COALESCE(SUM(CASE WHEN d.status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pendingCount
        FROM treatments t
        JOIN patients p ON p.id = t.patientId
        LEFT JOIN dose_logs d ON d.treatmentId = t.id
        GROUP BY t.id
        ORDER BY p.bedNumber, p.name
    """)

    fun observeSummary(): Flow<List<DoseSummaryRow>>
}
data class TreatmentOption(
    val treatmentId: String,
    val patientId: String,
    val patientName: String,
    val bedNumber: String,
    val drugName: String
)