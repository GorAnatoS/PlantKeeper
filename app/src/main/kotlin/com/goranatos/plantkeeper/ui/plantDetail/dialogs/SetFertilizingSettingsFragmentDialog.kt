package com.goranatos.plantkeeper.ui.plantDetail.dialogs

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
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.databinding.IncludePlantFertilizingSettingsBinding
import com.goranatos.plantkeeper.internal.TimeHelper
import com.goranatos.plantkeeper.ui.plantDetail.PlantDetailViewModel


/**
 * Created by qsufff on 12/7/2020.
 */

class SetFertilizingSettingsFragmentDialog(private val viewModel: PlantDetailViewModel) :
    DialogFragment() {

    lateinit var plant: Plant

    private lateinit var myDialog: Dialog

    lateinit var binding: IncludePlantFertilizingSettingsBinding

    private var isToSaveResult = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.include_plant_fertilizing_settings,
                container,
                false
            )

        plant = viewModel.thePlant.value!!

        setHibernateMode()

        setEditTextListeners()

        setTvToFertilizeFromDate()

        setToFertilizeFromDate()

        setSaveBtn()

        setCancelBtn()

        return binding.root
    }

    private fun setTvToFertilizeFromDate() {
        if (plant.long_next_fertilizing_date == null) {
            plant.long_next_fertilizing_date = TimeHelper.getNextDayDate()
            binding.tvToFertilizeFromDateVal.text =
                TimeHelper.getFormattedDateString(plant.long_next_fertilizing_date!!)
        } else {
            binding.tvToFertilizeFromDateVal.text =
                TimeHelper.getFormattedDateString(plant.long_next_fertilizing_date!!)
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

    private fun setToFertilizeFromDate() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Следующий раз удобряем:")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            plant.long_next_fertilizing_date = it
            binding.tvToFertilizeFromDateVal.text =
                TimeHelper.getFormattedDateString(plant.long_next_fertilizing_date!!)
        }

        binding.tvToFertilizeFromDateVal.setOnClickListener {
            materialDatePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    private fun setEditTextListeners() {
        plant.int_fertilizing_frequency_normal?.let {
            binding.etFertilizingFrequencyNormal.setText(
                it.toString(),
                TextView.BufferType.EDITABLE
            )
        }

        plant.int_fertilizing_frequency_in_hibernate?.let {
            binding.etFertilizingFrequencyInHibernate.setText(
                it.toString(),
                TextView.BufferType.EDITABLE
            )
        }

        binding.etFertilizingFrequencyNormal.addTextChangedListener {
            if (!it.isNullOrEmpty())
                plant.int_fertilizing_frequency_normal = it.toString().toInt()
        }

        binding.etFertilizingFrequencyInHibernate.addTextChangedListener {
            if (!it.isNullOrEmpty())
                plant.int_fertilizing_frequency_in_hibernate = it.toString().toInt()
        }
    }


    private fun setHibernateMode() {
        binding.switchHibernate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setFertilizingHibernateModeOn()
            } else {
                setFertilizingHibernateModeOff()
            }
        }

        binding.switchHibernate.isChecked = plant.is_fertilizing_hibernate_mode_on == 1
    }

    private fun setFertilizingHibernateModeOn() {
        plant.is_fertilizing_hibernate_mode_on = 1
        binding.groupFertilizingInHibernateMode.visibility = View.VISIBLE
    }

    private fun setFertilizingHibernateModeOff() {
        plant.is_fertilizing_hibernate_mode_on = 0
        binding.groupFertilizingInHibernateMode.visibility = View.GONE
    }

    private fun areCheckedInputsOk(): Boolean {
        var isOk = true

        if (binding.etFertilizingFrequencyNormal.editableText.isNullOrEmpty()) {
            binding.etLayoutFertilizingFrequencyNormal.isErrorEnabled = true
            binding.etLayoutFertilizingFrequencyNormal.error = "Ошибка"

            isOk = false
        } else {
            binding.etLayoutFertilizingFrequencyNormal.isErrorEnabled = false
            binding.etLayoutFertilizingFrequencyNormal.error = null
        }

        if (binding.switchHibernate.isChecked) {
            if (binding.etFertilizingFrequencyInHibernate.editableText.isNullOrEmpty()) {
                binding.etLayoutFertilizingFrequencyInHibernate.isErrorEnabled = true
                binding.etLayoutFertilizingFrequencyInHibernate.error = "Ошибка"
                isOk = false
            } else {
                binding.etLayoutFertilizingFrequencyInHibernate.isErrorEnabled = false
                binding.etLayoutFertilizingFrequencyInHibernate.error = null
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
            plant.is_fertilize_need_on = 0
            viewModel.updateThePlantOutside(plant)
        }

        super.onDismiss(dialog)
    }
}