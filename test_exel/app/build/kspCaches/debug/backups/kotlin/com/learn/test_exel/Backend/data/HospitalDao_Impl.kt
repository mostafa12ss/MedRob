package com.learn.test_exel.Backend.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class HospitalDao_Impl(
  __db: RoomDatabase,
) : HospitalDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDoseLogEntity: EntityInsertAdapter<DoseLogEntity>

  private val __upsertAdapterOfPatientEntity: EntityUpsertAdapter<PatientEntity>

  private val __upsertAdapterOfTreatmentEntity: EntityUpsertAdapter<TreatmentEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDoseLogEntity = object : EntityInsertAdapter<DoseLogEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `dose_logs` (`id`,`treatmentId`,`scheduledAt`,`status`,`recordedAt`,`updatedAt`,`synced`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DoseLogEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.treatmentId)
        statement.bindLong(3, entity.scheduledAt)
        statement.bindText(4, __DoseStatus_enumToString(entity.status))
        val _tmpRecordedAt: Long? = entity.recordedAt
        if (_tmpRecordedAt == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpRecordedAt)
        }
        statement.bindLong(6, entity.updatedAt)
        val _tmp: Int = if (entity.synced) 1 else 0
        statement.bindLong(7, _tmp.toLong())
      }
    }
    this.__upsertAdapterOfPatientEntity = EntityUpsertAdapter<PatientEntity>(object : EntityInsertAdapter<PatientEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `patients` (`id`,`name`,`bedNumber`,`updatedAt`,`synced`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PatientEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.bedNumber)
        statement.bindLong(4, entity.updatedAt)
        val _tmp: Int = if (entity.synced) 1 else 0
        statement.bindLong(5, _tmp.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<PatientEntity>() {
      protected override fun createQuery(): String = "UPDATE `patients` SET `id` = ?,`name` = ?,`bedNumber` = ?,`updatedAt` = ?,`synced` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PatientEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.bedNumber)
        statement.bindLong(4, entity.updatedAt)
        val _tmp: Int = if (entity.synced) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindText(6, entity.id)
      }
    })
    this.__upsertAdapterOfTreatmentEntity = EntityUpsertAdapter<TreatmentEntity>(object : EntityInsertAdapter<TreatmentEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `treatments` (`id`,`patientId`,`drugName`,`startDay`,`endDay`,`doseTimes`,`updatedAt`,`synced`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TreatmentEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.patientId)
        statement.bindText(3, entity.drugName)
        statement.bindLong(4, entity.startDay)
        statement.bindLong(5, entity.endDay)
        statement.bindText(6, entity.doseTimes)
        statement.bindLong(7, entity.updatedAt)
        val _tmp: Int = if (entity.synced) 1 else 0
        statement.bindLong(8, _tmp.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<TreatmentEntity>() {
      protected override fun createQuery(): String = "UPDATE `treatments` SET `id` = ?,`patientId` = ?,`drugName` = ?,`startDay` = ?,`endDay` = ?,`doseTimes` = ?,`updatedAt` = ?,`synced` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TreatmentEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.patientId)
        statement.bindText(3, entity.drugName)
        statement.bindLong(4, entity.startDay)
        statement.bindLong(5, entity.endDay)
        statement.bindText(6, entity.doseTimes)
        statement.bindLong(7, entity.updatedAt)
        val _tmp: Int = if (entity.synced) 1 else 0
        statement.bindLong(8, _tmp.toLong())
        statement.bindText(9, entity.id)
      }
    })
  }

  public override suspend fun insertDoseLogs(logs: List<DoseLogEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDoseLogEntity.insert(_connection, logs)
  }

  public override suspend fun upsertPatient(p: PatientEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfPatientEntity.upsert(_connection, p)
  }

  public override suspend fun upsertTreatment(t: TreatmentEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfTreatmentEntity.upsert(_connection, t)
  }

  public override suspend fun findTreatmentById(id: String): TreatmentEntity? {
    val _sql: String = "SELECT * FROM treatments WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPatientId: Int = getColumnIndexOrThrow(_stmt, "patientId")
        val _columnIndexOfDrugName: Int = getColumnIndexOrThrow(_stmt, "drugName")
        val _columnIndexOfStartDay: Int = getColumnIndexOrThrow(_stmt, "startDay")
        val _columnIndexOfEndDay: Int = getColumnIndexOrThrow(_stmt, "endDay")
        val _columnIndexOfDoseTimes: Int = getColumnIndexOrThrow(_stmt, "doseTimes")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: TreatmentEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPatientId: String
          _tmpPatientId = _stmt.getText(_columnIndexOfPatientId)
          val _tmpDrugName: String
          _tmpDrugName = _stmt.getText(_columnIndexOfDrugName)
          val _tmpStartDay: Long
          _tmpStartDay = _stmt.getLong(_columnIndexOfStartDay)
          val _tmpEndDay: Long
          _tmpEndDay = _stmt.getLong(_columnIndexOfEndDay)
          val _tmpDoseTimes: String
          _tmpDoseTimes = _stmt.getText(_columnIndexOfDoseTimes)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _result = TreatmentEntity(_tmpId,_tmpPatientId,_tmpDrugName,_tmpStartDay,_tmpEndDay,_tmpDoseTimes,_tmpUpdatedAt,_tmpSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun dosesOf(treatmentId: String): List<DoseLogEntity> {
    val _sql: String = "SELECT * FROM dose_logs WHERE treatmentId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, treatmentId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTreatmentId: Int = getColumnIndexOrThrow(_stmt, "treatmentId")
        val _columnIndexOfScheduledAt: Int = getColumnIndexOrThrow(_stmt, "scheduledAt")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfRecordedAt: Int = getColumnIndexOrThrow(_stmt, "recordedAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<DoseLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DoseLogEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTreatmentId: String
          _tmpTreatmentId = _stmt.getText(_columnIndexOfTreatmentId)
          val _tmpScheduledAt: Long
          _tmpScheduledAt = _stmt.getLong(_columnIndexOfScheduledAt)
          val _tmpStatus: DoseStatus
          _tmpStatus = __DoseStatus_stringToEnum(_stmt.getText(_columnIndexOfStatus))
          val _tmpRecordedAt: Long?
          if (_stmt.isNull(_columnIndexOfRecordedAt)) {
            _tmpRecordedAt = null
          } else {
            _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          }
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = DoseLogEntity(_tmpId,_tmpTreatmentId,_tmpScheduledAt,_tmpStatus,_tmpRecordedAt,_tmpUpdatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeTreatmentOptions(): Flow<List<TreatmentOption>> {
    val _sql: String = """
        |
        |    SELECT t.id AS treatmentId, p.id AS patientId, p.name AS patientName,
        |           p.bedNumber AS bedNumber, t.drugName AS drugName
        |    FROM treatments t JOIN patients p ON p.id = t.patientId
        |    ORDER BY p.bedNumber, p.name, t.drugName
        |""".trimMargin()
    return createFlow(__db, false, arrayOf("treatments", "patients")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfTreatmentId: Int = 0
        val _columnIndexOfPatientId: Int = 1
        val _columnIndexOfPatientName: Int = 2
        val _columnIndexOfBedNumber: Int = 3
        val _columnIndexOfDrugName: Int = 4
        val _result: MutableList<TreatmentOption> = mutableListOf()
        while (_stmt.step()) {
          val _item: TreatmentOption
          val _tmpTreatmentId: String
          _tmpTreatmentId = _stmt.getText(_columnIndexOfTreatmentId)
          val _tmpPatientId: String
          _tmpPatientId = _stmt.getText(_columnIndexOfPatientId)
          val _tmpPatientName: String
          _tmpPatientName = _stmt.getText(_columnIndexOfPatientName)
          val _tmpBedNumber: String
          _tmpBedNumber = _stmt.getText(_columnIndexOfBedNumber)
          val _tmpDrugName: String
          _tmpDrugName = _stmt.getText(_columnIndexOfDrugName)
          _item = TreatmentOption(_tmpTreatmentId,_tmpPatientId,_tmpPatientName,_tmpBedNumber,_tmpDrugName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun findTreatmentOption(id: String): TreatmentOption? {
    val _sql: String = """
        |
        |    SELECT t.id AS treatmentId, p.id AS patientId, p.name AS patientName,
        |           p.bedNumber AS bedNumber, t.drugName AS drugName
        |    FROM treatments t JOIN patients p ON p.id = t.patientId
        |    WHERE t.id = ? LIMIT 1
        |""".trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfTreatmentId: Int = 0
        val _columnIndexOfPatientId: Int = 1
        val _columnIndexOfPatientName: Int = 2
        val _columnIndexOfBedNumber: Int = 3
        val _columnIndexOfDrugName: Int = 4
        val _result: TreatmentOption?
        if (_stmt.step()) {
          val _tmpTreatmentId: String
          _tmpTreatmentId = _stmt.getText(_columnIndexOfTreatmentId)
          val _tmpPatientId: String
          _tmpPatientId = _stmt.getText(_columnIndexOfPatientId)
          val _tmpPatientName: String
          _tmpPatientName = _stmt.getText(_columnIndexOfPatientName)
          val _tmpBedNumber: String
          _tmpBedNumber = _stmt.getText(_columnIndexOfBedNumber)
          val _tmpDrugName: String
          _tmpDrugName = _stmt.getText(_columnIndexOfDrugName)
          _result = TreatmentOption(_tmpTreatmentId,_tmpPatientId,_tmpPatientName,_tmpBedNumber,_tmpDrugName)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun findPatientById(id: String): PatientEntity? {
    val _sql: String = "SELECT * FROM patients WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBedNumber: Int = getColumnIndexOrThrow(_stmt, "bedNumber")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: PatientEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBedNumber: String
          _tmpBedNumber = _stmt.getText(_columnIndexOfBedNumber)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _result = PatientEntity(_tmpId,_tmpName,_tmpBedNumber,_tmpUpdatedAt,_tmpSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countTreatment(
    patientId: String,
    drug: String,
    start: Long,
    end: Long,
  ): Int {
    val _sql: String = """
        |
        |    SELECT COUNT(*) FROM treatments
        |    WHERE patientId = ? AND drugName = ? AND startDay = ? AND endDay = ?
        |""".trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, patientId)
        _argIndex = 2
        _stmt.bindText(_argIndex, drug)
        _argIndex = 3
        _stmt.bindLong(_argIndex, start)
        _argIndex = 4
        _stmt.bindLong(_argIndex, end)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun unsyncedPatients(): List<PatientEntity> {
    val _sql: String = "SELECT * FROM patients WHERE synced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBedNumber: Int = getColumnIndexOrThrow(_stmt, "bedNumber")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<PatientEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PatientEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBedNumber: String
          _tmpBedNumber = _stmt.getText(_columnIndexOfBedNumber)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = PatientEntity(_tmpId,_tmpName,_tmpBedNumber,_tmpUpdatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun unsyncedTreatments(): List<TreatmentEntity> {
    val _sql: String = "SELECT * FROM treatments WHERE synced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPatientId: Int = getColumnIndexOrThrow(_stmt, "patientId")
        val _columnIndexOfDrugName: Int = getColumnIndexOrThrow(_stmt, "drugName")
        val _columnIndexOfStartDay: Int = getColumnIndexOrThrow(_stmt, "startDay")
        val _columnIndexOfEndDay: Int = getColumnIndexOrThrow(_stmt, "endDay")
        val _columnIndexOfDoseTimes: Int = getColumnIndexOrThrow(_stmt, "doseTimes")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<TreatmentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TreatmentEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPatientId: String
          _tmpPatientId = _stmt.getText(_columnIndexOfPatientId)
          val _tmpDrugName: String
          _tmpDrugName = _stmt.getText(_columnIndexOfDrugName)
          val _tmpStartDay: Long
          _tmpStartDay = _stmt.getLong(_columnIndexOfStartDay)
          val _tmpEndDay: Long
          _tmpEndDay = _stmt.getLong(_columnIndexOfEndDay)
          val _tmpDoseTimes: String
          _tmpDoseTimes = _stmt.getText(_columnIndexOfDoseTimes)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = TreatmentEntity(_tmpId,_tmpPatientId,_tmpDrugName,_tmpStartDay,_tmpEndDay,_tmpDoseTimes,_tmpUpdatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun unsyncedDoses(): List<DoseLogEntity> {
    val _sql: String = "SELECT * FROM dose_logs WHERE synced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTreatmentId: Int = getColumnIndexOrThrow(_stmt, "treatmentId")
        val _columnIndexOfScheduledAt: Int = getColumnIndexOrThrow(_stmt, "scheduledAt")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfRecordedAt: Int = getColumnIndexOrThrow(_stmt, "recordedAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<DoseLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DoseLogEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTreatmentId: String
          _tmpTreatmentId = _stmt.getText(_columnIndexOfTreatmentId)
          val _tmpScheduledAt: Long
          _tmpScheduledAt = _stmt.getLong(_columnIndexOfScheduledAt)
          val _tmpStatus: DoseStatus
          _tmpStatus = __DoseStatus_stringToEnum(_stmt.getText(_columnIndexOfStatus))
          val _tmpRecordedAt: Long?
          if (_stmt.isNull(_columnIndexOfRecordedAt)) {
            _tmpRecordedAt = null
          } else {
            _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          }
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = DoseLogEntity(_tmpId,_tmpTreatmentId,_tmpScheduledAt,_tmpStatus,_tmpRecordedAt,_tmpUpdatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun findPatient(name: String, bed: String): PatientEntity? {
    val _sql: String = "SELECT * FROM patients WHERE name = ? AND bedNumber = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        _argIndex = 2
        _stmt.bindText(_argIndex, bed)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBedNumber: Int = getColumnIndexOrThrow(_stmt, "bedNumber")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: PatientEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBedNumber: String
          _tmpBedNumber = _stmt.getText(_columnIndexOfBedNumber)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _result = PatientEntity(_tmpId,_tmpName,_tmpBedNumber,_tmpUpdatedAt,_tmpSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observePatients(): Flow<List<PatientEntity>> {
    val _sql: String = "SELECT * FROM patients ORDER BY bedNumber, name"
    return createFlow(__db, false, arrayOf("patients")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBedNumber: Int = getColumnIndexOrThrow(_stmt, "bedNumber")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<PatientEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PatientEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBedNumber: String
          _tmpBedNumber = _stmt.getText(_columnIndexOfBedNumber)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = PatientEntity(_tmpId,_tmpName,_tmpBedNumber,_tmpUpdatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeTreatments(patientId: String): Flow<List<TreatmentEntity>> {
    val _sql: String = "SELECT * FROM treatments WHERE patientId = ? ORDER BY startDay"
    return createFlow(__db, false, arrayOf("treatments")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, patientId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPatientId: Int = getColumnIndexOrThrow(_stmt, "patientId")
        val _columnIndexOfDrugName: Int = getColumnIndexOrThrow(_stmt, "drugName")
        val _columnIndexOfStartDay: Int = getColumnIndexOrThrow(_stmt, "startDay")
        val _columnIndexOfEndDay: Int = getColumnIndexOrThrow(_stmt, "endDay")
        val _columnIndexOfDoseTimes: Int = getColumnIndexOrThrow(_stmt, "doseTimes")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<TreatmentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TreatmentEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPatientId: String
          _tmpPatientId = _stmt.getText(_columnIndexOfPatientId)
          val _tmpDrugName: String
          _tmpDrugName = _stmt.getText(_columnIndexOfDrugName)
          val _tmpStartDay: Long
          _tmpStartDay = _stmt.getLong(_columnIndexOfStartDay)
          val _tmpEndDay: Long
          _tmpEndDay = _stmt.getLong(_columnIndexOfEndDay)
          val _tmpDoseTimes: String
          _tmpDoseTimes = _stmt.getText(_columnIndexOfDoseTimes)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = TreatmentEntity(_tmpId,_tmpPatientId,_tmpDrugName,_tmpStartDay,_tmpEndDay,_tmpDoseTimes,_tmpUpdatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeDoses(treatmentId: String): Flow<List<DoseLogEntity>> {
    val _sql: String = "SELECT * FROM dose_logs WHERE treatmentId = ? ORDER BY scheduledAt"
    return createFlow(__db, false, arrayOf("dose_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, treatmentId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTreatmentId: Int = getColumnIndexOrThrow(_stmt, "treatmentId")
        val _columnIndexOfScheduledAt: Int = getColumnIndexOrThrow(_stmt, "scheduledAt")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfRecordedAt: Int = getColumnIndexOrThrow(_stmt, "recordedAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<DoseLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DoseLogEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTreatmentId: String
          _tmpTreatmentId = _stmt.getText(_columnIndexOfTreatmentId)
          val _tmpScheduledAt: Long
          _tmpScheduledAt = _stmt.getLong(_columnIndexOfScheduledAt)
          val _tmpStatus: DoseStatus
          _tmpStatus = __DoseStatus_stringToEnum(_stmt.getText(_columnIndexOfStatus))
          val _tmpRecordedAt: Long?
          if (_stmt.isNull(_columnIndexOfRecordedAt)) {
            _tmpRecordedAt = null
          } else {
            _tmpRecordedAt = _stmt.getLong(_columnIndexOfRecordedAt)
          }
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = DoseLogEntity(_tmpId,_tmpTreatmentId,_tmpScheduledAt,_tmpStatus,_tmpRecordedAt,_tmpUpdatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeSummary(): Flow<List<DoseSummaryRow>> {
    val _sql: String = """
        |
        |        SELECT t.id AS treatmentId,
        |               p.name AS patientName,
        |               p.bedNumber AS bedNumber,
        |               t.drugName AS drugName,
        |               COALESCE(SUM(CASE WHEN d.status = 'TAKEN'   THEN 1 ELSE 0 END), 0) AS takenCount,
        |               COALESCE(SUM(CASE WHEN d.status = 'MISSED'  THEN 1 ELSE 0 END), 0) AS missedCount,
        |               COALESCE(SUM(CASE WHEN d.status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pendingCount
        |        FROM treatments t
        |        JOIN patients p ON p.id = t.patientId
        |        LEFT JOIN dose_logs d ON d.treatmentId = t.id
        |        GROUP BY t.id
        |        ORDER BY p.bedNumber, p.name
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("treatments", "patients", "dose_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfTreatmentId: Int = 0
        val _columnIndexOfPatientName: Int = 1
        val _columnIndexOfBedNumber: Int = 2
        val _columnIndexOfDrugName: Int = 3
        val _columnIndexOfTakenCount: Int = 4
        val _columnIndexOfMissedCount: Int = 5
        val _columnIndexOfPendingCount: Int = 6
        val _result: MutableList<DoseSummaryRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: DoseSummaryRow
          val _tmpTreatmentId: String
          _tmpTreatmentId = _stmt.getText(_columnIndexOfTreatmentId)
          val _tmpPatientName: String
          _tmpPatientName = _stmt.getText(_columnIndexOfPatientName)
          val _tmpBedNumber: String
          _tmpBedNumber = _stmt.getText(_columnIndexOfBedNumber)
          val _tmpDrugName: String
          _tmpDrugName = _stmt.getText(_columnIndexOfDrugName)
          val _tmpTakenCount: Int
          _tmpTakenCount = _stmt.getLong(_columnIndexOfTakenCount).toInt()
          val _tmpMissedCount: Int
          _tmpMissedCount = _stmt.getLong(_columnIndexOfMissedCount).toInt()
          val _tmpPendingCount: Int
          _tmpPendingCount = _stmt.getLong(_columnIndexOfPendingCount).toInt()
          _item = DoseSummaryRow(_tmpTreatmentId,_tmpPatientName,_tmpBedNumber,_tmpDrugName,_tmpTakenCount,_tmpMissedCount,_tmpPendingCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun resetPatientsSync() {
    val _sql: String = "UPDATE patients SET synced = 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun resetTreatmentsSync() {
    val _sql: String = "UPDATE treatments SET synced = 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun resetDosesSync() {
    val _sql: String = "UPDATE dose_logs SET synced = 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDoses(ids: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM dose_logs WHERE id IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in ids) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markPatientDosesUnsynced(patientId: String) {
    val _sql: String = """
        |
        |    UPDATE dose_logs SET synced = 0
        |    WHERE treatmentId IN (SELECT id FROM treatments WHERE patientId = ?)
        |""".trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, patientId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markDoseSynced(id: String) {
    val _sql: String = "UPDATE dose_logs SET synced = 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun applyRemoteStatus(
    id: String,
    status: DoseStatus,
    recordedAt: Long?,
    now: Long,
  ) {
    val _sql: String = """
        |
        |    UPDATE dose_logs
        |    SET status = ?, recordedAt = ?, updatedAt = ?, synced = 1
        |    WHERE id = ?
        |""".trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, __DoseStatus_enumToString(status))
        _argIndex = 2
        if (recordedAt == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, recordedAt)
        }
        _argIndex = 3
        _stmt.bindLong(_argIndex, now)
        _argIndex = 4
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteTreatment(id: String) {
    val _sql: String = "DELETE FROM treatments WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markDose(
    doseId: String,
    status: DoseStatus,
    now: Long,
  ) {
    val _sql: String = """
        |
        |        UPDATE dose_logs
        |        SET status = ?, recordedAt = ?, updatedAt = ?, synced = 0
        |        WHERE id = ?
        |    
        """.trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, __DoseStatus_enumToString(status))
        _argIndex = 2
        _stmt.bindLong(_argIndex, now)
        _argIndex = 3
        _stmt.bindLong(_argIndex, now)
        _argIndex = 4
        _stmt.bindText(_argIndex, doseId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __DoseStatus_enumToString(_value: DoseStatus): String = when (_value) {
    DoseStatus.PENDING -> "PENDING"
    DoseStatus.TAKEN -> "TAKEN"
    DoseStatus.MISSED -> "MISSED"
  }

  private fun __DoseStatus_stringToEnum(_value: String): DoseStatus = when (_value) {
    "PENDING" -> DoseStatus.PENDING
    "TAKEN" -> DoseStatus.TAKEN
    "MISSED" -> DoseStatus.MISSED
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
