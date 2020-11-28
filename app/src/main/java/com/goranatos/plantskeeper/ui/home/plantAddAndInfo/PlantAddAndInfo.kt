package com.goranatos.plantskeeper.ui.home.plantAddAndInfo

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.databinding.FragmentAddAndChangePlantBinding
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.goranatos.plantskeeper.ui.home.MyPlantsFragment.Companion.uiScope
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModel
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModelFactory
import com.goranatos.plantskeeper.util.Helper.Companion.hideKeyboard
import com.ramotion.circlemenu.CircleMenuView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.util.*

/*
    Добавляет новые и редактирует имеющийся цветок\растение

    при создание -1 -> создание нового цветка, иначе - редактирование номера в БД
 */

class PlantAddAndInfo : ScopedFragment(), DIAware {

    private fun isToCreateNewPlant() {
        plant_id = arguments?.getInt("plant_id_in_database")!!
        isAddNewPlant = plant_id == -1
    }

    companion object {
        var isAddNewPlant: Boolean = false

        //id and Plant которые надо изменить в БД
        lateinit var thePlant: Plant
        var plant_id = 0

    }

    override val di by closestDI()

    private lateinit var viewModel: MyPlantsViewModel
    private val viewModelFactory: MyPlantsViewModelFactory by instance()

    lateinit var binding: FragmentAddAndChangePlantBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isToCreateNewPlant()

        viewModel = ViewModelProvider(this, viewModelFactory).get(MyPlantsViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_add_and_change_plant,
                container,
                false
            )

        binding.lifecycleOwner = this

        if (!isAddNewPlant) {
            setHasOptionsMenu(true)
            bindUI()
            binding.buttonAddAndChange.text = getString(R.string.change)
        } else {
            binding.buttonAddAndChange.text = getString(R.string.create)
        }

        setCircleButton()

        binding.buttonAddAndChange.setOnClickListener {

            if (binding.editTextTextPlantName.text.toString().isNullOrEmpty()) {
                showWrongInput()
                Snackbar.make(
                    requireView(),
                    getString(R.string.give_a_name_to_a_plant),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                if (isAddNewPlant) {
                    uiScope.launch {

                        binding.apply {
                            val newPlant = Plant(
                                0,
                                editTextTextPlantName.text.toString(),
                                editTextTextPlantDescription.text.toString()
                            )

                            viewModel.insertPlant(newPlant)

                        }
                    }

                    Snackbar.make(requireView(), getString(R.string.added), Snackbar.LENGTH_SHORT)
                        .show()
                } else {

                    uiScope.launch(Dispatchers.IO) {

                        val newPlant = Plant(
                            plant_id,
                            binding.editTextTextPlantName.text.toString(),
                            binding.editTextTextPlantDescription.text.toString()
                        )

                        viewModel.updatePlant(newPlant)

                    }
                    Snackbar.make(requireView(), getString(R.string.changed), Snackbar.LENGTH_SHORT)
                        .show()

                }

                hideKeyboard()
                findNavController().navigateUp()
            }
        }

        return binding.root
    }


    private fun setCircleButton() {
        binding.circleButton.eventListener = object :
            CircleMenuView.EventListener() {
            override fun onMenuOpenAnimationStart(view: CircleMenuView) {
                //binding.circleButton.visibility = View.INVISIBLE
            }

            override fun onMenuCloseAnimationEnd(view: CircleMenuView) {
                //binding.circleButton.visibility = View.VISIBLE
            }

            override fun onButtonClickAnimationStart(
                view: CircleMenuView,
                index: Int
            ) {
                when (index) {
                    0 -> binding.plantImage.setImageResource(R.drawable.ic_flower)
                    1 -> binding.plantImage.setImageResource(R.drawable.ic_cactus)
                    2 -> binding.plantImage.setImageResource(R.drawable.ic_plant)
                    3 -> binding.plantImage.setImageResource(R.drawable.ic_tree)
                    4 -> {
                        dispatchTakePictureIntent()
                    }
                    5 -> {

                    }


                }

                fun onButtonClickAnimationEnd(
                    view: CircleMenuView,
                    index: Int
                ) {

                    binding.circleButton.visibility = View.VISIBLE
                }
            }
        }
    }


    val REQUEST_IMAGE_CAPTURE = 1
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.plantImage.setImageBitmap(imageBitmap)
            binding.circleButton.visibility = View.VISIBLE
        }
    }



    private fun showWrongInput() {
        hideKeyboard()

        val color = binding.editTextTextPlantName.currentHintTextColor
        if (binding.editTextTextPlantName.text.isEmpty()) binding.editTextTextPlantName.setHintTextColor(
            ContextCompat.getColor(requireContext(), R.color.errorColor)
        )
        else binding.editTextTextPlantName.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                color
            )
        )

        val mTimerTask =
            MyTimerTask()
        val mTimer = Timer()
        mTimer.schedule(mTimerTask, 850)
    }

    internal inner class MyTimerTask : TimerTask() {
        override fun run() {
            activity?.runOnUiThread {
                binding.editTextTextPlantName.setHintTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.textColor
                    )
                )
            }
        }
    }

    private fun bindUI() = launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {

            viewModel.getPlant(plant_id).observe(viewLifecycleOwner, {
                it?.let { plant ->
                    thePlant = plant

                    binding.apply {
                        binding.editTextTextPlantName.setText(thePlant.name.toString())
                        binding.editTextTextPlantDescription.setText(thePlant.desc.toString())
                    }
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plant_info_menu, menu)

        val deleteItemFromDB = menu?.findItem(R.id.delete_item_from_db)

        deleteItemFromDB.setOnMenuItemClickListener {
            deleteItemFromDB()
            true
        }

        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun deleteItemFromDB() {
        launch(Dispatchers.IO) {
            viewModel.deletePlant(thePlant)

            Snackbar.make(requireView(), getString(R.string.deleted), Snackbar.LENGTH_SHORT)
                .show()

            findNavController().navigateUp()
        }
    }

    override fun onStop() {
        super.onStop()
        binding.circleButton.visibility = View.INVISIBLE
    }
}