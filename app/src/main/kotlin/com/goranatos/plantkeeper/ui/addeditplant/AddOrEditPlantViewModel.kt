package com.goranatos.plantkeeper.ui.addeditplant

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.goranatos.plantkeeper.BuildConfig
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class AddOrEditPlantViewModel @Inject constructor(
    private val repository: PlantRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var plantId: Int? = -1

    init {
        plantId = savedStateHandle["plantId"]
    }

    fun initPlantDetailViewModel() {
        plantId = savedStateHandle["plantId"]
        setPlant()
    }

    companion object {
        //Camera request code
        const val REQUEST_IMAGE_CAPTURE = 631

        //selectPicture request code
        const val REQUEST_CHOOSE_FROM_GALLERY = 632

        lateinit var uriDestination: Uri
        lateinit var uriCapturedImage: Uri

    }

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var isToCreateNewPlant = false

    private val _thePlant: MutableLiveData<Plant> by lazy {
        MutableLiveData<Plant>()
    }
    val thePlant: LiveData<Plant>
        get() = _thePlant

    private suspend fun getPlant(plantId: Int): Plant {
        return withContext(Dispatchers.IO) {
            repository.getPlant(plantId)
        }
    }

    suspend fun deletePlant() {
        return withContext(Dispatchers.IO) {
            repository.deletePlant(_thePlant.value!!)
        }
    }

    private suspend fun insertPlant() {
        return withContext(Dispatchers.IO) {
            repository.insertPlant(_thePlant.value!!)
        }
    }

    private suspend fun updatePlant() {
        return withContext(Dispatchers.IO) {
            repository.updatePlant(_thePlant.value!!)
        }
    }


    fun setPlant() {
        if (plantId == -1) {
//            if (BuildConfig.BUILD_TYPE.equals("debug")){

            _thePlant.value = Plant(
                0,
                null,
                null,
                if (BuildConfig.BUILD_TYPE == "debug") "android.resource://com.goranatos.plantkeeper.debug/drawable/ic_plant1" else "android.resource://com.goranatos.plantkeeper/drawable/ic_plant1",

                0,
                null,
                null,

                0,
                null,
                0,
                null,
                null,

                0,
                null,
                0,
                null,
                null,
            )

            _thePlant.value = _thePlant.value
            isToCreateNewPlant = true
        } else {
            uiScope.launch {
                _thePlant.value = plantId?.let { getPlant(it) }

                isToCreateNewPlant = false

            }
        }
    }

    fun setWaterNeedModeOn() {
        _thePlant.value?.is_water_need_on = 1
    }

    fun setWaterNeedModeOff() {
        _thePlant.value?.is_water_need_on = 0
    }

    fun setFertilizeNeedModeOn() {
        _thePlant.value?.is_fertilize_need_on = 1
    }

    fun setFertilizeNeedModeOff() {
        _thePlant.value?.is_fertilize_need_on = 0
    }

    fun setHibernateModeOn() {
        _thePlant.value?.is_hibernate_mode_on = 1
    }

    fun setHibernateModeOff() {
        _thePlant.value?.is_hibernate_mode_on = 0
    }

    fun setPlantImageUriString(uri_string: String) {
        _thePlant.value?.string_uri_image_path = uri_string
    }

    fun setPlantName(plant_name: String) {
        _thePlant.value?.str_name = plant_name
    }

    fun setPlantDescription(plant_description: String) {
        _thePlant.value?.str_desc = plant_description
    }

    fun updateThePlantOutside(plant: Plant) {
        _thePlant.value = plant
    }

    fun onInsertOrUpdatePlant() {
        uiScope.launch {
            if (isToCreateNewPlant) {
                insertPlant()
            } else {
                updatePlant()
            }
        }
    }

}