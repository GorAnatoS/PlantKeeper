package com.goranatos.plantskeeper.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyPlantsViewModel(private val repository: PlantsRepository) : ViewModel() {

    lateinit var allPlants: LiveData<List<Plant>>

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allPlants = repository.getAllMyPlants().asLiveData()
        }
    }

    suspend fun updatePlant(plant: Plant) {
        repository.updatePlant(plant)
    }

    suspend fun deletePlant(plant: Plant) {
        repository.deletePlant(plant)
    }

    suspend fun insertPlant(plant: Plant) {
        repository.insertPlant(plant)
    }

    suspend fun getPlant(id: Int): LiveData<Plant> {
        return repository.getPlant(id)
    }


}

