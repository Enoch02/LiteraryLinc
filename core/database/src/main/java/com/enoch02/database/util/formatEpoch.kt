package com.enoch02.database.util

import android.icu.text.SimpleDateFormat
import android.os.Build
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun formatEpochDate(date: Long?): String {
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