package com.enoch02.database.util

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private const val TAG = "TimeFormatters"

fun formatEpochAsString(date: Long?): String {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && date != null -> {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            val dateObj = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(date),
                ZoneId.systemDefault()
            )
            formatter.format(dateObj)
        }

        Build.VERSION.SDK_INT <= Build.VERSION_CODES.O && date != null -> {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.ROOT)
            formatter.format(Date(date))
        }

        else -> {
            ""
        }
    }
}

fun getEpochFromString(dateString: String): Long? {
    try {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                val localDate = LocalDate.parse(dateString, formatter)
                // Convert LocalDate to LocalDateTime by setting time to start of day
                val localDateTime = localDate.atStartOfDay()
                val instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant()

                return instant.toEpochMilli()
            }

            Build.VERSION.SDK_INT <= Build.VERSION_CODES.O -> {
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.ROOT)
                val date = formatter.parse(dateString)

                return date?.time
            }

            else -> {
                return null
            }
        }

    } catch (e: Exception) {
        Log.e(TAG, "getEpochFromString: ${e.message}")
        return null
    }
}