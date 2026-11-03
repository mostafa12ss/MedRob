package com.learn.test_exel.Backend.data


import org.dhatim.fastexcel.Workbook
import java.io.OutputStream

object ExcelExporter {
    fun write(out: OutputStream, rows: List<DoseSummaryRow>) {
        val wb = Workbook(out, "HospitalApp", "1.0")
        val ws = wb.newWorksheet("الملخص")

        // إضافة الأعمدة الجديدة في رأس الجدول
        listOf("اسم المريض", "رقم السرير", "اسم الدواء", "أخد (مرات)", "ما أخدش (مرات)", "لسه", "موعد الدخول", "موعد الخروج", "التوقيت")
            .forEachIndexed { c, h -> ws.value(0, c, h) }

        rows.forEachIndexed { i, r ->
            val row = i + 1
            ws.value(row, 0, r.patientName)
            ws.value(row, 1, r.bedNumber)
            ws.value(row, 2, r.drugName)
            ws.value(row, 3, r.takenCount)
            ws.value(row, 4, r.missedCount)
            ws.value(row, 5, r.pendingCount)

            // يمكنك تعبئة القيم الخاصة بالدخول والخروج والتوقيت هنا بحسب البيانات المتوفرة لديك في الـ Row أو Model
            ws.value(row, 6, "-") // موعد الدخول (استبدلها بالمتغير الخاص بك إذا وجد)
            ws.value(row, 7, "-") // موعد الخروج
            ws.value(row, 8, "-") // التوقيت
        }
        wb.finish()
    }

    fun writeTemplate(out: OutputStream) {
        val wb = Workbook(out, "HospitalApp", "1.0")
        val ws = wb.newWorksheet("العلاجات")

        // تحديث نموذج القالب ليتوافق مع الأعمدة المطلوبة
        listOf("اسم المريض", "رقم السرير", "اسم الدواء", "أخد (مرات)", "ما أخدش (مرات)", "لسه", "موعد الدخول", "موعد الخروج", "التوقيت")
            .forEachIndexed { c, h -> ws.value(0, c, h) }

        listOf("ممدوح", "2", "بينترست", 0, 0, 4, "08:00 ص", "02:00 م", "صباحي ومسائي")
            .forEachIndexed { c, v ->
                if (v is Int) ws.value(1, c, v.toLong())
                else ws.value(1, c, v.toString())
            }
        wb.finish()
    }
}