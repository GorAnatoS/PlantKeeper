package com.goranatos.plantkeeper.ui.plantinfo

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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.databinding.DialogPlantInfoBinding
import com.goranatos.plantkeeper.ui.myplants.MyPlantsFragmentDirections
import com.goranatos.plantkeeper.ui.todo.TodoFragmentDirections
import com.goranatos.plantkeeper.utilities.Helper
import com.goranatos.plantkeeper.utilities.PlantHelper
import com.goranatos.plantkeeper.utilities.TimeHelper
import com.goranatos.plantkeeper.utilities.TimeHelper.Companion.longDatePlusDays
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by qsufff on 12/7/2020.
 */

@AndroidEntryPoint
class PlantInfoFragmentDialog(private val plantId: Int) : DialogFragment() {

    private val viewModel by viewModels<PlantInfoViewModel>()

    lateinit var plant: Plant

    private lateinit var myDialog: Dialog

    lateinit var binding: DialogPlantInfoBinding

    private var isToSaveResult = false
    private var isToShowSaveBtn = false


    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true

        super.onCreate(savedInstanceState)
        viewModel.initPlantInfoViewModel(plantId)
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
        setPlantDetail()

        if (!isToShowSaveBtn) binding.buttonSave.visibility = View.GONE

        setPlantImage()

        setPlantInfo()

        setOnButtonsClicked()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(view.context.getDrawable(R.drawable.background))

        viewModel.navigateToThePlant.observe(viewLifecycleOwner, {
            if (it == true) { // Observed state is t
                if (findNavController().currentDestination?.id == R.id.navigation_todo) {
                    findNavController().navigate(
                        TodoFragmentDirections.actionNavigationTodoToPlantAddAndInfo(viewModel.navigateToPlantId)
                    )
                    viewModel.doneNavigating()
                }
                if (findNavController().currentDestination?.id == R.id.navigation_my_plants) {
                    findNavController().navigate(
                        MyPlantsFragmentDirections.actionMyPlantsFragmentToPlantAddAndInfo(viewModel.navigateToPlantId)
                    )
                    viewModel.doneNavigating()
                }
            }
        })
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
            PlantHelper.deletePlantItemFromDB(
                requireContext(),
                ::onDeleteButtonClicked,
                requireParentFragment().requireView()
            )
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
        if (PlantHelper.isWaterTodayNeeded(plant)) {
            binding.checkBoxWatered.visibility = View.VISIBLE
            isToShowSaveBtn = true
        } else {
            binding.checkBoxWatered.visibility = View.GONE
        }
    }

    private fun setFertilizingNeedVisible() {
        if (PlantHelper.isFertilizeTodayNeeded(plant)) {
            binding.checkBoxFertilized.visibility = View.VISIBLE
            isToShowSaveBtn = true
        } else {
            binding.checkBoxFertilized.visibility = View.GONE
        }
    }

    private fun setPlantDetail() {
        if (plant.str_desc.isNullOrEmpty()) {
            binding.plantDetailsBtn.visibility = View.GONE
        } else {
            binding.plantDetailsBtn.visibility = View.VISIBLE
            binding.plantDetailsBtn.setOnClickListener {
                Helper.showMaterialDialogPositiveOnly(plant.str_desc!!, requireContext())
            }
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
        viewModel.deleteThePlant()
        dismiss()
    }
}