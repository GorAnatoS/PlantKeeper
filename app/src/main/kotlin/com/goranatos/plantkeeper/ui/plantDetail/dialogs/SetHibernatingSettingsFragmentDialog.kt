package com.goranatos.plantkeeper.ui.plantDetail.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.databinding.IncludeHibernateSettingsBinding
import com.goranatos.plantkeeper.internal.TimeHelper
import com.goranatos.plantkeeper.ui.plantDetail.PlantDetailViewModel
import java.util.*


/**
 * Created by qsufff on 12/7/2020.
 */

class SetHibernatingSettingsFragmentDialog(private val viewModel: PlantDetailViewModel) :
    DialogFragment() {

    lateinit var plant: Plant

    private lateinit var myDialog: Dialog

    lateinit var binding: IncludeHibernateSettingsBinding

    private var isToSaveResult = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.include_hibernate_settings,
                container,
                false
            )

        plant = viewModel.thePlant.value!!

        setTvHibernateDateStartFromVal()

        setTvHibernateDateFinishVal()

        initTimePickers()

        setSaveBtn()

        setCancelBtn()

        return binding.root
    }

    private fun setCancelBtn() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
            isToSaveResult = false
        }
    }

    private fun setSaveBtn() {
        binding.buttonSave.setOnClickListener {
            isToSaveResult = true
            //я сохраняю данные при onDismiss
            dismiss()
        }
    }

    private fun setTvHibernateDateStartFromVal() {
        if (plant.long_to_hibernate_from_date != null) {
            binding.tvHibernateDateStartFromVal.text =
                TimeHelper.getFormattedDateString(plant.long_to_hibernate_from_date!!)
        } else {
            plant.long_to_hibernate_from_date = TimeHelper.getCurrentTimeInMs()
            binding.tvHibernateDateStartFromVal.text =
                TimeHelper.getFormattedDateString(plant.long_to_hibernate_from_date!!)
        }
    }

    private fun setTvHibernateDateFinishVal() {
        if (plant.long_to_hibernate_till_date != null) {
            binding.tvHibernateDateFinishVal.text =
                TimeHelper.getFormattedDateString(plant.long_to_hibernate_till_date!!)
        } else {
            val calendar: Calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 4)
            plant.long_to_hibernate_till_date = calendar.timeInMillis

            binding.tvHibernateDateFinishVal.text =
                TimeHelper.getFormattedDateString(plant.long_to_hibernate_till_date!!)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        myDialog = super.onCreateDialog(savedInstanceState)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return myDialog
    }

    private fun initTimePickers() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Режим покоя начинается с")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            plant.long_to_hibernate_from_date = it
            binding.tvHibernateDateStartFromVal.text =
                TimeHelper.getFormattedDateString(plant.long_to_hibernate_from_date!!)
        }

        binding.tvHibernateDateStartFromVal.setOnClickListener {
            materialDatePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        val builder2 = MaterialDatePicker.Builder.datePicker()
        builder2.setTitleText("Режим покоя заканчивается")
        val materialDatePicker2 = builder2.build()

        materialDatePicker2.addOnPositiveButtonClickListener {
            plant.long_to_hibernate_till_date = it
            binding.tvHibernateDateFinishVal.text =
                TimeHelper.getFormattedDateString(plant.long_to_hibernate_till_date!!)
        }

        binding.tvHibernateDateFinishVal.setOnClickListener {
            materialDatePicker2.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (isToSaveResult)
            viewModel.updateThePlantOutside(plant)
        else {
            plant.is_hibernate_mode_on = 0
            viewModel.updateThePlantOutside(plant)
        }

        super.onDismiss(dialog)
    }
}