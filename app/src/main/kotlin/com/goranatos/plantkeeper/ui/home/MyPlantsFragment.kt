package com.goranatos.plantkeeper.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.data.entity.*
import com.goranatos.plantkeeper.databinding.FragmentMyPlantsBinding
import com.goranatos.plantkeeper.ui.base.ScopedFragment
import com.goranatos.plantkeeper.util.Helper.Companion.getScreenWidth
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.CoroutineScope
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

            PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(
                getString(R.string.pref_option_is_list_home_adapter), true
            ).also { isListHomeAdapter ->
//                if (isListHomeAdapter || calculateSpanCount() < 2) initRecycleViewWithLinearAdapter(it.toPlantListItemCards())
                if (isListHomeAdapter) initRecycleViewWithLinearAdapter(it.toPlantListItemCards())
                else
                    initRecycleViewWithGridAdapter(it.toPlantGridItemCards())
            }

            if (it.isNotEmpty()) binding.textViewEmptyDatabaseNotification.visibility = View.GONE
            else binding.textViewEmptyDatabaseNotification.visibility = View.VISIBLE
        })

        viewModel.navigateToThePlant.observe(viewLifecycleOwner, {
            if (it == true) { // Observed state is true.
                findNavController().navigate(
                    MyPlantsFragmentDirections.actionMyPlantsFragmentToPlantAddAndInfo(viewModel.navigateToPlantId)
                )
                viewModel.doneNavigating()
            }
        })

        binding.fab.setOnClickListener {
            viewModel.updateNavigateToPlantId(-1)
            viewModel.onItemClicked()
        }

        setHasOptionsMenu(true)
    }

    private fun initRecycleViewWithLinearAdapter(items: List<PlantItemListCard>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(items)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }
    }

    private fun initRecycleViewWithGridAdapter(items: List<PlantItemGridCard>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(items)
        }

        binding.recyclerView.apply {
            val spanCount = calculateSpanCount()
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = groupAdapter
        }
    }

    private fun calculateSpanCount(): Int {
        val density = resources.displayMetrics.density
        val dpWidth = getScreenWidth(requireActivity()) / density
        val columns = (dpWidth / (170 + 20)).toInt()
        return columns
    }

    private fun List<Plant>.toPlantListItemCards(): List<PlantItemListCard> {
        return this.map {
            PlantItemListCard(
                it,
                onPlantItemCardClickedListener,
                onPlantItemCardLongClickedListener
            )
        }
    }

    private fun List<Plant>.toPlantGridItemCards(): List<PlantItemGridCard> {
        return this.map {
            PlantItemGridCard(
                it,
                onPlantItemCardClickedListener,
                onPlantItemCardLongClickedListener
            )
        }
    }

    private val onPlantItemCardClickedListener = object : OnPlantItemCardClickedListener {
        override fun onPlantItemCardClicked(id: Int) {
            launch {
                viewModel.setPlant(id)
            }
            val fragmentManager = parentFragmentManager
            val newFragment = PlantInfoFragmentDialog(viewModel)
            newFragment.show(fragmentManager, "dialog")
        }
    }

    private val onPlantItemCardLongClickedListener = object : OnPlantItemCardLongClickedListener {
        override fun onPlantItemCardLongClicked(id: Int, menuCode: Int) {
            when (menuCode) {
                PlantItemCardMenu.EDIT_MENU.menuCode -> {
                    viewModel.updateNavigateToPlantId(id)
                    viewModel.onItemClicked()
                }
                PlantItemCardMenu.DELETE_MENU.menuCode -> {
                    deletePlantItemFromDB(
                        id,
                        requireContext(),
                        viewModel.viewModelScope,
                        viewModel,
                        requireView()
                    )
                }
                PlantItemCardMenu.NOT_SELECTED.menuCode -> {
                }
            }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.my_plants_menu, menu)

        val settingsOption = menu.findItem(R.id.action_option)

        settingsOption.setOnMenuItemClickListener {
            findNavController().navigate(
                MyPlantsFragmentDirections.actionMyPlantsFragmentToSettingsFragment()
            )
            true
        }

        val changeAppearanceOption = menu.findItem(R.id.action_change_appearance)

        changeAppearanceOption.setOnMenuItemClickListener {

            PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(
                getString(R.string.pref_option_is_list_home_adapter), true
            ).also { isListHomeAdapter ->

                viewModel.allPlants.value?.let {
                    //if ( calculateSpanCount() > 1) {
                    if (true) {
                        val pmEditor =
                            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()

                        if (isListHomeAdapter) {
                            initRecycleViewWithGridAdapter(it.toPlantGridItemCards())
                            pmEditor.putBoolean(
                                getString(R.string.pref_option_is_list_home_adapter),
                                false
                            ).apply()

                        } else {
                            initRecycleViewWithLinearAdapter(it.toPlantListItemCards())
                            pmEditor.putBoolean(
                                getString(R.string.pref_option_is_list_home_adapter),
                                true
                            ).apply()
                        }
                    }
                }

            }

            true
        }

        return super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {
        fun deletePlantItemFromDB(
            plantId: Int,
            context: Context,
            viewModelScope: CoroutineScope,
            viewModel: MyPlantsViewModel,
            view: View
        ) {
            MaterialAlertDialogBuilder(context)
                .setTitle(context.resources.getString(R.string.delete_plant_from_db))
                .setMessage(context.resources.getString(R.string.are_you_sure_to_delete_the_plant_from_db))
                .setNeutralButton(context.resources.getString(R.string.cancel)) { _, _ ->
                }
                .setPositiveButton(context.resources.getString(R.string.delete_item)) { _, _ ->
                    viewModelScope.launch(Dispatchers.IO) {
                        viewModel.deletePlantWithId(plantId)

                        Snackbar.make(
                            view,
                            context.getString(R.string.deleted),
                            Snackbar.LENGTH_SHORT
                        )
                            .show()

                    }
                }.show()
        }
    }
}