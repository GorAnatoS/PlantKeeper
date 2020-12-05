package com.goranatos.plantskeeper.ui.home


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.entity.PlantItemCard
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.goranatos.plantskeeper.ui.home.plantAddAndInfo.PlantAddAndInfo
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.fragment_my_plants.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class MyPlantsFragment : ScopedFragment(), DIAware {

    override val di by closestDI()
    private lateinit var viewModel: MyPlantsViewModel
    private val viewModelFactory: MyPlantsViewModelFactory by instance()

    companion object {
        var viewModelJob = Job()
        val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory).get(MyPlantsViewModel::class.java)

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.fragment_my_plants, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.allPlants.observe(viewLifecycleOwner, Observer {
            initRecycleView(it.toPlantItemCards())
            if (it.isNotEmpty()) textViewEmptyDatabaseNotification.visibility = View.GONE
            else textViewEmptyDatabaseNotification.visibility = View.VISIBLE
        })

        fab.setOnClickListener { view ->
            val action =
                    MyPlantsFragmentDirections
                            .actionMyPlantsFragmentToPlantAddAndInfo()
            view.findNavController().navigate(action)
        }

        bindUI()
    }

    private fun bindUI() = launch {

    }

    private fun initRecycleView(items: List<PlantItemCard>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(items)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }
    }

    private fun List<Plant>.toPlantItemCards(): List<PlantItemCard> {
        return this.map {
            PlantItemCard(it)
        }
    }

}
