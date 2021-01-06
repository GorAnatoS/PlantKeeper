package com.goranatos.plantskeeper.internal

import com.goranatos.plantskeeper.data.entity.Plant
import java.text.SimpleDateFormat
import java.time.Duration
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

        fun getNextWateringDate(): Long {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            return calendar.timeInMillis
        }

        fun minutesFromMidnightToHourlyTime(persistedMinutesFromMidnight: Int): CharSequence? {
            return "${persistedMinutesFromMidnight / 60}:${persistedMinutesFromMidnight % 60}"
        }

        fun getDaysTillWateringNotification(plant: Plant): Int {
            if (plant.is_water_need_on == 1){

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = plant.long_to_water_from_date!!
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val currentTimeCalendar = Calendar.getInstance()
                val currentTime = System.currentTimeMillis()
                currentTimeCalendar.timeInMillis = currentTime
                currentTimeCalendar.set(Calendar.HOUR_OF_DAY, 0)
                currentTimeCalendar.set(Calendar.MINUTE, 0)
                currentTimeCalendar.set(Calendar.SECOND, 0)
                currentTimeCalendar.set(Calendar.MILLISECOND, 0)

                val result = calendar.timeInMillis - currentTimeCalendar.timeInMillis

                return TimeUnit.MILLISECONDS.toDays(result).toInt()

            } else {
                return -1
            }
        }

        fun longDatePlusDays(currentDate: Long, plusDays: Int): Long{
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, plusDays)
            return calendar.timeInMillis
        }
    }
}