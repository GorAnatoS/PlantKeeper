package com.goranatos.plantskeeper.ui.home.plantInfo

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.databinding.FragmentPlantInfoBinding
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.goranatos.plantskeeper.ui.home.MyPlantsFragment.Companion.uiScope
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModel
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModelFactory
import com.goranatos.plantskeeper.util.Helper.Companion.hideKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.util.*

class PlantInfo : ScopedFragment(), DIAware {

    companion object {
        lateinit var thePlant: Plant
        var plant_id = 0
    }

    override val di by closestDI()


    private lateinit var viewModel: MyPlantsViewModel
    private val viewModelFactory: MyPlantsViewModelFactory by instance()

    lateinit var bindingPlantInfo: FragmentPlantInfoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        plant_id = arguments?.getInt("plant_id_in_database")!!
        viewModel = ViewModelProvider(this, viewModelFactory).get(MyPlantsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        bindingPlantInfo =
                DataBindingUtil.inflate(inflater, R.layout.fragment_plant_info, container, false)

        bindingPlantInfo.lifecycleOwner = this
        return bindingPlantInfo.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bindUI()


        bindingPlantInfo.buttonChange.setOnClickListener {

            if (bindingPlantInfo.editTextTextPlantName.text.toString().isNullOrEmpty()) {
                showWrongInput()
                Snackbar.make(requireView(), getString(R.string.give_a_name_to_a_plant), Snackbar.LENGTH_SHORT).show()
            } else {
                uiScope.launch(Dispatchers.IO) {//launch(Dispatchers.Default){

                    val newPlant = Plant(
                            plant_id,
                            bindingPlantInfo.editTextTextPlantName.text.toString(),
                            bindingPlantInfo.editTextTextPlantDescription.text.toString()
                    )

                    viewModel.updatePlant(newPlant)

                    hideKeyboard()

                    Snackbar.make(requireView(), getString(R.string.changed), Snackbar.LENGTH_SHORT).show()

                    findNavController().navigateUp()
                }
            }
        }

        setHasOptionsMenu(true)

    }

    private fun showWrongInput(){
        if (bindingPlantInfo.editTextTextPlantName.text.isEmpty()) bindingPlantInfo.editTextTextPlantName.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.errorColor))
        else bindingPlantInfo.editTextTextPlantName.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.textColor))

        val mTimerTask =
                MyTimerTask()
        val mTimer = Timer()
        mTimer.schedule(mTimerTask, 850)

    }

    internal inner class MyTimerTask : TimerTask() {
        override fun run() {
            activity?.runOnUiThread {
                bindingPlantInfo.editTextTextPlantName.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.textColor))
            }
        }
    }

    private fun bindUI() = launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {

            viewModel.getPlant(plant_id).observe(viewLifecycleOwner, Observer {
                it?.let { plant ->
                    thePlant = plant
                    bindingPlantInfo.apply {

                        editTextTextPlantName.setText(thePlant.name.toString())
                        editTextTextPlantDescription.setText(thePlant.desc.toString())

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

            Snackbar.make(requireView(), getString(R.string.deleted), Snackbar.LENGTH_SHORT).show()

            findNavController().navigateUp()
        }
    }

}