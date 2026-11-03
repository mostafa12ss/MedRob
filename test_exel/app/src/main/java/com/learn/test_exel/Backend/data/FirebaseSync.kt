package com.learn.test_exel.Backend.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.text.get

private fun Boolean?.toStatus(): DoseStatus = when (this) {
    true -> DoseStatus.TAKEN
    false -> DoseStatus.MISSED
    null -> DoseStatus.PENDING
}

class FirebaseSync(
    private val dao: HospitalDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun root(uid: String) = firestore.collection("users").document(uid)
    suspend fun deleteDoses(uid: String, ids: List<String>) {
        ids.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(root(uid).collection("doseLogs").document(it)) }
            batch.commit().await()
        }
    }
    /** رفع البيانات الجديدة. مش بنكتب حقل taken أبدًا عشان ما نمسحش اللي كتبه النظام التاني */
    suspend fun push(uid: String) {
        val patients = dao.unsyncedPatients()
        val treatments = dao.unsyncedTreatments()
        val doses = dao.unsyncedDoses()

        // 1) المرضى والعلاجات
        patients.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { p ->
                batch.set(
                    root(uid).collection("patients").document(p.id),
                    mapOf("name" to p.name, "bedNumber" to p.bedNumber, "updatedAt" to p.updatedAt),
                    SetOptions.merge()
                )
            }
            batch.commit().await()
            chunk.forEach { dao.upsertPatient(it.copy(synced = true)) }
        }

        treatments.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { t ->
                batch.set(
                    root(uid).collection("treatments").document(t.id),
                    mapOf(
                        "patientId" to t.patientId,
                        "drugName" to t.drugName,
                        "startDay" to t.startDay,
                        "endDay" to t.endDay,
                        "doseTimes" to t.doseTimes,
                        "updatedAt" to t.updatedAt
                    ),
                    SetOptions.merge()
                )
            }
            batch.commit().await()
            chunk.forEach { dao.upsertTreatment(it.copy(synced = true)) }
        }

        // 2) الجرعات (من غير حقل taken)
        doses.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { d ->
                batch.set(
                    root(uid).collection("doseLogs").document(d.id),
                    mapOf("treatmentId" to d.treatmentId, "scheduledAt" to d.scheduledAt),
                    SetOptions.merge()
                )
            }
            batch.commit().await()
            chunk.forEach { dao.markDoseSynced(it.id) }
        }
    }
    /** سحب كل البيانات من Firebase وتخزينها في Room. الترتيب مهم بسبب الـ Foreign Keys */
    suspend fun pull(uid: String) {
        root(uid).collection("patients").get().await().documents.forEach { d ->
            val name = d.getString("name") ?: return@forEach
            dao.upsertPatient(
                PatientEntity(
                    id = d.id, name = name,
                    bedNumber = d.getString("bedNumber") ?: "",
                    updatedAt = d.getLong("updatedAt") ?: 0L, synced = true
                )
            )
        }
        root(uid).collection("treatments").get().await().documents.forEach { d ->
            val patientId = d.getString("patientId") ?: return@forEach
            val drug = d.getString("drugName") ?: return@forEach
            // لو المريض مش موجود يتخطى الصف بدل ما يقع التطبيق
            runCatching {
                dao.upsertTreatment(
                    TreatmentEntity(
                        id = d.id, patientId = patientId, drugName = drug,
                        startDay = d.getLong("startDay") ?: 0L,
                        endDay = d.getLong("endDay") ?: 0L,
                        doseTimes = d.getString("doseTimes") ?: "",
                        updatedAt = d.getLong("updatedAt") ?: 0L, synced = true
                    )
                )
            }
        }
        val doses = root(uid).collection("doseLogs").get().await().documents.mapNotNull { d ->
            val treatmentId = d.getString("treatmentId") ?: return@mapNotNull null
            DoseLogEntity(
                id = d.id, treatmentId = treatmentId,
                scheduledAt = d.getLong("scheduledAt") ?: return@mapNotNull null,
                status = d.getBoolean("taken").toStatus(),   // true=أخد، false=ما أخدش، مفيش=لسه
                recordedAt = d.getLong("recordedAt"),
                synced = true
            )
        }
        runCatching { dao.insertDoseLogs(doses) }
    }
    /** استماع لحظي: أي تغيير في taken بيتطبق على Room فورًا */
    fun listenToDoseStatus(uid: String, scope: CoroutineScope): ListenerRegistration =
        root(uid).collection("doseLogs").addSnapshotListener { snap, error ->
            if (error != null || snap == null) return@addSnapshotListener
            scope.launch {
                snap.documentChanges
                    .filter { it.type != DocumentChange.Type.REMOVED }
                    .forEach { change ->
                        val d = change.document
                        dao.applyRemoteStatus(
                            id = d.id,
                            status = d.getBoolean("taken").toStatus(),
                            recordedAt = d.getLong("recordedAt"),
                            now = System.currentTimeMillis()
                        )
                    }
            }
        }
}
