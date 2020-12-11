package com.goranatos.plantskeeper.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import com.goranatos.plantskeeper.internal.lazyDeferred
import kotlinx.coroutines.*

class MyPlantsViewModel(private val repository: PlantsRepository) :
    ViewModel() {


    private val viewModelJob = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    lateinit var allPlants: LiveData<List<Plant>>

    private val _navigateToThePlant = MutableLiveData<Boolean?>()
    val navigateToThePlant: LiveData<Boolean?>
        get() = _navigateToThePlant


    fun doneNavigating() {
        _navigateToThePlant.value = null
    }

    fun onItemClicked(){
        _navigateToThePlant.value = true
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allPlants = repository.getAllMyPlants().asLiveData()
        }
    }

    suspend fun updatePlant(plant: Plant) {
        withContext(Dispatchers.IO) {
            repository.updatePlant(plant)
        }
    }

    suspend fun deletePlant(plant: Plant) {
        withContext(Dispatchers.IO) {
            repository.deletePlant(plant)
        }
    }

    suspend fun getPlant(id: Int): LiveData<Plant> {
        return withContext(Dispatchers.IO) {
            repository.getPlant(id)
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}