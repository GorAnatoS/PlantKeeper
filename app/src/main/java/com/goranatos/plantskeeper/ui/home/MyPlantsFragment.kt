package com.goranatos.plantskeeper.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.OnPlantItemCardClickedListener
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.entity.PlantItemCard
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_my_plants.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

        viewModel.allPlants.observe(viewLifecycleOwner, {
            initRecycleView(it.toPlantItemCards())
            if (it.isNotEmpty()) textViewEmptyDatabaseNotification.visibility = View.GONE
            else textViewEmptyDatabaseNotification.visibility = View.VISIBLE
        })

        viewModel.navigateToThePlant.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(
                    MyPlantsFragmentDirections.actionMyPlantsFragmentToPlantAddAndInfo(viewModel.navigateToPlantId))
                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                viewModel.doneNavigating()

            }
        })

        fab.setOnClickListener { view ->
            viewModel.navigateToPlantId = -1
            viewModel.onItemClicked()
        }
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
            PlantItemCard(it, onPlantItemCardClickedListener)
        }
    }

    private val onPlantItemCardClickedListener = object : OnPlantItemCardClickedListener {
        override fun onPlantItemCardClicked(id: Int) {
            viewModel.navigateToPlantId = id
            viewModel.onItemClicked()
        }
    }
}