package com.goranatos.plantskeeper.ui.home

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import com.goranatos.plantskeeper.internal.TimeHelper
import com.goranatos.plantskeeper.receiver.AlarmReceiver
import com.goranatos.plantskeeper.util.cancelNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class MyPlantsViewModel(private val repository: PlantsRepository, val app: Application) :
    AndroidViewModel(app) {

    val job = Job()

    lateinit var allPlants: LiveData<List<Plant>>

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

    fun setNotificationsForPlantList(plantList: List<Plant>?) {

        val notificationManager =
            ContextCompat.getSystemService(
                app,
                NotificationManager::class.java
            ) as NotificationManager
        notificationManager.cancelNotifications()

        plantList?.forEach { plant ->
            if (plant.is_water_need_on == 1) {

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = plant.long_to_water_from_date!!

                val r = TimeHelper.getFormattedDateTimeString( plant.long_to_water_from_date!!)

                val prefMinute = sharedPreferences.getInt("notification_time", 9 * 60 + 30)

                calendar.set(Calendar.HOUR_OF_DAY, prefMinute / 60)
                calendar.set(Calendar.MINUTE, prefMinute % 60)
                calendar.set(Calendar.SECOND, 0)

                var triggerTime = calendar.timeInMillis

                val notifyPendingIntent = PendingIntent.getBroadcast(
                    getApplication(),
                    plant.int_id,
                    notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    notifyPendingIntent
                )
            }
        }
    }
}