package com.goranatos.plantkeeper.ui.myplants

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.data.repository.PlantRepository
import com.goranatos.plantkeeper.receiver.AlarmReceiver
import com.goranatos.plantkeeper.utilities.SharedPreferencesRepositoryConstants
import com.goranatos.plantkeeper.utilities.cancelNotifications
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MyPlantsViewModel @Inject constructor(
    private val repository: PlantRepository,
    @ApplicationContext private val context: Context
) : ViewModel(), LifecycleObserver {

    lateinit var allPlants: LiveData<List<Plant>>

    fun initMyPlantsViewModel() {
        viewModelScope.launch(Dispatchers.IO) {
            allPlants = repository.getAllMyPlants().asLiveData()
        }
        isToUpdateRecycleView.value
    }

    private val _isToUpdateRecycleView = MutableLiveData<Boolean?>()
    val isToUpdateRecycleView: LiveData<Boolean?>
        get() = _isToUpdateRecycleView

    fun updateRecycleView() {
        _isToUpdateRecycleView.value = true
    }

    lateinit var thePlant: Plant

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

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private val notifyIntent = Intent(context, AlarmReceiver::class.java)


    suspend fun setPlant(plantId: Int) {
        return withContext(Dispatchers.IO) {
            thePlant = repository.getPlant(plantId)
        }
    }

    fun setNotificationsForPlantList(plantList: List<Plant>?) {

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

            //Добавляю в список из времени и фильтрую на повторы, чтобы потом запускать pendingIntents
            plantList?.forEach { plant ->
                if (plant.is_water_need_on == 1 && plant.long_next_watering_date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = plant.long_next_watering_date!!

                    val prefMinute = sharedPreferences.getInt(
                        SharedPreferencesRepositoryConstants.PREF_OPTION_NOTIFICATION_TIME,
                        9 * 60 + 30
                    )

                    calendar.set(Calendar.HOUR_OF_DAY, prefMinute / 60)
                    calendar.set(Calendar.MINUTE, prefMinute % 60)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val triggerTime = calendar.timeInMillis

                    if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(triggerTime)
                }

                if (plant.is_fertilize_need_on == 1 && plant.long_next_fertilizing_date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = plant.long_next_fertilizing_date!!

                    val prefMinute = sharedPreferences.getInt(
                        SharedPreferencesRepositoryConstants.PREF_OPTION_NOTIFICATION_TIME,
                        9 * 60 + 30
                    )

                    calendar.set(Calendar.HOUR_OF_DAY, prefMinute / 60)
                    calendar.set(Calendar.MINUTE, prefMinute % 60)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val triggerTime = calendar.timeInMillis

                    if (!collectionOfDates.contains(triggerTime)) collectionOfDates.add(triggerTime)
                }
            }

            collectionOfDates.forEach {
                val notifyPendingIntent = PendingIntent.getBroadcast(
                    context,
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
        } else {
            notificationManager.cancelNotifications()
        }
    }

    fun deleteThePlant() {
        viewModelScope.launch {
            deletePlantWithId(thePlant.int_id)
        }
    }

    private suspend fun deletePlantWithId(plantId: Int) {
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

    override fun onCleared() {
        super.onCleared()
        setNotificationsForPlantList(allPlants.value)
    }
}