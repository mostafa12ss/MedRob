package com.learn.test_exel.Backend.data

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

data class ImportRow(
    val patient: String,
    val bed: String,
    val drug: String,
    val takenCount: Int,
    val missedCount: Int,
    val pendingCount: Int,
    val entryTime: String,
    val exitTime: String,
    val timing: String
)

data class ImportResult(val rows: List<ImportRow>, val errors: List<String>)

object ExcelImporter {

    private class Cell(val text: String, val number: Double?)
    private class SheetRow(val number: Int, val cells: Map<Int, Cell>)

    private val LABELS = mapOf(
        "patient" to "اسم المريض",
        "bed" to "رقم السرير",
        "drug" to "اسم الدواء",
        "taken" to "أخد (مرات)",
        "missed" to "ما أخدش (مرات)",
        "pending" to "لسه",
        "entryTime" to "موعد الدخول",
        "exitTime" to "موعد الخروج",
        "timing" to "التوقيت"
    )

    private val ALIASES: Map<String, String> = mapOf(
        "patient" to listOf("اسم المريض", "المريض", "patient", "name"),
        "bed" to listOf("رقم السرير", "السرير", "bed", "bed number"),
        "drug" to listOf("اسم الدواء", "الدواء", "drug", "medicine"),
        "taken" to listOf("أخد (مرات)", "أخد", "taken"),
        "missed" to listOf("ما أخدش (مرات)", "ما أخدش", "missed"),
        "pending" to listOf("لسه", "معلق", "pending"),
        "entryTime" to listOf("موعد الدخول", "دخول", "entry time"),
        "exitTime" to listOf("موعد الخروج", "خروج", "exit time"),
        "timing" to listOf("التوقيت", "الوقت", "timing")
    ).flatMap { (field, names) -> names.map { norm(it) to field } }.toMap()

    private fun normalizeDigits(s: String): String = buildString {
        for (ch in s) append(
            when (ch) {
                in '٠'..'٩' -> '0' + (ch - '٠')
                in '۰'..'۹' -> '0' + (ch - '۰')
                else -> ch
            }
        )
    }

    private fun norm(s: String): String = normalizeDigits(s).trim().lowercase()
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
        .replace('ى', 'ي').replace('ة', 'ه')
        .replace(Regex("\\s+"), " ")

    fun parse(input: InputStream): ImportResult {
        var shared: ByteArray? = null
        val sheets = sortedMapOf<Int, ByteArray>()

        ZipInputStream(input).use { zip ->
            var e = zip.nextEntry
            while (e != null) {
                val name = e.name
                when {
                    name == "xl/sharedStrings.xml" -> shared = zip.readBytes()
                    name.startsWith("xl/worksheets/sheet") && name.endsWith(".xml") -> {
                        val n = name.removePrefix("xl/worksheets/sheet").removeSuffix(".xml").toIntOrNull() ?: 0
                        sheets[n] = zip.readBytes()
                    }
                }
                e = zip.nextEntry
            }
        }
        if (sheets.isEmpty())
            throw IllegalArgumentException("الملف مش Excel بصيغة xlsx.")

        val strings = shared?.let { parseSharedStrings(it) } ?: emptyList()
        val rows = parseSheet(sheets.values.first(), strings)

        val header = rows.firstOrNull { r -> r.cells.values.any { it.text.isNotBlank() } }
            ?: throw IllegalArgumentException("الملف فاضي")

        val cols = mutableMapOf<String, Int>()
        header.cells.forEach { (idx, c) -> ALIASES[norm(c.text)]?.let { cols.putIfAbsent(it, idx) } }

        // التحقق من الأعمدة الإجبارية الأساسية
        val missing = listOf("patient", "bed", "drug").filter { it !in cols }
        if (missing.isNotEmpty())
            throw IllegalArgumentException("أعمدة أساسية ناقصة في الصف الأول تأكد من مطابقتها للصورة.")

        val result = mutableListOf<ImportRow>()
        val errors = mutableListOf<String>()

        for (r in rows) {
            if (r.number <= header.number) continue
            if (r.cells.values.all { it.text.isBlank() }) continue

            fun text(f: String) = cols[f]?.let { r.cells[it]?.text?.trim() }.orEmpty()
            fun intVal(f: String) = text(f).toIntOrNull() ?: 0

            val patient = text("patient")
            val bed = text("bed")
            val drug = text("drug")
            val taken = intVal("taken")
            val missed = intVal("missed")
            val pending = intVal("pending")
            val entryTime = text("entryTime")
            val exitTime = text("exitTime")
            val timing = text("timing")

            val problems = mutableListOf<String>()
            if (patient.isEmpty()) problems += "اسم المريض ناقص"
            if (bed.isEmpty()) problems += "رقم السرير ناقص"
            if (drug.isEmpty()) problems += "اسم الدواء ناقص"

            if (problems.isNotEmpty()) {
                errors += "صف ${r.number}: ${problems.joinToString("، ")}"
            } else {
                result += ImportRow(patient, bed, drug, taken, missed, pending, entryTime, exitTime, timing)
            }
        }
        return ImportResult(result, errors)
    }

