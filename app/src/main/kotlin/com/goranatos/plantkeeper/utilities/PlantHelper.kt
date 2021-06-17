package com.goranatos.plantkeeper.utilities

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.receiver.AlarmReceiver
import com.goranatos.plantkeeper.ui.myplants.MyPlantsViewModel
import java.util.*


/**
 * Created by qsufff on 5/21/2021.
 */
class PlantHelper {
    companion object {

        fun isWaterTodayNeeded(plant: Plant): Boolean {
            return plant.is_water_need_on == 1 && TimeHelper.getDaysTillEventNotification(
                System.currentTimeMillis(),
                plant.long_next_watering_date!!
            ) <= 0
        }

        fun isFertilizeTodayNeeded(plant: Plant): Boolean {
            return plant.is_fertilize_need_on == 1 && TimeHelper.getDaysTillEventNotification(
                System.currentTimeMillis(),
                plant.long_next_fertilizing_date!!
            ) <= 0
        }

        //Set Notifications for Plants
        fun setNotificationsForPlantList(plantList: List<Plant>?, context: Context) {

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)

            val notifyIntent = Intent(context, AlarmReceiver::class.java)

            val isToShowNotifications = sharedPreferences.getBoolean(
                SharedPreferencesRepositoryConstants.PREF_OPTION_IS_TO_SHOW_NOTIFICATIONS,
                false
            )

            val notificationManager =
                ContextCompat.getSystemService(
                    context,
                    NotificationManager::class.java
                ) as NotificationManager

            if (isToShowNotifications) {
                val collectionOfDates = mutableListOf<Long>()

                val prefMinute = sharedPreferences.getInt(
                    SharedPreferencesRepositoryConstants.PREF_OPTION_NOTIFICATION_TIME,
                    9 * 60 + 30
                )

                val calendarToday = Calendar.getInstance().apply {
                    timeInMillis = TimeHelper.getCurrentTimeInMs()
                    set(Calendar.HOUR_OF_DAY, prefMinute / 60)
                    set(Calendar.MINUTE, prefMinute % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val calendarEndOfToday = Calendar.getInstance().apply {
                    timeInMillis = TimeHelper.getCurrentTimeInMs()
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 99)
                    set(Calendar.MILLISECOND, 999)
                }


                //Добавляю в список из времени и фильтрую на повторы, чтобы потом запускать pendingIntents
                plantList?.forEach { plant ->
                    if (plant.is_water_need_on == 1 && plant.long_next_watering_date != null) {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = plant.long_next_watering_date!!

                        calendar.set(Calendar.HOUR_OF_DAY, prefMinute / 60)
                        calendar.set(Calendar.MINUTE, prefMinute % 60)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        val triggerTime = calendar.timeInMillis

                        if (triggerTime > calendarEndOfToday.timeInMillis) {
                            if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(
                                triggerTime
                            )
                        } else {
                            if (triggerTime > TimeHelper.getCurrentTimeInMs() && !MyPlantsViewModel.isNotExistsTodayOrBeforeDates) {
                                if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(
                                    triggerTime
                                )
                                MyPlantsViewModel.isNotExistsTodayOrBeforeDates = true
                            }
                        }
                    }

                    if (plant.is_fertilize_need_on == 1 && plant.long_next_fertilizing_date != null) {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = plant.long_next_fertilizing_date!!

                        calendar.set(Calendar.HOUR_OF_DAY, prefMinute / 60)
                        calendar.set(Calendar.MINUTE, prefMinute % 60)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        val triggerTime = calendar.timeInMillis

                        if (triggerTime > calendarEndOfToday.timeInMillis) {
                            if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(
                                triggerTime
                            )
                        } else {
                            if (triggerTime > TimeHelper.getCurrentTimeInMs() && !MyPlantsViewModel.isNotExistsTodayOrBeforeDates) {
                                if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(
                                    triggerTime
                                )
                                MyPlantsViewModel.isNotExistsTodayOrBeforeDates = true
                            }
                        }
                    }
                }

                collectionOfDates.forEach {
                    notifyIntent.putExtra(NOTIFICATION_EXTRA_LONG_REQUEST_CODE, it)
                    val notifyPendingIntent = PendingIntent.getBroadcast(
                        context,
                        it.toInt(),
                        notifyIntent,
                        PendingIntent.FLAG_ONE_SHOT
                    )

                    AlarmManagerCompat.setExactAndAllowWhileIdle(
                        alarmManager,
                        AlarmManager.RTC_WAKEUP,
                        it,
                        notifyPendingIntent
                    )
                }
            } else {
                notificationManager.cancelNotifications()
            }
        }

        //Set Notification for the plant
        fun setNotificationsForPlant(plant: Plant, context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)

            val notifyIntent = Intent(context, AlarmReceiver::class.java)

            val isToShowNotifications = sharedPreferences.getBoolean(
                SharedPreferencesRepositoryConstants.PREF_OPTION_IS_TO_SHOW_NOTIFICATIONS,
                false
            )

            val notificationManager =
                ContextCompat.getSystemService(
                    context,
                    NotificationManager::class.java
                ) as NotificationManager

            if (isToShowNotifications) {
                val collectionOfDates = mutableListOf<Long>()

                val prefMinute = sharedPreferences.getInt(
                    SharedPreferencesRepositoryConstants.PREF_OPTION_NOTIFICATION_TIME,
                    9 * 60 + 30
                )

                val calendarToday = Calendar.getInstance().apply {
                    timeInMillis = TimeHelper.getCurrentTimeInMs()
                    set(Calendar.HOUR_OF_DAY, prefMinute / 60)
                    set(Calendar.MINUTE, prefMinute % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val calendarEndOfToday = Calendar.getInstance().apply {
                    timeInMillis = TimeHelper.getCurrentTimeInMs()
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 99)
                    set(Calendar.MILLISECOND, 999)
                }


                //Добавляю в список из времени и фильтрую на повторы, чтобы потом запускать pendingIntents
                if (plant.is_water_need_on == 1 && plant.long_next_watering_date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = plant.long_next_watering_date!!

                    calendar.set(Calendar.HOUR_OF_DAY, prefMinute / 60)
                    calendar.set(Calendar.MINUTE, prefMinute % 60)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val triggerTime = calendar.timeInMillis

                    if (triggerTime > calendarEndOfToday.timeInMillis) {
                        if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(
                            triggerTime
                        )
                    } else {
                        if (triggerTime > TimeHelper.getCurrentTimeInMs() && !MyPlantsViewModel.isNotExistsTodayOrBeforeDates) {
                            if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(
                                triggerTime
                            )
                            MyPlantsViewModel.isNotExistsTodayOrBeforeDates = true
                        }
                    }
                }

                if (plant.is_fertilize_need_on == 1 && plant.long_next_fertilizing_date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = plant.long_next_fertilizing_date!!

                    calendar.set(Calendar.HOUR_OF_DAY, prefMinute / 60)
                    calendar.set(Calendar.MINUTE, prefMinute % 60)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val triggerTime = calendar.timeInMillis

                    if (triggerTime > calendarEndOfToday.timeInMillis) {
                        if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(
                            triggerTime
                        )
                    } else {
                        if (triggerTime > TimeHelper.getCurrentTimeInMs() && !MyPlantsViewModel.isNotExistsTodayOrBeforeDates) {
                            if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(
                                triggerTime
                            )
                            MyPlantsViewModel.isNotExistsTodayOrBeforeDates = true
                        }
                    }
                }


                collectionOfDates.forEach {
                    notifyIntent.putExtra(NOTIFICATION_EXTRA_LONG_REQUEST_CODE, it)
                    val notifyPendingIntent = PendingIntent.getBroadcast(
                        context,
                        it.toInt(),
                        notifyIntent,
                        PendingIntent.FLAG_ONE_SHOT
                    )

                    AlarmManagerCompat.setExactAndAllowWhileIdle(
                        alarmManager,
                        AlarmManager.RTC_WAKEUP,
                        it,
                        notifyPendingIntent
                    )
                }
            } else {
                notificationManager.cancelNotifications()
            }
        }

        fun deletePlantItemFromDB(
            context: Context,
            actionOnDelete: () -> Unit,
            view: View
        ) {
            MaterialAlertDialogBuilder(context)
                .setTitle(context.resources.getString(R.string.delete_plant_from_db))
                .setMessage(context.resources.getString(R.string.are_you_sure_to_delete_the_plant_from_db))
                .setNeutralButton(context.resources.getString(R.string.cancel)) { _, _ ->

                }
                .setPositiveButton(
                    context.resources.getString(R.string.delete_item),
                    DialogInterface.OnClickListener { dialog, which ->
                        actionOnDelete()

                        //Do nothing here because we override this button later to change the close behaviour.
                        //However, we still need this because on older versions of Android unless we
                        //pass a handler the button doesn't get instantiated

                        Snackbar.make(
                            view,
                            context.getString(R.string.deleted),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    })
                .show()
        }


    }
}