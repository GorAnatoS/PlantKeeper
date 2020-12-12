package com.goranatos.plantskeeper.ui.plantDetail

import android.net.Uri
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import kotlinx.coroutines.*

class PlantDetailViewModel(
    private val repository: PlantsRepository,
    private val plantId: Int,
) : ViewModel() {

    companion object {
        //Camera request code
        const val REQUEST_IMAGE_CAPTURE = 631

        //selectPicture request code
        const val REQUEST_CHOOSE_FROM_GALLERY = 632

        lateinit var uriDestination: Uri
        lateinit var uriCapturedImage: Uri

        var formattedDateLong: Long = 0
    }

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


    fun updatePlantName(name: String) {
        thePlant.value?.name = name
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

    //OPTIONS MENU START



    //OPTIONS MENU END




    /**
     * If plant.is_hibernate_on == 1 then is checked, 0 - unchecked
     */
    val isSwitchHibernateBtnVisible = Transformations.map(thePlant) {
        it.is_hibernate_on != 0
    }

    /**
     * If isSwitchHibernateBtnVisible returns View.VISIBLE for hibernateGroup.visibility
     */
    val hibernateGroupVisible = Transformations.map(isSwitchHibernateBtnVisible) {
        if (it) View.VISIBLE
        else View.GONE
    }

    /**
     * Если у растения поле water_need не установлено или пусто, то значит кнопка отжата. Иначе - наоборот
     */
    val isToggleToWaterChecked = Transformations.map(thePlant) {
        !it.water_need.isNullOrEmpty()
    }

    /**
     * Если у растения поле isToggleToWaterChecked истино, то показываем tvToWaterFromDateVal, иначе - нет
     */
    val tvToWaterFromDateValVisible = Transformations.map(isToggleToWaterChecked) {
        if (it) View.VISIBLE
        else View.GONE
    }



}