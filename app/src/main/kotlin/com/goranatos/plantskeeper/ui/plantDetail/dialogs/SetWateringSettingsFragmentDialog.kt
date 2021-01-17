package com.goranatos.plantskeeper.ui.plantDetail.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.databinding.IncludePlantWateringSettingsBinding
import com.goranatos.plantskeeper.internal.TimeHelper
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModel


/**
 * Created by qsufff on 12/7/2020.
 */

class SetWateringSettingsFragmentDialog(private val viewModel: PlantDetailViewModel) : DialogFragment() {

    lateinit var plant: Plant

    private lateinit var myDialog: Dialog

    lateinit var binding: IncludePlantWateringSettingsBinding

    private var isToSaveResult = false


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
        if (plant.long_next_watering_date == null) {
            plant.long_next_watering_date = TimeHelper.getNextDayDate()
            binding.tvToWaterFromDateVal.text = TimeHelper.getFormattedDateString(plant.long_next_watering_date!!)
        } else {
            binding.tvToWaterFromDateVal.text =
                TimeHelper.getFormattedDateString(plant.long_next_watering_date!!)
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
            plant.long_next_watering_date = it
            binding.tvToWaterFromDateVal.text =
                TimeHelper.getFormattedDateString(plant.long_next_watering_date!!)
        }

        binding.tvToWaterFromDateVal.setOnClickListener {
            materialDatePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    private fun setEditTextListeners(){
        plant.int_watering_frequency_normal?.let {
            binding.etWateringFrequencyNormal.setText(it.toString(), TextView.BufferType.EDITABLE)
        }

        plant.int_watering_frequency_in_hibernate?.let {
            binding.etWateringFrequencyInHibernate.setText(it.toString(), TextView.BufferType.EDITABLE)
        }

        binding.etWateringFrequencyNormal.addTextChangedListener{
            if (!it.isNullOrEmpty())
                plant.int_watering_frequency_normal = it.toString().toInt()
        }

        binding.etWateringFrequencyInHibernate.addTextChangedListener{
            if (!it.isNullOrEmpty())
                plant.int_watering_frequency_in_hibernate = it.toString().toInt()
        }
    }


    private fun setHibernateMode() {
        binding.switchHibernate.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                setWateringHibernateModeOn()
            } else {
                setWateringHibernateModeOff()
            }
        }

        binding.switchHibernate.isChecked = plant.is_watering_hibernate_mode_on == 1
    }

    private fun setWateringHibernateModeOn(){
        plant.is_watering_hibernate_mode_on = 1
        binding.groupWateringInHibernateMode.visibility = View.VISIBLE
    }

    private fun setWateringHibernateModeOff(){
        plant.is_watering_hibernate_mode_on = 0
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
            viewModel.updateThePlantOutside(plant)
        } else {
            plant.is_water_need_on = 0
            viewModel.updateThePlantOutside(plant)
        }

        super.onDismiss(dialog)
    }
}

