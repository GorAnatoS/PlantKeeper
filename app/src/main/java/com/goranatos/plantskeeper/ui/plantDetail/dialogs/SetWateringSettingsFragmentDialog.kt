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


    var saved_to_water_from_date: String = ""
    /** The system calls this to get the DialogFragment's layout, regardless
    of whether it's being displayed as a dialog or an embedded fragment. */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout to use as dialog or embedded fragment

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.include_plant_watering_settings,
                container,
                false
            )

        setHibernateMode()

        saved_to_water_from_date = Time.getFormattedDateString()
        binding.tvToWaterFromDateVal.text = saved_to_water_from_date

        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Поливать с")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            saved_to_water_from_date = Time.getFormattedDateString(it)
            binding.tvToWaterFromDateVal.text = saved_to_water_from_date
        }



        binding.tvToWaterFromDateVal.setOnClickListener {
            materialDatePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        binding.buttonSave.setOnClickListener {
            dismiss()

            findNavController().currentBackStackEntry?.savedStateHandle?.set(
                TO_WATER_FROM_DATE_STRING,
                saved_to_water_from_date
            )
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun dismiss() {

        findNavController().currentBackStackEntry?.savedStateHandle?.set(
            TO_WATER_FROM_DATE_STRING,
            null
        )
        super.dismiss()


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

    /** The system calls this only when creating the layout in a dialog. */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        myDialog = super.onCreateDialog(savedInstanceState)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return myDialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    // TODO: 12/8/2020 Проверять вводные значения!
}
