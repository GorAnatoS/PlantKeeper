package com.goranatos.plantskeeper.ui.home

import androidx.lifecycle.*
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import kotlinx.coroutines.*

class MyPlantsViewModel(private val repository: PlantsRepository) :
    ViewModel() {

    val job = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + job)

    lateinit var allPlants: LiveData<List<Plant>>

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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allPlants = repository.getAllMyPlants().asLiveData()
        }
    }

}