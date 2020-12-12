package com.goranatos.plantskeeper.ui.plantDetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import kotlinx.coroutines.*

class PlantDetailViewModel(
    private val repository: PlantsRepository,
    private val plantId: Int,
) : ViewModel() {

    private val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var mPlantId = plantId

    var isToCreateNewPlant = false

    val thePlant: MutableLiveData<Plant> by lazy {
        MutableLiveData<Plant>()
    }

    suspend fun getPlant(plantId: Int): Plant {
        return withContext(Dispatchers.IO) {
             repository.getPlant(plantId)
        }
    }

    suspend fun deletePlant(){
        return withContext(Dispatchers.IO) {
            repository.deletePlant(thePlant.value!!)
        }
    }

    fun setPlant() {
        if (mPlantId == -1) {
            thePlant.value = Plant(
                0,
                null,
                null,
                "android.resource://com.goranatos.plantskeeper/drawable/ic_plant1",
                null,
                0,
                null,
                null,
            )
            isToCreateNewPlant = false
        } else {
            uiScope.launch {
                thePlant.value = getPlant(mPlantId)

                isToCreateNewPlant = true
            }
        }
    }
}