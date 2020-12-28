package com.goranatos.plantskeeper.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.OnPlantItemCardClickedListener
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.entity.PlantItemCard
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.goranatos.plantskeeper.util.sendNotification
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_my_plants.*
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class MyPlantsFragment : ScopedFragment(), DIAware {

    override val di by closestDI()

    private lateinit var viewModel: MyPlantsViewModel

    private val viewModelFactory: MyPlantsViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MyPlantsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // TODO: Step 1.7 call create channel
        createChannel(
            getString(R.string.plant_notification_channel_id),
            getString(R.string.plant_notification_channel_name)
        )

        return inflater.inflate(R.layout.fragment_my_plants, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.allPlants.observe(viewLifecycleOwner, {
            initRecycleView(it.toPlantItemCards())
            if (it.isNotEmpty()) textViewEmptyDatabaseNotification.visibility = View.GONE
            else textViewEmptyDatabaseNotification.visibility = View.VISIBLE
        })

        viewModel.navigateToThePlant.observe(viewLifecycleOwner, {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(
                    MyPlantsFragmentDirections.actionMyPlantsFragmentToPlantAddAndInfo(viewModel.navigateToPlantId)
                )
                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                viewModel.doneNavigating()

            }
        })

        fab.setOnClickListener { view ->
            viewModel.updateNavigateToPlantId(-1)
            viewModel.onItemClicked()

            viewModel.setAlarm(true)
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
            viewModel.updateNavigateToPlantId(id)
            viewModel.onItemClicked()
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.plants_notification_description)

            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }
}