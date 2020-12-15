package com.goranatos.plantskeeper.ui.plantDetail.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.databinding.IncludePlantWateringSettingsBinding
import com.goranatos.plantskeeper.internal.TimeHelper
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModel
import java.util.*


/**
 * Created by qsufff on 12/7/2020.
 */

const val TO_WATER_FROM_DATE_STRING = "to_water_from_date_string"

class SetWateringSettingsFragmentDialog(private val viewModel: PlantDetailViewModel) : DialogFragment() {

    lateinit var plant: Plant

    lateinit var myDialog: Dialog

    lateinit var binding: IncludePlantWateringSettingsBinding

    var isToSaveResult = false

    var str_watering_frequency = ""
    var str_is_watering_in_hibernate_on = "N"
    var str_watering_frequency_normal = ""
    var str_watering_frequency_in_hibernate = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.include_plant_watering_settings,
                container,
                false
            )

        plant = viewModel.thePlant.value!!

        setHibernateMode()
        setEditTextListeners()

        setTvToWaterFromDate()

        setToWaterFromDate()

        setSaveBtn()

        setCancelBtn()

        return binding.root
    }

    private fun setTvToWaterFromDate() {
        if (plant.long_to_water_from_date == null) {
            plant.long_to_water_from_date = TimeHelper.getCurrentTimeInMs()
            binding.tvToWaterFromDateVal.text = TimeHelper.getFormattedDateString(plant.long_to_water_from_date!!)
        } else {
            binding.tvToWaterFromDateVal.text =
                TimeHelper.getFormattedDateString(plant.long_to_water_from_date!!)
        }
    }

    private fun setCancelBtn() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
            isToSaveResult = false
        }
    }

    private fun setSaveBtn() {
        binding.buttonSave.setOnClickListener {
            if (areCheckedInputsOk()) {
                isToSaveResult = true
                //я сохраняю данные при onDismiss
                dismiss()
            }
        }
    }

    private fun setToWaterFromDate() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Поливать с")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            plant.long_to_water_from_date = it
            binding.tvToWaterFromDateVal.text =
                TimeHelper.getFormattedDateString(plant.long_to_water_from_date!!)
        }

        binding.tvToWaterFromDateVal.setOnClickListener {
            materialDatePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    private fun setEditTextListeners(){
        binding.etWateringFrequencyNormal.addTextChangedListener{
            str_watering_frequency_normal = it.toString()
        }

        binding.etWateringFrequencyInHibernate.addTextChangedListener{
            str_watering_frequency_in_hibernate = it.toString()
        }
    }


    private fun setHibernateMode() {
        binding.switchHibernate.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                setHibernateModeOn()
            } else {
                setHibernateModeOff()
            }
        }

        binding.switchHibernate.isChecked = plant.is_hibernate_on == 1
    }

    private fun setHibernateModeOn(){
        str_is_watering_in_hibernate_on = "Y"
        binding.groupWateringInHibernateMode.visibility = View.VISIBLE
    }

    private fun setHibernateModeOff(){
        str_is_watering_in_hibernate_on = "N"
        binding.groupWateringInHibernateMode.visibility = View.GONE
    }

    private fun areCheckedInputsOk(): Boolean {
        var isOk = true

        if (binding.etWateringFrequencyNormal.editableText.isNullOrEmpty()) {
            binding.etLayoutWateringFrequencyNormal.isErrorEnabled = true
            binding.etLayoutWateringFrequencyNormal.error = "Ошибка"

            isOk = false
        } else {
            binding.etLayoutWateringFrequencyNormal.isErrorEnabled = false
            binding.etLayoutWateringFrequencyNormal.error = null
        }

        if (binding.switchHibernate.isChecked){
            if (binding.etWateringFrequencyInHibernate.editableText.isNullOrEmpty()) {
                binding.etLayoutWateringFrequencyInHibernate.isErrorEnabled = true
                binding.etLayoutWateringFrequencyInHibernate.error = "Ошибка"
                isOk = false
            } else {
                binding.etLayoutWateringFrequencyInHibernate.isErrorEnabled = false
                binding.etLayoutWateringFrequencyInHibernate.error = null
            }
        }

        return isOk
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        myDialog = super.onCreateDialog(savedInstanceState)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return myDialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (isToSaveResult) {
            viewModel.updateThePlant(plant)
        }

        str_watering_frequency = "$str_watering_frequency_normal|$str_watering_frequency_in_hibernate|$str_is_watering_in_hibernate_on"
        var asd = str_watering_frequency.split("|")
        Toast.makeText(requireContext(), str_watering_frequency + "\n\n $asd", Toast.LENGTH_LONG).show()
        super.onDismiss(dialog)
    }
}

