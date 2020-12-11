package com.goranatos.plantskeeper.ui.home

import androidx.lifecycle.*
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyPlantsViewModel(private val repository: PlantsRepository) :
    ViewModel() {

    lateinit var allPlants: LiveData<List<Plant>>

    var thePlantId = -1

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