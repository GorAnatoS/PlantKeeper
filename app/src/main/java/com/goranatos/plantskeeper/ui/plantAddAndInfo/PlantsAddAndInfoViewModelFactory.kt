package com.goranatos.plantskeeper.ui.plantAddAndInfo

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.goranatos.plantskeeper.data.repository.PlantsRepository

class PlantsAddAndInfoViewModelFactory(private val repository: PlantsRepository, val application: Application) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantAddAndInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantAddAndInfoViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}