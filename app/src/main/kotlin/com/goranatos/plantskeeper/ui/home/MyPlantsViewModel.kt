package com.goranatos.plantskeeper.ui.home

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.preference.PreferenceManager
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import com.goranatos.plantskeeper.receiver.AlarmReceiver
import com.goranatos.plantskeeper.util.SharedPreferencesRepository
import com.goranatos.plantskeeper.util.cancelNotifications
import kotlinx.coroutines.*
import java.util.*

class MyPlantsViewModel(private val repository: PlantsRepository, val app: Application) :
    AndroidViewModel(app) {

    lateinit var allPlants: LiveData<List<Plant>>

    lateinit var thePlant: Plant

    private val viewModelJob = Job()
    val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //create new plant
    var navigateToPlantId = -1

    private val _navigateToThePlant = MutableLiveData<Boolean?>()
    val navigateToThePlant: LiveData<Boolean?>
        get() = _navigateToThePlant

    fun doneNavigating() {
        _navigateToThePlant.value = null
    }

    fun onItemClicked() {
        _navigateToThePlant.value = true
    }

    fun updateNavigateToPlantId(newId: Int) {
        navigateToPlantId = newId
    }

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    private val notifyIntent = Intent(app, AlarmReceiver::class.java)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allPlants = repository.getAllMyPlants().asLiveData()
        }
    }

    suspend fun setPlant(plantId: Int) {
        return withContext(Dispatchers.IO) {
            thePlant = repository.getPlant(plantId)
        }
    }

    fun setNotificationsForPlantList(plantList: List<Plant>?) {

        val isToShowNotifications = sharedPreferences.getBoolean(
            SharedPreferencesRepository.PREF_OPTION_IS_TO_SHOW_NOTIFICATIONS,
            false
        )

        if (isToShowNotifications) {

            val notificationManager =
                ContextCompat.getSystemService(
                    app,
                    NotificationManager::class.java
                ) as NotificationManager
            notificationManager.cancelNotifications()

            val collectionOfDates = mutableListOf<Long>()

            //Добавляю в список из времени и фильтрую на повторы, чтобы потом запускать pendingIntents
            plantList?.forEach { plant ->
                if (plant.is_water_need_on == 1 && plant.long_next_watering_date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = plant.long_next_watering_date!!

                    val prefMinute = sharedPreferences.getInt(
                        SharedPreferencesRepository.PREF_OPTION_NOTIFICATION_TIME,
                        9 * 60 + 30
                    )

                    calendar.set(Calendar.HOUR_OF_DAY, prefMinute / 60)
                    calendar.set(Calendar.MINUTE, prefMinute % 60)
                    calendar.set(Calendar.SECOND, 0)

                    val triggerTime = calendar.timeInMillis

                    if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(triggerTime)
                }

                if (plant.is_fertilize_need_on == 1 && plant.long_next_fertilizing_date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = plant.long_next_fertilizing_date!!

                    val prefMinute = sharedPreferences.getInt(
                        SharedPreferencesRepository.PREF_OPTION_NOTIFICATION_TIME,
                        9 * 60 + 30
                    )

                    calendar.set(Calendar.HOUR_OF_DAY, prefMinute / 60)
                    calendar.set(Calendar.MINUTE, prefMinute % 60)
                    calendar.set(Calendar.SECOND, 0)

                    val triggerTime = calendar.timeInMillis

                    if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(triggerTime)
                }
            }

            collectionOfDates.forEach {
                val notifyPendingIntent = PendingIntent.getBroadcast(
                    getApplication(),
                    it.toInt(),
                    notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.RTC_WAKEUP,
                    it,
                    notifyPendingIntent
                )
            }
        }
    }

    suspend fun deletePlantWithId(plantId: Int) {
        return withContext(Dispatchers.IO) {
            repository.deletePlantWithId(plantId)
        }
    }

    private suspend fun updatePlant() {
        return withContext(Dispatchers.IO) {
            repository.updatePlant(thePlant)
        }
    }

    fun updateThePlant() {
        viewModelScope.launch {
            updatePlant()
        }
    }
}