package com.goranatos.plantskeeper.ui.home

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
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.databinding.DialogPlantInfoBinding
import com.goranatos.plantskeeper.internal.TimeHelper
import com.goranatos.plantskeeper.internal.TimeHelper.Companion.getDaysTillEventNotification
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
    var isToShowSaveBtn = false

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

        if (!isToShowSaveBtn) binding.buttonSave.visibility = View.INVISIBLE

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
                TimeHelper.getFormattedDateString(plant.long_next_watering_date!!)
            else binding.tvToWaterFromDateVal.visibility = View.GONE

            if (plant.int_watering_frequency_normal == null) binding.tvWateringFrequency.visibility =
                View.GONE
            else binding.tvWateringFrequency.text = plant.int_watering_frequency_normal.toString()

            if (plant.int_watering_frequency_in_hibernate == null) binding.tvWateringFrequencyInHibernate.visibility =
                View.GONE
            else binding.tvWateringFrequencyInHibernate.text =
                plant.int_watering_frequency_in_hibernate.toString()

            if (plant.is_watering_hibernate_mode_on == 0){
                binding.tvWateringFrequencyInHibernate.visibility = View.INVISIBLE
            }

        } else {
            binding.tvToWaterFromDateVal.visibility = View.INVISIBLE
            binding.tvWateringFrequency.visibility =
                View.INVISIBLE
            binding.tvWateringFrequencyInHibernate.visibility =
                View.INVISIBLE

            binding.tvToWaterIcon.visibility = View.INVISIBLE
        }


        //FERTILIZING
        if (plant.is_fertilize_need_on == 1) {

            if (plant.long_next_fertilizing_date != null) binding.tvToFertilizeFromDateVal.text =
                TimeHelper.getFormattedDateString(plant.long_next_fertilizing_date!!)
            else binding.tvToFertilizeFromDateVal.visibility = View.GONE

            if (plant.int_fertilizing_frequency_normal == null) binding.tvFertilizingFrequency.visibility =
                View.GONE
            else binding.tvFertilizingFrequency.text =
                plant.int_fertilizing_frequency_normal.toString()

            if (plant.int_fertilizing_frequency_in_hibernate == null) binding.tvFertilizingFrequencyInHibernate.visibility =
                View.GONE
            else binding.tvFertilizingFrequencyInHibernate.text =
                plant.int_fertilizing_frequency_in_hibernate.toString()

            if (plant.is_fertilizing_hibernate_mode_on == 0){
                binding.tvFertilizingFrequencyInHibernate.visibility = View.INVISIBLE
            }
        } else {
            binding.tvToFertilizeFromDateVal.visibility = View.INVISIBLE
            binding.tvFertilizingFrequency.visibility =
                View.INVISIBLE
            binding.tvFertilizingFrequencyInHibernate.visibility =
                View.INVISIBLE

            binding.tvToFertilizeIcon.visibility = View.INVISIBLE
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
            if (plant.is_hibernate_mode_on == 1 && plant.long_to_hibernate_from_date != null && plant.long_to_hibernate_till_date != null && plant.long_next_fertilizing_date != null) {
                if (TimeHelper.isDateInPlantHibernateRange(
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
    }

    private fun isWateredCheckBoxChecked() {
        if (binding.checkBoxWatered.isChecked) {
            if (plant.is_hibernate_mode_on == 1 && plant.long_to_hibernate_from_date != null && plant.long_to_hibernate_till_date != null && plant.long_next_watering_date != null) {
                if (TimeHelper.isDateInPlantHibernateRange(
                        TimeHelper.getCurrentTimeInMs(),
                        plant.long_to_hibernate_from_date!!,
                        plant.long_to_hibernate_till_date!!
                    )
                ) {
                    plant.int_watering_frequency_in_hibernate?.let {
                        viewModel.thePlant.long_next_watering_date = longDatePlusDays(
                            TimeHelper.getCurrentTimeInMs(),
                            plant.int_watering_frequency_in_hibernate!!
                        )
                    }
                }
            } else {
                plant.int_watering_frequency_normal?.let {
                    viewModel.thePlant.long_next_watering_date = longDatePlusDays(
                        TimeHelper.getCurrentTimeInMs(),
                        plant.int_watering_frequency_normal!!
                    )
                }
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