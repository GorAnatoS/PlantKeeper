package com.goranatos.plantskeeper.ui.plantDetail.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.databinding.IncludePlantWateringSettingsBinding
import com.goranatos.plantskeeper.internal.Time


/**
 * Created by qsufff on 12/7/2020.
 */

const val TO_WATER_FROM_DATE_STRING = "to_water_from_date_string"

class SetWateringSettingsFragmentDialog : DialogFragment() {

    lateinit var myDialog: Dialog

    lateinit var binding: IncludePlantWateringSettingsBinding

    var long_saved_to_water_from_date: Long = 0L

    var isNotSaveResult = true

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

        setHibernateMode()

        long_saved_to_water_from_date = Time.getCurrentTimeInMs()
        binding.tvToWaterFromDateVal.text = Time.getFormattedDateString(long_saved_to_water_from_date)

        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Поливать с")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            long_saved_to_water_from_date = it
            binding.tvToWaterFromDateVal.text = Time.getFormattedDateString(long_saved_to_water_from_date)
        }

        binding.tvToWaterFromDateVal.setOnClickListener {
            materialDatePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        binding.buttonSave.setOnClickListener {
            isNotSaveResult = false

            //я сохраняю данные при onDismiss
            dismiss()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()

            isNotSaveResult = true

        }

        return binding.root
    }

    private fun setHibernateMode() {
        binding.groupWateringInHibernateMode.visibility = View.GONE
        binding.switchHibernate.isChecked = false

        binding.switchHibernate.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.groupWateringInHibernateMode.visibility = View.VISIBLE
            } else {
                binding.groupWateringInHibernateMode.visibility = View.GONE
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        myDialog = super.onCreateDialog(savedInstanceState)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return myDialog
    }

    override fun onDismiss(dialog: DialogInterface) {


        if (isNotSaveResult) {
            findNavController().currentBackStackEntry?.savedStateHandle?.set(
                TO_WATER_FROM_DATE_STRING,
                0L
            )
        } else {
            findNavController().currentBackStackEntry?.savedStateHandle?.set(
                TO_WATER_FROM_DATE_STRING,
                long_saved_to_water_from_date
            )
        }

        super.onDismiss(dialog)
    }

    // TODO: 12/8/2020 Проверять вводные значения!
}
