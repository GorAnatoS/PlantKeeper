package com.goranatos.plantskeeper.ui.plantDetail

import android.net.Uri
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import com.goranatos.plantskeeper.internal.Time
import com.goranatos.plantskeeper.ui.plantDetail.dialogs.SelectPlantImageFromCollectionFragment
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

    }

    private val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var isToCreateNewPlant = false

    private val _thePlant: MutableLiveData<Plant> by lazy {
        MutableLiveData<Plant>()
    }
    val thePlant: LiveData<Plant>
        get() = _thePlant



    suspend fun getPlant(plantId: Int): Plant {
        return withContext(Dispatchers.IO) {
            repository.getPlant(plantId)
        }
    }

    suspend fun deletePlant() {
        return withContext(Dispatchers.IO) {
            repository.deletePlant(_thePlant.value!!)
        }
    }

    suspend fun insertPlant() {
        return withContext(Dispatchers.IO) {
            repository.insertPlant(_thePlant.value!!)
        }
    }

    suspend fun updatePlant() {
        return withContext(Dispatchers.IO) {
            repository.updatePlant(_thePlant.value!!)
        }
    }


    fun updatePlantName(name: String) {
        _thePlant.value?.name = name
    }

    fun setPlant() {
        if (plantId == -1) {
            _thePlant.value = Plant(
                0,
                null,
                null,
                "android.resource://com.goranatos.plantskeeper/drawable/ic_plant1",
                0,
                null,
                0,
                null,
                null,
            )
            isToCreateNewPlant = true
        } else {
            uiScope.launch {
                _thePlant.value = getPlant(plantId)

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

    fun setWaterNeed(to_water_from_date_string: String) {
        _thePlant.value?.water_need = to_water_from_date_string
        updateThePlant()
    }

    fun setHibernateModeOn() {
        _thePlant.value?.is_hibernate_on = 1
    }

    fun setHibernateModeOff() {
        _thePlant.value?.is_hibernate_on = 0
    }

    fun setHibernateModeDateStart(to_hibernate_from_date_long: Long) {
        _thePlant.value?.hibernate_mode_date_start = to_hibernate_from_date_long
        updateThePlant()
    }

    fun setHibernateModeDateFinish(to_hibernate_till_date_long: Long) {
        _thePlant.value?.hibernate_mode_date_finish = to_hibernate_till_date_long
        updateThePlant()
    }


    fun setPlantImageUriString(uri_string: String) {
        _thePlant.value?.image_path = uri_string
    }

    fun setPlantName(plant_name: String) {
        _thePlant.value?.name = plant_name
    }

    fun updateThePlant(){
        _thePlant.value = _thePlant.value
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

    //OPTIONS MENU START
    //OPTIONS MENU END


    /**
     * Запускает MaterialDatePicker и менять textView на выборанную дату
     */
    fun startDatePicker(title: String, fragmentManager: FragmentManager, textView: TextView) {
        val builder = MaterialDatePicker.Builder.datePicker()

        builder.setTitleText(title)
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            textView.text = Time.getFormattedDateString(it)
        }

        materialDatePicker.show(fragmentManager, "DATE_PICKER")
    }


    /**
     * Для запуска диалога по выбору изображения из коллекции
     */

    fun toggleSelectImageClicked(fragmentManager: FragmentManager) {
        val newFragment = SelectPlantImageFromCollectionFragment()
        newFragment.show(fragmentManager, "dialog")
    }

    fun setDatePickerForStartWatering(fragmentManager: FragmentManager) {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Поливать с")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            setWaterNeed(Time.getFormattedDateString(it))
        }

        materialDatePicker.show(fragmentManager, "DATE_PICKER")
    }

    fun setStartDatePickerForHibernateMode(fragmentManager: FragmentManager) {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Режим покоя начинается с")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            setHibernateModeDateStart(it)
        }

        materialDatePicker.show(fragmentManager, "DATE_PICKER")
    }

    fun setFinishDatePickerForHibernateMode(fragmentManager: FragmentManager) {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Режим покоя заканчивается")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            setHibernateModeDateFinish(it)
        }

        materialDatePicker.show(fragmentManager, "DATE_PICKER")
    }

}

///**
// * If plant.is_hibernate_on == 1 then is checked, 0 - unchecked
// */
//val isSwitchHibernateBtnVisible = Transformations.map(_thePlant) {
//    it.is_hibernate_on != 0
//}
//
///**
// * Если у растения поле water_need не установлено или пусто, то значит кнопка отжата. Иначе - наоборот
// */
//val isToggleToWaterChecked = Transformations.map(_thePlant) {
//    !it.water_need.isNullOrEmpty()
//}
//
//val toStartWaterFromDate = Transformations.map(_thePlant) {
//    it.water_need
//}