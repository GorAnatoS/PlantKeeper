package com.goranatos.plantskeeper.ui.home

import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.viewModelScope
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.databinding.DialogPlantInfoBinding
import com.goranatos.plantskeeper.internal.TimeHelper
import com.goranatos.plantskeeper.internal.TimeHelper.Companion.getDaysTillWateringNotification
import com.goranatos.plantskeeper.internal.TimeHelper.Companion.longDatePlusDays
import com.goranatos.plantskeeper.ui.home.MyPlantsFragment.Companion.deletePlantItemFromDB

/**
 * Created by qsufff on 12/7/2020.
 */

class PlantInfoFragmentDialog(private val viewModel: MyPlantsViewModel) :
    DialogFragment() {

    lateinit var plant: Plant

    lateinit var myDialog: Dialog

    lateinit var binding: DialogPlantInfoBinding

    var isToSaveResult = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.dialog_plant_info,
                container,
                false
            )

        binding.viewModel = viewModel

        plant = viewModel.thePlant

        setWateringNeedVisible()
        setPlantImage()
        setPlantInfo()

        setSaveBtn()

        setOnButtonsClicked()

        return binding.root
    }

    private fun setPlantImage() {
        if (plant.string_uri_image_path != null) {
            binding.imageViewPlant.setImageURI(Uri.parse(plant.string_uri_image_path))
        }
    }

    private fun setOnButtonsClicked() {
        binding.toggleEditPlant.setOnClickListener {
            onEditButtonClicked()

            dismiss()
        }

        binding.toggleDeletePlant.setOnClickListener {
            onDeleteButtonClicked()
            dismiss()
        }
    }

    private fun setPlantInfo() {
        if (plant.is_water_need_on == 1) {
            if (plant.long_next_watering_date != null) binding.tvToWaterFromDateVal.text =
                TimeHelper.getFormattedDateString(plant.long_next_watering_date!!)
            else binding.tvToWaterFromDateVal.visibility = View.GONE

            if (plant.int_watering_frequency_normal == null) binding.tvWateringFrequency.visibility =
                View.GONE
            else binding.tvWateringFrequency.text = plant.int_watering_frequency_normal.toString()

            if (plant.int_watering_frequency_in_hibernate == null) binding.tvWateringFrequencyInHibernate.visibility =
                View.GONE
            else binding.tvWateringFrequencyInHibernate.text =
                plant.int_watering_frequency_in_hibernate.toString()
        } else {
            binding.tvToWaterFromDateVal.visibility = View.INVISIBLE
            binding.tvWateringFrequency.visibility =
                View.INVISIBLE
            binding.tvWateringFrequencyInHibernate.visibility =
                View.INVISIBLE
        }
    }

    private fun setWateringNeedVisible() {
        if (plant.is_water_need_on == 1 && getDaysTillWateringNotification(plant) <= 0) {
            binding.checkBoxWatering.visibility = View.VISIBLE
        } else {
            binding.checkBoxWatering.visibility = View.GONE
            binding.buttonSave.visibility = View.INVISIBLE
        }
    }

    private fun setSaveBtn() {
        binding.buttonSave.setOnClickListener {
            isToSaveResult = true
            //я сохраняю данные при onDismiss

            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        myDialog = super.onCreateDialog(savedInstanceState)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return myDialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (isToSaveResult)
            if (binding.checkBoxWatering.isChecked) {

                if (TimeHelper.isDateInPlantHibernateRange(TimeHelper.getCurrentTimeInMs(), plant)){
                    plant.int_watering_frequency_in_hibernate?.let {
                        viewModel.thePlant.long_next_watering_date = longDatePlusDays(
                            TimeHelper.getCurrentTimeInMs(),
                            plant.int_watering_frequency_in_hibernate!!
                        )
                    }
                } else {
                    plant.int_watering_frequency_normal?.let {
                        viewModel.thePlant.long_next_watering_date = longDatePlusDays(
                            TimeHelper.getCurrentTimeInMs(),
                            plant.int_watering_frequency_normal!!
                        )
                    }
                }

                viewModel.updateThePlant()
            }
        super.onDismiss(dialog)
    }

    private fun onEditButtonClicked() {
        viewModel.updateNavigateToPlantId(viewModel.thePlant.int_id)
        viewModel.onItemClicked()
    }

    private fun onDeleteButtonClicked() {
        deletePlantItemFromDB(
            plant.int_id,
            requireContext(),
            viewModel.viewModelScope,
            viewModel,
            requireView()
        )
    }
}