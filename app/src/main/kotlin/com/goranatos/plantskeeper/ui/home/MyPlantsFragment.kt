package com.goranatos.plantskeeper.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.*
import com.goranatos.plantskeeper.databinding.FragmentMyPlantsBinding
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance


class MyPlantsFragment : ScopedFragment(), DIAware {

    override val di by closestDI()

    private lateinit var viewModel: MyPlantsViewModel

    private val viewModelFactory: MyPlantsViewModelFactory by instance()

    lateinit var binding: FragmentMyPlantsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MyPlantsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_my_plants,
                container,
                false
            )

        createChannel(
            getString(R.string.plant_notification_channel_id),
            getString(R.string.plant_notification_channel_name)
        )

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.allPlants.observe(viewLifecycleOwner, {
            initRecycleView(it.toPlantItemCards())

            if (it.isNotEmpty()) binding.textViewEmptyDatabaseNotification.visibility = View.GONE
            else binding.textViewEmptyDatabaseNotification.visibility = View.VISIBLE

            viewModel.setNotificationsForPlantList(it)

        })

        viewModel.navigateToThePlant.observe(viewLifecycleOwner, {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(
                    MyPlantsFragmentDirections.actionMyPlantsFragmentToPlantAddAndInfo(viewModel.navigateToPlantId)
                )
                viewModel.doneNavigating()
            }
        })

        binding.fab.setOnClickListener { view ->


            viewModel.updateNavigateToPlantId(-1)
            viewModel.onItemClicked()
        }

        setHasOptionsMenu(true)
    }

    private fun initRecycleView(items: List<PlantItemCard>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(items)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }
    }

    private fun List<Plant>.toPlantItemCards(): List<PlantItemCard> {
        return this.map {
            PlantItemCard(it, onPlantItemCardClickedListener, onPlantItemCardLongClickedListener)
        }
    }

    private val onPlantItemCardClickedListener = object : OnPlantItemCardClickedListener {
        override fun onPlantItemCardClicked(id: Int) {

        }
    }

    private val onPlantItemCardLongClickedListener = object : OnPlantItemCardLongClickedListener {
        override fun onPlantItemCardLongClicked(id: Int, menuCode: Int) {
            when (menuCode){
                PlantItemCardMenu.EDIT_MENU.menuCode -> {
                    viewModel.updateNavigateToPlantId(id)
                    viewModel.onItemClicked()

                    Toast.makeText(context, "EditPlantMenuClicked", Toast.LENGTH_SHORT).show()
                }
                PlantItemCardMenu.DELETE_MENU.menuCode -> {
                    Toast.makeText(context, "DeletePlantMenuClicked", Toast.LENGTH_SHORT).show()
                    deletePlantItemFromDB(id)

                }
                PlantItemCardMenu.NOT_SELECTED.menuCode -> {Toast.makeText(context, id.toString(), Toast.LENGTH_SHORT).show()}
            }
        }
    }

    private fun deletePlantItemFromDB(plantId: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.delete_plant_from_db))
            .setMessage(resources.getString(R.string.are_you_sure_to_delete_the_plant_from_db))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
            }
            .setPositiveButton(resources.getString(R.string.delete_item)) { dialog, which ->
                launch(Dispatchers.IO) {
                    viewModel.deletePlantWithId(plantId)

                    Snackbar.make(requireView(), getString(R.string.deleted), Snackbar.LENGTH_SHORT)
                        .show()

                }
            }.show()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)

        val settingsOption = menu?.findItem(R.id.action_settings)

        settingsOption.setOnMenuItemClickListener {
            findNavController().navigate(
                MyPlantsFragmentDirections.actionMyPlantsFragmentToSettingsFragment()
            )
            true
        }

        return super.onCreateOptionsMenu(menu, inflater)
    }

}