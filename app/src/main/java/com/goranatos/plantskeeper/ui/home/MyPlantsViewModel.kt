package com.goranatos.plantskeeper.ui.home

import androidx.lifecycle.*
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import com.goranatos.plantskeeper.internal.lazyDeferred

class MyPlantsViewModel(private val repository: PlantsRepository) : ViewModel() {

    val allPlants by lazyDeferred {
        repository.getAllMyPlants()
    }

}

