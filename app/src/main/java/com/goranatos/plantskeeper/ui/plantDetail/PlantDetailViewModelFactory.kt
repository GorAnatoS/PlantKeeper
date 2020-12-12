package com.goranatos.plantskeeper.ui.plantDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.goranatos.plantskeeper.data.repository.PlantsRepository

class PlantDetailViewModelFactory(private val repository: PlantsRepository, val plantId: Int) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantDetailViewModel(repository, plantId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}