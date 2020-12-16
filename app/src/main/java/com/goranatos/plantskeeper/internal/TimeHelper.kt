package com.goranatos.plantskeeper.internal

import java.text.SimpleDateFormat
import java.util.*


/**
 Класс для работы с временными функциями
 */
class TimeHelper {
    companion object {
        private val date: Date = Calendar.getInstance().time

        fun formattedDateStringToFormattedDateLong(string: String): Long {
            val formatter =
                SimpleDateFormat.getDateInstance() //or use getDateInstance()
            val formatedDateString = formatter.format(date)
            return formatter.parse(formatedDateString).time
        }

        fun getFormattedDateString(): String {
            val formatter =
                SimpleDateFormat.getDateInstance()
            return formatter.format(date)
        }

        fun getFormattedDateString(ms: Long): String {
            val formatter =
                SimpleDateFormat("MMM, d")//.getDateInstance()
            return formatter.format(ms)
        }

        fun getCurrentTimeInMs(): Long = date.time

    }
}