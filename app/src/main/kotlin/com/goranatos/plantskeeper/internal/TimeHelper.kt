package com.goranatos.plantskeeper.internal

import com.goranatos.plantskeeper.data.entity.Plant
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


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

        fun getFormattedDateTimeString(ms: Long): String {
            val formatter =
                SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
            return formatter.format(ms)
        }

        fun getCurrentTimeInMs(): Long = date.time

        fun minutesFromMidnightToHourlyTime(persistedMinutesFromMidnight: Int): CharSequence? {
            return "${persistedMinutesFromMidnight / 60}:${persistedMinutesFromMidnight % 60}"
        }

        fun getDaysTillWateringNotification(plant: Plant): Int {
            if (plant.is_water_need_on == 1){

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = plant.long_to_water_from_date!!

                val currentTime = System.currentTimeMillis()

                val result = calendar.timeInMillis - currentTime

                return TimeUnit.MILLISECONDS.toDays(result).toInt()

            } else {
                return -1
            }
        }
    }
}