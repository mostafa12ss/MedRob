package com.learn.test_exel.Backend.data


import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

enum class QrKind(val code: String) { PATIENT("P"), TREATMENT("T") }

data class QrPayload(val kind: QrKind, val id: String)

object QrCodec {
    private const val PREFIX = "HOSP"

    fun encode(kind: QrKind, id: String) = "$PREFIX:${kind.code}:$id"

    /** بيرجّع null لو الكود مش تابع للتطبيق */
    fun parse(raw: String?): QrPayload? {
        val parts = raw?.trim()?.split(":") ?: return null
        if (parts.size != 3 || parts[0] != PREFIX || parts[2].isBlank()) return null
        val kind = QrKind.entries.firstOrNull { it.code == parts[1] } ?: return null
        return QrPayload(kind, parts[2])
    }

    fun bitmap(text: String, size: Int = 800): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 2
        )
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] = if (matrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
    }
}