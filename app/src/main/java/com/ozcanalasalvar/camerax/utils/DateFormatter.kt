package com.ozcanalasalvar.camerax.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertBackendDateToUnderstandableHumanTime(date: String): String {
        val parsedDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
        return parsedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    fun getCurrentDate(pattern: String): String {
        val dateFormatter = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormatter.format(System.currentTimeMillis())
    }

}

object DateFormats {
    const val backend = "yyyy-MM-dd'T'HH:mm:ss"
    const val yearMonthDay = "yyyy-MM-dd"
}