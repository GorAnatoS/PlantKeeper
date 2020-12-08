package com.goranatos.plantskeeper.internal

import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*


/**
 Класс для работы с временными функциями
 */
class Time {
    companion object {
        fun formattedDateStringToFormattedDateLong(string: String): Long {
            val date = Calendar.getInstance().time
            val formatter =
                SimpleDateFormat.getDateInstance() //or use getDateInstance()
            var formatedDateString = formatter.format(date)
            var formatedDateLong = formatter.parse(formatedDateString).time
            return formatedDateLong
        }

        fun getFormattedDateString(): String {
            val date = Calendar.getInstance().time
            val formatter =
                SimpleDateFormat.getDateInstance()
            var formatedDateString = formatter.format(date)
            return formatedDateString
        }

        fun getFormattedDateString(ms: Long): String {
            val date = Calendar.getInstance().time
            val formatter =
                SimpleDateFormat.getDateInstance()
            var formatedDateString = formatter.format(ms)

            return formatedDateString
        }


    }
}