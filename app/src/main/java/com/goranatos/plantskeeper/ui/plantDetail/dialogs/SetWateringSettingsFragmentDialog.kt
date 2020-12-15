package com.goranatos.plantskeeper.ui.plantDetail.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
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

        setHibernateMode()
        setEditTextListeners()

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

    private fun setEditTextListeners(){
        binding.etWateringFrequencyNormal.addTextChangedListener{
            str_watering_frequency_normal = it.toString()
        }

        binding.etWateringFrequencyInHibernate.addTextChangedListener{
            str_watering_frequency_in_hibernate = it.toString()
        }
    }


    private fun setHibernateMode() {
        binding.groupWateringInHibernateMode.visibility = View.GONE
        binding.switchHibernate.isChecked = false

        binding.switchHibernate.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                setHibernateModeOn()
            } else {
                setHibernateModeOff()
            }
        }
    }

    private fun setHibernateModeOn(){
        str_is_watering_in_hibernate_on = "Y"
        binding.groupWateringInHibernateMode.visibility = View.VISIBLE
    }

    private fun setHibernateModeOff(){
        str_is_watering_in_hibernate_on = "N"
        binding.groupWateringInHibernateMode.visibility = View.GONE
    }

    fun checkInputs(): Boolean{
        if (binding.etWateringFrequencyNormal.editableText.isNullOrEmpty()) return false
        if (binding.switchHibernate.isChecked){
            if (binding.etWateringFrequencyInHibernate.editableText.isNullOrEmpty()) return false
        }
        return true
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

        str_watering_frequency = "$str_watering_frequency_normal|$str_watering_frequency_in_hibernate|$str_is_watering_in_hibernate_on"
        var asd = str_watering_frequency.split("|")
        Toast.makeText(requireContext(), str_watering_frequency + "\n\n $asd", Toast.LENGTH_LONG).show()
        super.onDismiss(dialog)
    }

    // TODO: 12/8/2020 Проверять вводные значения!
}
// TODO: 12/15/2020 textChecker in another class + Timer 
