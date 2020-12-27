package com.goranatos.plantskeeper.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import kotlinx.coroutines.*

class MyPlantsViewModel(private val repository: PlantsRepository, application: Application) :
    AndroidViewModel(application) {

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

    fun updateNavigateToPlantId(newId : Int){
        navigateToPlantId = newId
    }
    init {
        viewModelScope.launch(Dispatchers.IO) {
            allPlants = repository.getAllMyPlants().asLiveData()
        }
    }
}