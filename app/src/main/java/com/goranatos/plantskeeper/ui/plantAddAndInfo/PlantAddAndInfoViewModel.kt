package com.goranatos.plantskeeper.ui.plantAddAndInfo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goranatos.plantskeeper.data.repository.PlantsRepository

class PlantAddAndInfoViewModel(private val repository: PlantsRepository, application: Application) :
    AndroidViewModel(application) {

}