    private fun parseSharedStrings(xml: ByteArray): List<String> {
        val p = Xml.newPullParser()
        p.setInput(ByteArrayInputStream(xml), null)
        val list = mutableListOf<String>()
        var sb = StringBuilder()
        var event = p.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (p.name) {
                    "si" -> sb = StringBuilder()
                    "t" -> sb.append(p.nextText())
                }
            } else if (event == XmlPullParser.END_TAG && p.name == "si") {
                list += sb.toString()
            }
            event = p.next()
        }
        return list
    }

    private fun colIndex(ref: String?): Int {
        var n = 0
        for (ch in ref.orEmpty()) {
            if (ch in 'A'..'Z') n = n * 26 + (ch - 'A' + 1) else break
        }
        return n - 1
    }

    private fun parseSheet(xml: ByteArray, shared: List<String>): List<SheetRow> {
        val p = Xml.newPullParser()
        p.setInput(ByteArrayInputStream(xml), null)

        val rows = mutableListOf<SheetRow>()
        var cells = mutableMapOf<Int, Cell>()
        var rowNum = 0
        var col = 0
        var type: String? = null
        var v: String? = null
        var inline = StringBuilder()

        var event = p.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (p.name) {
                    "row" -> {
                        cells = mutableMapOf()
                        rowNum = p.getAttributeValue(null, "r")?.toIntOrNull() ?: (rowNum + 1)
                    }
                    "c" -> {
                        col = colIndex(p.getAttributeValue(null, "r"))
                        type = p.getAttributeValue(null, "t")
                        v = null
                        inline = StringBuilder()
                    }
                    "v" -> v = p.nextText()
                    "t" -> if (type == "inlineStr") inline.append(p.nextText())
                }
            } else if (event == XmlPullParser.END_TAG) {
                when (p.name) {
                    "c" -> {
                        val raw = v
                        val cell = when (type) {
                            "s" -> Cell(shared.getOrNull(raw?.toIntOrNull() ?: -1).orEmpty(), null)
                            "inlineStr" -> Cell(inline.toString(), null)
                            "str", "e", "b" -> Cell(raw.orEmpty(), null)
                            else -> {
                                val num = raw?.toDoubleOrNull()
                                val txt = when {
                                    num == null -> raw.orEmpty()
                                    num % 1.0 == 0.0 -> num.toLong().toString()
                                    else -> num.toString()
                                }
                                Cell(txt, num)
                            }
                        }
                        if (col >= 0) cells[col] = cell
                    }
                    "row" -> rows += SheetRow(rowNum, cells)
                }
            }
            event = p.next()
        }
        return rows
    }
}