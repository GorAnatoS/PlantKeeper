package com.goranatos.plantkeeper.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.goranatos.plantkeeper.data.repository.PlantsRepository

class MyPlantsViewModelFactory(
    private val repository: PlantsRepository,
    private val application: Application
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPlantsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyPlantsViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}