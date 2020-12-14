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
import com.goranatos.plantskeeper.databinding.IncludeHibernateSettingsBinding
import com.goranatos.plantskeeper.internal.Time


/**
 * Created by qsufff on 12/7/2020.
 */

const val TO_HIBERNATE_FROM_DATE_STRING = "to_hibernate_from_date_string"

class SetHibernateSettingsFragmentDialog : DialogFragment() {

    lateinit var myDialog: Dialog

    lateinit var binding: IncludeHibernateSettingsBinding

    var isNotSaveResult = true

    var saved_to_hibernate_from_date: String = ""
    var saved_to_hibernate_till_date: String = ""

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

        saved_to_hibernate_from_date = Time.getFormattedDateString()
        binding.tvHibernateDateStartFromVal.text = saved_to_hibernate_from_date
        binding.tvHibernateDateFinishVal.text = saved_to_hibernate_till_date

        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Режим покоя начинается с")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            saved_to_hibernate_from_date = Time.getFormattedDateString(it)
            binding.tvHibernateDateStartFromVal.text = saved_to_hibernate_from_date
        }

        binding.tvHibernateDateStartFromVal.setOnClickListener {
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        myDialog = super.onCreateDialog(savedInstanceState)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return myDialog
    }

    override fun onDismiss(dialog: DialogInterface) {


        if (isNotSaveResult) {
            findNavController().currentBackStackEntry?.savedStateHandle?.set(
                TO_HIBERNATE_FROM_DATE_STRING,
                "uncheck"
            )
        } else {
            findNavController().currentBackStackEntry?.savedStateHandle?.set(
                TO_HIBERNATE_FROM_DATE_STRING,
                saved_to_hibernate_from_date
            )
        }

        super.onDismiss(dialog)
    }

    // TODO: 12/8/2020 Проверять вводные значения!
}
