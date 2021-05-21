package com.goranatos.plantkeeper.ui.plantinfo

import android.content.Context
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlantInfoViewModel @Inject constructor(
    private val repository: PlantRepository,
    @ApplicationContext private val context: Context
) : ViewModel(), LifecycleObserver {

    fun initPlantInfoViewModel() {
    }

    lateinit var thePlant: Plant

    //create new plant
    var navigateToPlantId = -1

    fun initPlantInfoViewModel(plantId: Int) {
        viewModelScope.launch {
            setPlant(plantId)
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

    private val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)


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