
package com.learn.robmed.tools

sealed class ScanResult {
    data class Patient(val id: String) : ScanResult()
    data class Medication(val id: String) : ScanResult()
    data class Unknown(val raw: String) : ScanResult()
}

fun parseQr(raw: String): ScanResult {
    val value = raw.trim()

    if (value.isBlank()) {
        return ScanResult.Unknown(value)
    }

    // دعم صيغة التطبيق الآخر: HOSP:P:id أو HOSP:T:id
    val parts = value.split(":", limit = 3)

    if (
        parts.size == 3 &&
        parts[0].equals("HOSP", ignoreCase = true) &&
        parts[2].isNotBlank()
    ) {
        val id = parts[2].trim()

        return when (parts[1].uppercase()) {
            "P" -> ScanResult.Patient(id)
            "T" -> ScanResult.Medication(id)
            else -> ScanResult.Unknown(value)
        }
    }

    // دعم الصيغ القديمة في RobMed
    val separator = value.indexOf(':')

    if (separator < 0) {
        return ScanResult.Unknown(value)
    }

    val type = value.substring(0, separator).trim()
    val id = value.substring(separator + 1).trim()

    if (id.isBlank()) {
        return ScanResult.Unknown(value)
    }

    return when {
        type.equals("patient", ignoreCase = true) ->
            ScanResult.Patient(id)

        type.equals("med", ignoreCase = true) ->
            ScanResult.Medication(id)

        else -> ScanResult.Unknown(value)
    }
}
