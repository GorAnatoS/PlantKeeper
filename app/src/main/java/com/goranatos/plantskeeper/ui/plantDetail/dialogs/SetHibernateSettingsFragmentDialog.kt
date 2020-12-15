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
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.databinding.IncludeHibernateSettingsBinding
import com.goranatos.plantskeeper.internal.TimeHelper
import java.util.*


/**
 * Created by qsufff on 12/7/2020.
 */

const val TO_HIBERNATE_FROM_DATE_LONG = "to_hibernate_from_date_long"
const val TO_HIBERNATE_TILL_DATE_LONG = "to_hibernate_till_date_long"

class SetHibernateSettingsFragmentDialog(val plant: Plant) : DialogFragment() {

    lateinit var myDialog: Dialog

    lateinit var binding: IncludeHibernateSettingsBinding

    var isNotSaveResult = true

    var saved_to_hibernate_from_date_long = 0L
    var saved_to_hibernate_till_date_long = 0L

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

        saved_to_hibernate_from_date_long = TimeHelper.getCurrentTimeInMs()
        binding.tvHibernateDateStartFromVal.text = TimeHelper.getFormattedDateString(saved_to_hibernate_from_date_long)

        val calendar : Calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 4)
        saved_to_hibernate_till_date_long = calendar.timeInMillis
        binding.tvHibernateDateFinishVal.text = TimeHelper.getFormattedDateString(saved_to_hibernate_till_date_long)

        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Режим покоя начинается с")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            saved_to_hibernate_from_date_long = it
            binding.tvHibernateDateStartFromVal.text = TimeHelper.getFormattedDateString(saved_to_hibernate_from_date_long)
        }

        binding.tvHibernateDateStartFromVal.setOnClickListener {
            materialDatePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        ////////////////////

        val builder2 = MaterialDatePicker.Builder.datePicker()
        builder2.setTitleText("Режим покоя заканчивается")
        val materialDatePicker2 = builder2.build()

        materialDatePicker2.addOnPositiveButtonClickListener {
            saved_to_hibernate_till_date_long = it
            binding.tvHibernateDateFinishVal.text = TimeHelper.getFormattedDateString(saved_to_hibernate_till_date_long)
        }

        binding.tvHibernateDateFinishVal.setOnClickListener {
            materialDatePicker2.show(parentFragmentManager, "DATE_PICKER")
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        myDialog = super.onCreateDialog(savedInstanceState)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return myDialog
    }

    override fun onDismiss(dialog: DialogInterface) {


        if (isNotSaveResult) {
            findNavController().currentBackStackEntry?.savedStateHandle?.set(
                TO_HIBERNATE_FROM_DATE_LONG,
                0L
            )
            findNavController().currentBackStackEntry?.savedStateHandle?.set(
                TO_HIBERNATE_TILL_DATE_LONG,
                0L
            )
        } else {
            findNavController().currentBackStackEntry?.savedStateHandle?.set(
                TO_HIBERNATE_FROM_DATE_LONG,
                saved_to_hibernate_from_date_long
            )

            findNavController().currentBackStackEntry?.savedStateHandle?.set(
                TO_HIBERNATE_TILL_DATE_LONG,
                saved_to_hibernate_till_date_long
            )
        }

        super.onDismiss(dialog)
    }

    // TODO: 12/8/2020 Проверять вводные значения!
}
