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

    var isToCreateNewPlant = false

    val thePlant: MutableLiveData<Plant> by lazy {
        MutableLiveData<Plant>()
    }

    suspend fun getPlant(plantId: Int): Plant {
        return withContext(Dispatchers.IO) {
            repository.getPlant(plantId)
        }
    }

    suspend fun deletePlant() {
        return withContext(Dispatchers.IO) {
            repository.deletePlant(thePlant.value!!)
        }
    }

    suspend fun insertPlant() {
        return withContext(Dispatchers.IO) {
            repository.insertPlant(thePlant.value!!)
        }
    }

    suspend fun updatePlant() {
        return withContext(Dispatchers.IO) {
            repository.updatePlant(thePlant.value!!)
        }
    }


    fun setPlant() {
        if (plantId == -1) {
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
            isToCreateNewPlant = true
        } else {
            uiScope.launch {
                thePlant.value = getPlant(plantId)

                isToCreateNewPlant = false
            }
        }
    }

    fun onInsertOrUpdatePlant() {
        uiScope.launch {
            if (isToCreateNewPlant) {
                insertPlant()
                /* Snackbar.make(requireView(), getString(R.string.added), Snackbar.LENGTH_SHORT)
                 .show()*/
            } else {
                updatePlant()
                /*Snackbar.make(requireView(), getString(R.string.changed), Snackbar.LENGTH_SHORT)
                 .show()*/
            }
        }
    }
}