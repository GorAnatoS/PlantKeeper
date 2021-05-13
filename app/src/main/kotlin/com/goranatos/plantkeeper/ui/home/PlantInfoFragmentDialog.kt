package com.goranatos.plantkeeper.ui.home

import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.databinding.DialogPlantInfoBinding
import com.goranatos.plantkeeper.internal.TimeHelper
import com.goranatos.plantkeeper.internal.TimeHelper.Companion.getDaysTillEventNotification
import com.goranatos.plantkeeper.internal.TimeHelper.Companion.longDatePlusDays
import com.goranatos.plantkeeper.ui.home.MyPlantsFragment.Companion.deletePlantItemFromDB

/**
 * Created by qsufff on 12/7/2020.
 */

class PlantInfoFragmentDialog(private val viewModel: MyPlantsViewModel) :
    DialogFragment() {

    lateinit var plant: Plant

    private lateinit var myDialog: Dialog

    lateinit var binding: DialogPlantInfoBinding

    private var isToSaveResult = false
    private var isToShowSaveBtn = false


    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)
    }

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
        setFertilizingNeedVisible()

        if (!isToShowSaveBtn) binding.buttonSave.visibility = View.GONE

        setPlantImage()

        setPlantInfo()

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
        }

        binding.toggleDeletePlant.setOnClickListener {
            onDeleteButtonClicked()
        }

        binding.buttonSave.setOnClickListener {
            isToSaveResult = true
            //я сохраняю данные при onDismiss

            dismiss()
        }
    }

    private fun setPlantInfo() {
        //WATERING
        if (plant.is_water_need_on == 1) {

            if (plant.long_next_watering_date != null) binding.tvToWaterFromDateVal.text =
                getString(
                    R.string.start_with_space_string,
                    TimeHelper.getFormattedDateString(plant.long_next_watering_date!!)
                )
            else binding.tvToWaterFromDateVal.visibility = View.GONE

            if (plant.int_watering_frequency_normal == null) binding.tvWateringFrequency.visibility =
                View.GONE
            else binding.tvWateringFrequency.text = getString(
                R.string.start_with_space_string,
                plant.int_watering_frequency_normal.toString()
            )

            if (plant.int_watering_frequency_in_hibernate == null) binding.tvWateringFrequencyInHibernate.visibility =
                View.GONE
            else binding.tvWateringFrequencyInHibernate.text = getString(
                R.string.start_with_space_string,
                plant.int_watering_frequency_in_hibernate.toString()
            )

            if (plant.is_watering_hibernate_mode_on == 0) {
                binding.tvWateringFrequencyInHibernate.visibility = View.GONE
            }

        } else {
            binding.tvToWaterFromDateVal.visibility = View.GONE
            binding.tvWateringFrequency.visibility =
                View.GONE
            binding.tvWateringFrequencyInHibernate.visibility =
                View.GONE

            binding.tvToWaterIcon.visibility = View.GONE
        }


        //FERTILIZING
        if (plant.is_fertilize_need_on == 1) {

            if (plant.long_next_fertilizing_date != null) binding.tvToFertilizeFromDateVal.text =
                getString(
                    R.string.start_with_space_string,
                    TimeHelper.getFormattedDateString(plant.long_next_fertilizing_date!!)
                )
            else binding.tvToFertilizeFromDateVal.visibility = View.GONE

            if (plant.int_fertilizing_frequency_normal == null) binding.tvFertilizingFrequency.visibility =
                View.GONE
            else binding.tvFertilizingFrequency.text = getString(
                R.string.start_with_space_string,
                plant.int_fertilizing_frequency_normal.toString()
            )

            if (plant.int_fertilizing_frequency_in_hibernate == null) binding.tvFertilizingFrequencyInHibernate.visibility =
                View.GONE
            else binding.tvFertilizingFrequencyInHibernate.text = getString(
                R.string.start_with_space_string,
                plant.int_fertilizing_frequency_in_hibernate.toString()
            )

            if (plant.is_fertilizing_hibernate_mode_on == 0) {
                binding.tvFertilizingFrequencyInHibernate.visibility = View.GONE
            }
        } else {
            binding.tvToFertilizeFromDateVal.visibility = View.GONE
            binding.tvFertilizingFrequency.visibility =
                View.GONE
            binding.tvFertilizingFrequencyInHibernate.visibility =
                View.GONE

            binding.tvToFertilizeIcon.visibility = View.GONE
        }
    }

    private fun setWateringNeedVisible() {
        if (plant.is_water_need_on == 1 && getDaysTillEventNotification(
                System.currentTimeMillis(),
                plant.long_next_watering_date!!
            ) <= 0
        ) {
            binding.checkBoxWatered.visibility = View.VISIBLE
            isToShowSaveBtn = true
        } else {
            binding.checkBoxWatered.visibility = View.GONE
        }
    }

    private fun setFertilizingNeedVisible() {
        if (plant.is_fertilize_need_on == 1 && getDaysTillEventNotification(
                System.currentTimeMillis(),
                plant.long_next_fertilizing_date!!
            ) <= 0
        ) {
            binding.checkBoxFertilized.visibility = View.VISIBLE
            isToShowSaveBtn = true
        } else {
            binding.checkBoxFertilized.visibility = View.GONE
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        myDialog = super.onCreateDialog(savedInstanceState)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return myDialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (isToSaveResult) {
            isWateredCheckBoxChecked()
            isFertilizedCheckBoxChecked()

            viewModel.updateThePlant()
        }

        super.onDismiss(dialog)
    }

    private fun isFertilizedCheckBoxChecked() {
        if (binding.checkBoxFertilized.isChecked) {
            updatePlantFertilizeData()
        }
    }

    private fun updatePlantFertilizeData() {
        if (plant.is_fertilizing_hibernate_mode_on == 1 && plant.is_hibernate_mode_on == 1 &&
            plant.long_to_hibernate_from_date != null && plant.long_to_hibernate_till_date != null &&
            TimeHelper.isDateInPlantHibernateRange(
                TimeHelper.getCurrentTimeInMs(),
                plant.long_to_hibernate_from_date!!,
                plant.long_to_hibernate_till_date!!
            )
        ) {

            plant.int_fertilizing_frequency_in_hibernate?.let {
                viewModel.thePlant.long_next_fertilizing_date = longDatePlusDays(
                    TimeHelper.getCurrentTimeInMs(),
                    plant.int_fertilizing_frequency_in_hibernate!!
                )
            }
        } else {
            plant.int_fertilizing_frequency_normal?.let {
                viewModel.thePlant.long_next_fertilizing_date = longDatePlusDays(
                    TimeHelper.getCurrentTimeInMs(),
                    plant.int_fertilizing_frequency_normal!!
                )
            }
        }
    }

    private fun isWateredCheckBoxChecked() {
        if (binding.checkBoxWatered.isChecked) {
            updatePlantWateringData()
        }
    }

    private fun updatePlantWateringData() {
        if (plant.is_watering_hibernate_mode_on == 1 && plant.is_hibernate_mode_on == 1 &&
            plant.long_to_hibernate_from_date != null && plant.long_to_hibernate_till_date != null &&
            TimeHelper.isDateInPlantHibernateRange(
                TimeHelper.getCurrentTimeInMs(),
                plant.long_to_hibernate_from_date!!,
                plant.long_to_hibernate_till_date!!
            )
        )
            plant.int_watering_frequency_in_hibernate?.let {
                viewModel.thePlant.long_next_watering_date = longDatePlusDays(
                    TimeHelper.getCurrentTimeInMs(),
                    plant.int_watering_frequency_in_hibernate!!
                )
            }
        else {
            plant.int_watering_frequency_normal?.let {
                viewModel.thePlant.long_next_watering_date = longDatePlusDays(
                    TimeHelper.getCurrentTimeInMs(),
                    plant.int_watering_frequency_normal!!
                )
            }
        }
    }

    private fun onEditButtonClicked() {
        viewModel.updateNavigateToPlantId(viewModel.thePlant.int_id)
        viewModel.onItemClicked()

        dismiss()
    }

    private fun onDeleteButtonClicked() {
        deletePlantItemFromDB(
            plant.int_id,
            requireContext(),
            viewModel.viewModelScope,
            viewModel,
            requireView()
        )

        dismiss()
    }
}