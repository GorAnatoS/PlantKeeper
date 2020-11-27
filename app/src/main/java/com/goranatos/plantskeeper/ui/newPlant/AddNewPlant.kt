package com.goranatos.plantskeeper.ui.newPlant

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.databinding.FragmentAddNewPlantBinding
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.goranatos.plantskeeper.ui.home.MyPlantsFragment.Companion.IS_ADDED_NEW_PLANT
import com.goranatos.plantskeeper.ui.home.MyPlantsFragment.Companion.uiScope
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModel
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModelFactory
import com.goranatos.plantskeeper.util.Helper.Companion.hideKeyboard
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance


class AddNewPlant : ScopedFragment(), DIAware {



    override val di by closestDI()

    private lateinit var viewModel: MyPlantsViewModel
    private val viewModelFactory: MyPlantsViewModelFactory by instance()

    lateinit var bindingAddNewPlantBinding: FragmentAddNewPlantBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory).get(MyPlantsViewModel::class.java)



    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        bindingAddNewPlantBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_new_plant, container, false)

        bindingAddNewPlantBinding.buttonAdd.setOnClickListener {
            uiScope.launch {

                bindingAddNewPlantBinding.apply {
                    val newPlant: Plant = Plant(
                        0,
                        editTextTextPlantName.text.toString(),
                        editTextTextPlantDescription.text.toString()
                    )

                    viewModel.insertPlant(newPlant)

                }

                findNavController().previousBackStackEntry?.savedStateHandle?.set(IS_ADDED_NEW_PLANT, true)

                hideKeyboard()

                Snackbar.make(requireView(), getString(R.string.added), Snackbar.LENGTH_SHORT).show()



                findNavController().navigateUp()
            }
        }


        return bindingAddNewPlantBinding.root
    }


}