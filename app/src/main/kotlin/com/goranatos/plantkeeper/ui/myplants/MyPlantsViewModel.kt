package com.goranatos.plantkeeper.ui.myplants

import androidx.lifecycle.*
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MyPlantsViewModel @Inject constructor(
    private val repository: PlantRepository
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

    suspend fun setPlant(plantId: Int) {
        return withContext(Dispatchers.IO) {
            thePlant = repository.getPlant(plantId)
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

    companion object {
        var isNotExistsTodayOrBeforeDates = false
    }
}