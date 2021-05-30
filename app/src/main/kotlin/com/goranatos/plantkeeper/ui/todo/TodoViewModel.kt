package com.goranatos.plantkeeper.ui.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.data.repository.PlantRepository
import com.goranatos.plantkeeper.utilities.TimeHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: PlantRepository,
) :
    ViewModel() {

    lateinit var allPlants: LiveData<List<Plant>>
    lateinit var todayPlantList: LinkedHashSet<Plant>

    var dateWhenIntCode: Int = -1
    fun setIntDateWhen(code: Int) {
        dateWhenIntCode = if (code < DateWhen.TODAY.code || code > DateWhen.WEEK.code) -1
        else code
    }

    private val _dateInMls: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
    val dateInMls: LiveData<Long>
        get() = _dateInMls

    fun setDateInMls(mls: Long) {
        viewModelScope.launch {
            _dateInMls.value = mls
            _dateInMls.value = _dateInMls.value
        }
    }

    fun updateCurrentIntDateWhen() {
        dateInMls.value?.let { setDateInMls(it) }
    }

    private val viewModelJob = Job()
    val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            allPlants = repository.getAllMyPlants().asLiveData()
            todayPlantList = LinkedHashSet<Plant>()
        }
    }

    fun initTodoViewModel() {
        viewModelScope.launch(Dispatchers.IO) {
            allPlants = repository.getAllMyPlants().asLiveData()
            todayPlantList = LinkedHashSet<Plant>()
        }
    }

    fun getMatchPlantList(): List<Plant> {
        val matchList = LinkedHashSet<Plant>()

        allPlants.value?.let {
            if (dateWhenIntCode != -1) {
                todayPlantList.clear()
                for (plant in it) {
                    plant.long_next_watering_date?.let { plant_mls ->
                        if (plant.is_water_need_on == 1) {
                            when (dateWhenIntCode) {
                                DateWhen.TODAY.code -> {
                                    if (TimeHelper.isBeforeOrEqualThanDate(
                                            plant_mls,
                                            dateInMls.value!!
                                        )
                                    ) {
                                        matchList.add(plant)
                                        todayPlantList.add(plant)
                                    }
                                }
                                DateWhen.TOMORROW.code -> {
                                    if (TimeHelper.isInThatDate(plant_mls, dateInMls.value!!)) {
                                        matchList.add(plant)
                                    }
                                }
                                DateWhen.WEEK.code -> {
                                    if (TimeHelper.isBeforeOrEqualThanDate(
                                            plant_mls,
                                            dateInMls.value!!
                                        )
                                    ) {
                                        matchList.add(plant)
                                    }
                                }
                            }
                        }
                    }

                    plant.long_next_fertilizing_date?.let { plant_mls ->
                        if (plant.is_fertilize_need_on == 1) {
                            when (dateWhenIntCode) {
                                DateWhen.TODAY.code -> {
                                    if (TimeHelper.isBeforeOrEqualThanDate(
                                            plant_mls,
                                            dateInMls.value!!
                                        )
                                    ) {
                                        matchList.add(plant)
                                        todayPlantList.add(plant)
                                    }
                                }
                                DateWhen.TOMORROW.code -> {
                                    if (TimeHelper.isInThatDate(plant_mls, dateInMls.value!!)) {
                                        matchList.add(plant)
                                    }
                                }
                                DateWhen.WEEK.code -> {
                                    if (TimeHelper.isBeforeOrEqualThanDate(
                                            plant_mls,
                                            dateInMls.value!!
                                        )
                                    ) {
                                        matchList.add(plant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return matchList.toList()

    }

    private suspend fun updatePlant(plant: Plant) {
        return withContext(Dispatchers.IO) {
            repository.updatePlant(plant)
        }
    }

    private fun updateThePlant(plant: Plant) {
        viewModelScope.launch {
            updatePlant(plant)
        }
    }

    fun completePlantTasks() {
        for (plant in todayPlantList) {
            if (plant.is_fertilizing_hibernate_mode_on == 1 && plant.is_hibernate_mode_on == 1 &&
                plant.long_to_hibernate_from_date != null && plant.long_to_hibernate_till_date != null &&
                TimeHelper.isDateInPlantHibernateRange(
                    TimeHelper.getCurrentTimeInMs(),
                    plant.long_to_hibernate_from_date!!,
                    plant.long_to_hibernate_till_date!!
                )
            ) {

                plant.int_fertilizing_frequency_in_hibernate?.let {
                    plant.long_next_fertilizing_date = TimeHelper.longDatePlusDays(
                        TimeHelper.getCurrentTimeInMs(),
                        plant.int_fertilizing_frequency_in_hibernate!!
                    )
                }
            } else {
                plant.int_fertilizing_frequency_normal?.let {
                    plant.long_next_fertilizing_date = TimeHelper.longDatePlusDays(
                        TimeHelper.getCurrentTimeInMs(),
                        plant.int_fertilizing_frequency_normal!!
                    )
                }
            }


            if (plant.is_watering_hibernate_mode_on == 1 && plant.is_hibernate_mode_on == 1 &&
                plant.long_to_hibernate_from_date != null && plant.long_to_hibernate_till_date != null &&
                TimeHelper.isDateInPlantHibernateRange(
                    TimeHelper.getCurrentTimeInMs(),
                    plant.long_to_hibernate_from_date!!,
                    plant.long_to_hibernate_till_date!!
                )
            )
                plant.int_watering_frequency_in_hibernate?.let {
                    plant.long_next_watering_date = TimeHelper.longDatePlusDays(
                        TimeHelper.getCurrentTimeInMs(),
                        plant.int_watering_frequency_in_hibernate!!
                    )
                }
            else {
                plant.int_watering_frequency_normal?.let {
                    plant.long_next_watering_date = TimeHelper.longDatePlusDays(
                        TimeHelper.getCurrentTimeInMs(),
                        plant.int_watering_frequency_normal!!
                    )
                }
            }
            updateThePlant(plant)
        }
    }

    suspend fun updateWateredData(plantId: Int) {
        val plant: Plant = withContext(Dispatchers.IO) {
            repository.getPlant(plantId)
        }

        if (plant.is_watering_hibernate_mode_on == 1 && plant.is_hibernate_mode_on == 1 &&
            plant.long_to_hibernate_from_date != null && plant.long_to_hibernate_till_date != null &&
            TimeHelper.isDateInPlantHibernateRange(
                TimeHelper.getCurrentTimeInMs(),
                plant.long_to_hibernate_from_date!!,
                plant.long_to_hibernate_till_date!!
            )
        )
            plant.int_watering_frequency_in_hibernate?.let {
                plant.long_next_watering_date = TimeHelper.longDatePlusDays(
                    TimeHelper.getCurrentTimeInMs(),
                    plant.int_watering_frequency_in_hibernate!!
                )
            }
        else {
            plant.int_watering_frequency_normal?.let {
                plant.long_next_watering_date = TimeHelper.longDatePlusDays(
                    TimeHelper.getCurrentTimeInMs(),
                    plant.int_watering_frequency_normal!!
                )
            }
        }
        updateThePlant(plant)
    }

    suspend fun updateFertilizedData(plantId: Int) {
        val plant: Plant = withContext(Dispatchers.IO) {
            repository.getPlant(plantId)
        }

        if (plant.is_fertilizing_hibernate_mode_on == 1 && plant.is_hibernate_mode_on == 1 &&
            plant.long_to_hibernate_from_date != null && plant.long_to_hibernate_till_date != null &&
            TimeHelper.isDateInPlantHibernateRange(
                TimeHelper.getCurrentTimeInMs(),
                plant.long_to_hibernate_from_date!!,
                plant.long_to_hibernate_till_date!!
            )
        ) {
            plant.int_fertilizing_frequency_in_hibernate?.let {
                plant.long_next_fertilizing_date = TimeHelper.longDatePlusDays(
                    TimeHelper.getCurrentTimeInMs(),
                    plant.int_fertilizing_frequency_in_hibernate!!
                )
            }
        } else {
            plant.int_fertilizing_frequency_normal?.let {
                plant.long_next_fertilizing_date = TimeHelper.longDatePlusDays(
                    TimeHelper.getCurrentTimeInMs(),
                    plant.int_fertilizing_frequency_normal!!
                )
            }
        }

        updateThePlant(plant)
    }
}

enum class DateWhen(val code: Int) {
    TODAY(0),
    TOMORROW(1),
    WEEK(2)
}

