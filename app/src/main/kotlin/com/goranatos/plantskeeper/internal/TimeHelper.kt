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

        fun getNextWateringDate(): Long {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            return calendar.timeInMillis
        }

        fun minutesFromMidnightToHourlyTime(persistedMinutesFromMidnight: Int): CharSequence {
            return "${persistedMinutesFromMidnight / 60}:${persistedMinutesFromMidnight % 60}"
        }

        fun getDaysTillWateringNotification(plant: Plant): Int {
            if (plant.is_water_need_on == 1) {

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = plant.long_next_watering_date!!
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

        // TODO: 1/14/2021  
        fun isDateInPlantHibernateRange(dateInMs: Long, plant: Plant): Boolean {
            if (plant.is_hibernate_mode_on == 1 && plant.long_to_hibernate_till_date != null && plant.long_to_hibernate_till_date != null && plant.long_next_watering_date != null) {
                val date = Calendar.getInstance()
                date.timeInMillis = dateInMs

                val calendarTill = Calendar.getInstance()
                calendarTill.timeInMillis = plant.long_to_hibernate_till_date!!

                val calendarFrom = Calendar.getInstance()
                calendarFrom.timeInMillis = plant.long_to_hibernate_from_date!!

                calendarFrom.set(Calendar.YEAR, date.get(Calendar.YEAR))

                if (calendarFrom.before(calendarTill)) {
                    calendarTill.set(Calendar.YEAR, date.get(Calendar.YEAR))
                } else {
                    calendarTill.set(Calendar.YEAR, date.get(Calendar.YEAR) + 1)
                }

                calendarFrom.set(Calendar.DAY_OF_YEAR, calendarFrom.get(Calendar.DAY_OF_YEAR)-1)
                calendarTill.set(Calendar.DAY_OF_YEAR, calendarTill.get(Calendar.DAY_OF_YEAR)+1)

                return date.before(calendarTill) && date.after(calendarFrom)

            } else return false
        }

        fun longDatePlusDays(date: Long, plusDays: Int): Long {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date
            calendar.add(Calendar.DAY_OF_YEAR, plusDays)
            return calendar.timeInMillis
        }
    }
}
