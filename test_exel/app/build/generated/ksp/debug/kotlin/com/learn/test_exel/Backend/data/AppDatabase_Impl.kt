package com.learn.test_exel.Backend.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _hospitalDao: Lazy<HospitalDao> = lazy {
    HospitalDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "12cc2a71553a5d9a494393f23a4a17c4", "9bbcb583a38e0074925b3f5a21bdf367") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `patients` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `bedNumber` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `treatments` (`id` TEXT NOT NULL, `patientId` TEXT NOT NULL, `drugName` TEXT NOT NULL, `startDay` INTEGER NOT NULL, `endDay` INTEGER NOT NULL, `doseTimes` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`patientId`) REFERENCES `patients`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_treatments_patientId` ON `treatments` (`patientId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `dose_logs` (`id` TEXT NOT NULL, `treatmentId` TEXT NOT NULL, `scheduledAt` INTEGER NOT NULL, `status` TEXT NOT NULL, `recordedAt` INTEGER, `updatedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`treatmentId`) REFERENCES `treatments`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_dose_logs_treatmentId` ON `dose_logs` (`treatmentId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '12cc2a71553a5d9a494393f23a4a17c4')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `patients`")
        connection.execSQL("DROP TABLE IF EXISTS `treatments`")
        connection.execSQL("DROP TABLE IF EXISTS `dose_logs`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsPatients: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPatients.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPatients.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPatients.put("bedNumber", TableInfo.Column("bedNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPatients.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPatients.put("synced", TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPatients: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPatients: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPatients: TableInfo = TableInfo("patients", _columnsPatients, _foreignKeysPatients, _indicesPatients)
        val _existingPatients: TableInfo = read(connection, "patients")
        if (!_infoPatients.equals(_existingPatients)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |patients(com.learn.test_exel.Backend.data.PatientEntity).
              | Expected:
              |""".trimMargin() + _infoPatients + """
              |
              | Found:
              |""".trimMargin() + _existingPatients)
        }
        val _columnsTreatments: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTreatments.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTreatments.put("patientId", TableInfo.Column("patientId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTreatments.put("drugName", TableInfo.Column("drugName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTreatments.put("startDay", TableInfo.Column("startDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTreatments.put("endDay", TableInfo.Column("endDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTreatments.put("doseTimes", TableInfo.Column("doseTimes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTreatments.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTreatments.put("synced", TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTreatments: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysTreatments.add(TableInfo.ForeignKey("patients", "CASCADE", "NO ACTION", listOf("patientId"), listOf("id")))
        val _indicesTreatments: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTreatments.add(TableInfo.Index("index_treatments_patientId", false, listOf("patientId"), listOf("ASC")))
        val _infoTreatments: TableInfo = TableInfo("treatments", _columnsTreatments, _foreignKeysTreatments, _indicesTreatments)
        val _existingTreatments: TableInfo = read(connection, "treatments")
        if (!_infoTreatments.equals(_existingTreatments)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |treatments(com.learn.test_exel.Backend.data.TreatmentEntity).
              | Expected:
              |""".trimMargin() + _infoTreatments + """
              |
              | Found:
              |""".trimMargin() + _existingTreatments)
        }
        val _columnsDoseLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDoseLogs.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDoseLogs.put("treatmentId", TableInfo.Column("treatmentId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDoseLogs.put("scheduledAt", TableInfo.Column("scheduledAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDoseLogs.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDoseLogs.put("recordedAt", TableInfo.Column("recordedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDoseLogs.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDoseLogs.put("synced", TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDoseLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysDoseLogs.add(TableInfo.ForeignKey("treatments", "CASCADE", "NO ACTION", listOf("treatmentId"), listOf("id")))
        val _indicesDoseLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesDoseLogs.add(TableInfo.Index("index_dose_logs_treatmentId", false, listOf("treatmentId"), listOf("ASC")))
        val _infoDoseLogs: TableInfo = TableInfo("dose_logs", _columnsDoseLogs, _foreignKeysDoseLogs, _indicesDoseLogs)
        val _existingDoseLogs: TableInfo = read(connection, "dose_logs")
        if (!_infoDoseLogs.equals(_existingDoseLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |dose_logs(com.learn.test_exel.Backend.data.DoseLogEntity).
              | Expected:
              |""".trimMargin() + _infoDoseLogs + """
              |
              | Found:
              |""".trimMargin() + _existingDoseLogs)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "patients", "treatments", "dose_logs")
  }

  public override fun clearAllTables() {
    super.performClear(true, "patients", "treatments", "dose_logs")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(HospitalDao::class, HospitalDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun dao(): HospitalDao = _hospitalDao.value
}
