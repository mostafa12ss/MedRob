package com.learn.test_exel.Backend.data


import androidx.room.withTransaction
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class HospitalRepository(private val db: AppDatabase) {
    private val dao = db.dao()

    val summary = dao.observeSummary()
    val patients = dao.observePatients()
    fun doses(treatmentId: String) = dao.observeDoses(treatmentId)

    /** الفورم الواحد: بيانات المريض + الدواء + الفترة + مواعيد الجرعات + بيانات الدخول والخروج والتوقيت */
    /** تعديل علاج موجود. بيرجّع IDs الجرعات اللي اتمسحت عشان تتمسح من Firebase كمان */
    suspend fun updateTreatment(
        treatmentId: String,
        patientName: String,
        bedNumber: String,
        drugName: String,
        start: LocalDate,
        end: LocalDate,
        times: List<LocalTime>
    ): List<String> {
        require(patientName.isNotBlank() && bedNumber.isNotBlank() && drugName.isNotBlank()) {
            "اكمل اسم المريض والسرير والدواء"
        }
        require(!end.isBefore(start)) { "تاريخ النهاية قبل البداية" }
        require(times.isNotEmpty()) { "حدد موعد جرعة واحد على الأقل" }
        val days = end.toEpochDay() - start.toEpochDay() + 1
        require(days * times.size <= 1000) { "عدد الجرعات كبير جدًا" }

        return db.withTransaction {
            val t = dao.findTreatmentById(treatmentId)
                ?: throw IllegalArgumentException("العلاج مش موجود")
            val p = dao.findPatientById(t.patientId)
                ?: throw IllegalArgumentException("المريض مش موجود")
            val now = System.currentTimeMillis()
            val sorted = times.sorted()

            dao.upsertPatient(
                p.copy(name = patientName.trim(), bedNumber = bedNumber.trim(), updatedAt = now, synced = false)
            )
            dao.upsertTreatment(
                t.copy(
                    drugName = drugName.trim(),
                    startDay = start.toEpochDay(),
                    endDay = end.toEpochDay(),
                    doseTimes = sorted.joinToString(",") { it.toString() },
                    updatedAt = now,
                    synced = false
                )
            )

            val zone = ZoneId.systemDefault()
            val wanted = generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .flatMap { day -> sorted.map { day.atTime(it).atZone(zone).toInstant().toEpochMilli() } }
                .toList()
            val wantedSet = wanted.toSet()

            val existing = dao.dosesOf(treatmentId)
            val existingTimes = existing.map { it.scheduledAt }.toSet()

            // بنمسح بس الجرعات اللي لسه PENDING وخرجت من الجدول الجديد
            val toRemove = existing
                .filter { it.scheduledAt !in wantedSet && it.status == DoseStatus.PENDING }
                .map { it.id }
            toRemove.chunked(500).forEach { dao.deleteDoses(it) }

            dao.insertDoseLogs(
                wanted.filter { it !in existingTimes }
                    .map { DoseLogEntity(treatmentId = treatmentId, scheduledAt = it) }
            )

            dao.markPatientDosesUnsynced(p.id)
            toRemove
        }
    }
    suspend fun addTreatment(
        patientName: String,
        bedNumber: String,
        drugName: String,
        start: LocalDate,
        end: LocalDate,
        times: List<LocalTime>,
        entryTime: String = "",
        exitTime: String = "",
        timing: String = "",
        skipIfExists: Boolean = false
    ): Boolean {
        require(!end.isBefore(start)) { "تاريخ النهاية قبل البداية" }
        require(times.isNotEmpty()) { "حدد موعد جرعة واحد على الأقل" }

        return db.withTransaction {
            val patient = dao.findPatient(patientName.trim(), bedNumber.trim())
                ?: PatientEntity(name = patientName.trim(), bedNumber = bedNumber.trim())
                    .also { dao.upsertPatient(it) }

            if (skipIfExists && dao.countTreatment(
                    patient.id, drugName.trim(), start.toEpochDay(), end.toEpochDay()
                ) > 0
            ) return@withTransaction false

            val sortedTimes = times.sorted()
            val treatment = TreatmentEntity(
                patientId = patient.id,
                drugName = drugName.trim(),
                startDay = start.toEpochDay(),
                endDay = end.toEpochDay(),
                doseTimes = sortedTimes.joinToString(",") { it.toString() }
                // إذا كانت الجداول في قاعدة البيانات تحتوي على أعمدة للدخول والخروج والتوقيت، يمكنك إضافتها هنا أيضاً
            )
            dao.upsertTreatment(treatment)

            val zone = ZoneId.systemDefault()
            val logs = generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .flatMap { day ->
                    sortedTimes.map { t ->
                        DoseLogEntity(
                            treatmentId = treatment.id,
                            scheduledAt = day.atTime(t).atZone(zone).toInstant().toEpochMilli()
                        )
                    }
                }.toList()
            dao.insertDoseLogs(logs)
            true
        }
    }

    /** استيراد صفوف Excel كلها في transaction واحدة. بيرجّع (اتضاف، اتخطى لأنه مكرر) */
    suspend fun importRows(rows: List<ImportRow>): Pair<Int, Int> {
        var added = 0
        var skipped = 0
        db.withTransaction {
            rows.forEach { r ->
                // ملحوظة: إذا كان ملف الإكسل لا يحتوي على أعمدة "من تاريخ" و"إلى تاريخ" صريحة
                // يمكنك هنا تعبئة تواريخ افتراضية (مثل تاريخ اليوم) لكي لا يحدث خطأ في إنشاء الجرعات،
                // أو تعديل استخراج التواريخ بناءً على البيانات الواردة من ملف الإكسل الخاص بك.
                val defaultStart = LocalDate.now()
                val defaultEnd = LocalDate.now().plusDays(3) // مثال افتراضي
                val defaultTimes = listOf(LocalTime.of(8, 0)) // وقت افتراضي للجرعة

                if (addTreatment(
                        patientName = r.patient,
                        bedNumber = r.bed,
                        drugName = r.drug,
                        start = defaultStart,
                        end = defaultEnd,
                        times = defaultTimes,
                        entryTime = r.entryTime,
                        exitTime = r.exitTime,
                        timing = r.timing,
                        skipIfExists = true
                    ))
                    added++ else skipped++
            }
        }
        return added to skipped
    }

    suspend fun markDose(doseId: String, status: DoseStatus) =
        dao.markDose(doseId, status, System.currentTimeMillis())

    suspend fun deleteTreatment(id: String) = dao.deleteTreatment(id)
}