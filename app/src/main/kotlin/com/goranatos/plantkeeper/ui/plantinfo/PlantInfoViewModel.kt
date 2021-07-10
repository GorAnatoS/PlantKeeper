package com.goranatos.plantkeeper.ui.plantinfo

import androidx.lifecycle.*
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlantInfoViewModel @Inject constructor(
    private val repository: PlantRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), LifecycleObserver {

    private var plantId: Int? = -1

    init {
        plantId = savedStateHandle["plantId"]
    }

    lateinit var thePlant: Plant

    //create new plant
    var navigateToPlantId = -1

    fun initPlantInfoViewModel() {
        viewModelScope.launch {
            plantId?.let { setPlant(it) }
        }
    }

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

    private suspend fun deletePlant() {
        return withContext(Dispatchers.IO) {
            repository.deletePlantWithId(thePlant.int_id)
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

    fun deleteThePlant() {
        viewModelScope.launch {
            deletePlant()
        }
    }
}