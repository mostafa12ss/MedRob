package com.learn.test_exel.Backend.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class DoseStatus { PENDING, TAKEN, MISSED }

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val bedNumber: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(
    tableName = "treatments",
    foreignKeys = [ForeignKey(
        entity = PatientEntity::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientId")]
)
data class TreatmentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val drugName: String,
    val startDay: Long,          // LocalDate.toEpochDay()
    val endDay: Long,
    val doseTimes: String,       // "08:00,14:00,20:00"
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(
    tableName = "dose_logs",
    foreignKeys = [ForeignKey(
        entity = TreatmentEntity::class,
        parentColumns = ["id"],
        childColumns = ["treatmentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("treatmentId")]
)
data class DoseLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val treatmentId: String,
    val scheduledAt: Long,       // epoch millis لموعد الجرعة
    val status: DoseStatus = DoseStatus.PENDING,
    val recordedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

// صف الملخص (جدولك الأول) — مش جدول مخزّن، ده نتيجة Query
data class DoseSummaryRow(
    val treatmentId: String,
    val patientName: String,
    val bedNumber: String,
    val drugName: String,
    val takenCount: Int,
    val missedCount: Int,
    val pendingCount: Int
)