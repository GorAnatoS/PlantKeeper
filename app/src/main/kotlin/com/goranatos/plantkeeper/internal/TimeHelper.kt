package com.goranatos.plantkeeper.internal

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

        fun getNextDayDate(): Long {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            return calendar.timeInMillis
        }

        fun minutesFromMidnightToHourlyTime(persistedMinutesFromMidnight: Int): CharSequence {
            return "${persistedMinutesFromMidnight / 60}:${persistedMinutesFromMidnight % 60}"
        }

        fun getDaysTillEventNotification(dateFrom: Long, dateTill: Long): Int {
            val calendarEventDate = Calendar.getInstance()
            calendarEventDate.timeInMillis = dateTill
            calendarEventDate.set(Calendar.HOUR_OF_DAY, 0)
            calendarEventDate.set(Calendar.MINUTE, 0)
            calendarEventDate.set(Calendar.SECOND, 0)
            calendarEventDate.set(Calendar.MILLISECOND, 0)

            val currentTimeCalendar = Calendar.getInstance()
            currentTimeCalendar.timeInMillis = dateFrom
            currentTimeCalendar.set(Calendar.HOUR_OF_DAY, 0)
            currentTimeCalendar.set(Calendar.MINUTE, 0)
            currentTimeCalendar.set(Calendar.SECOND, 0)
            currentTimeCalendar.set(Calendar.MILLISECOND, 0)

            val result = calendarEventDate.timeInMillis - currentTimeCalendar.timeInMillis

            return TimeUnit.MILLISECONDS.toDays(result).toInt()
        }

        fun isDateInPlantHibernateRange(
            dateInMs: Long,
            rangeDateFromMs: Long,
            rangeDateTillMs: Long
        ): Boolean {

            val calendarDate = Calendar.getInstance()
            calendarDate.timeInMillis = dateInMs

            val calendarFrom = Calendar.getInstance()
            calendarFrom.timeInMillis = rangeDateFromMs

            val calendarTill = Calendar.getInstance()
            calendarTill.timeInMillis = rangeDateTillMs

            calendarFrom.set(Calendar.YEAR, calendarDate.get(Calendar.YEAR))

            if (calendarFrom.before(calendarTill)) {
                calendarTill.set(Calendar.YEAR, calendarDate.get(Calendar.YEAR))
            } else {
                calendarTill.set(Calendar.YEAR, calendarDate.get(Calendar.YEAR) + 1)
            }

            calendarFrom.set(Calendar.DAY_OF_YEAR, calendarFrom.get(Calendar.DAY_OF_YEAR) - 1)
            calendarTill.set(Calendar.DAY_OF_YEAR, calendarTill.get(Calendar.DAY_OF_YEAR) + 1)

            return calendarDate.before(calendarTill) && calendarDate.after(calendarFrom)
        }

        fun longDatePlusDays(date: Long, plusDays: Int): Long {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date
            calendar.add(Calendar.DAY_OF_YEAR, plusDays)
            return calendar.timeInMillis
        }
    }
}
