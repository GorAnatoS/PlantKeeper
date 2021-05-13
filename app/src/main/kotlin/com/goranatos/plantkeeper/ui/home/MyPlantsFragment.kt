package com.goranatos.plantkeeper.ui.home

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.data.entity.*
import com.goranatos.plantkeeper.databinding.FragmentMyPlantsBinding
import com.goranatos.plantkeeper.ui.base.ScopedFragment
import com.goranatos.plantkeeper.utilities.Helper.Companion.getScreenWidth
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

    private lateinit var changeAppearanceActionMenuItem: MenuItem

    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MyPlantsViewModel::class.java)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
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
            getString(R.string.plant_notification_channel_name),
            requireActivity()
        )

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.allPlants.observe(viewLifecycleOwner, {
            viewModel.updateRecycleView()
        })

        viewModel.navigateToThePlant.observe(viewLifecycleOwner, {
            if (it == true) { // Observed state is true.
                findNavController().navigate(
                    MyPlantsFragmentDirections.actionMyPlantsFragmentToPlantAddAndInfo(viewModel.navigateToPlantId)
                )
                viewModel.doneNavigating()
            }
        })

        viewModel.isToUpdateRecycleView.observe(viewLifecycleOwner, {
            if (it == true) {

                PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(
                    getString(R.string.pref_option_is_list_home_adapter), true
                ).also { isListHomeAdapter ->
                    if (isListHomeAdapter || calculateSpanCount(resources, requireActivity()) < 2)
                        initRecycleViewWithLinearAdapter(
                            viewModel.allPlants.value?.toPlantListItemCards(), binding.recyclerView
                        )
                    else
                        initRecycleViewWithGridAdapter(
                            viewModel.allPlants.value?.toPlantGridItemCards(),
                            binding.recyclerView,
                            requireActivity()
                        )
                }

                if (viewModel.allPlants.value?.isNotEmpty() == true) {
                    binding.textViewEmptyDatabaseNotification.visibility = View.GONE
                    runRecycleViewAnimation(binding.recyclerView, R.anim.app_start_layout_animation)
                } else binding.textViewEmptyDatabaseNotification.visibility = View.VISIBLE
            }
        })

        binding.fab.setOnClickListener {
            viewModel.updateNavigateToPlantId(-1)
            viewModel.onItemClicked()
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

        changeAppearanceActionMenuItem = menu.findItem(R.id.action_change_appearance)

        if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(
                getString(R.string.pref_option_is_list_home_adapter), true
            )
        ) menu.findItem(R.id.action_change_appearance)
            .setIcon(R.drawable.ic_baseline_view_agenda_24)
        else menu.findItem(R.id.action_change_appearance)
            .setIcon(R.drawable.ic_baseline_dashboard_24)

        changeAppearanceActionMenuItem.setOnMenuItemClickListener {

            preferences.getBoolean(
                getString(R.string.pref_option_is_list_home_adapter), true
            ).also { isListHomeAdapter ->

                val pmEditor =
                    preferences.edit()

                if (isListHomeAdapter) {
                    changeAppearanceActionMenuItem
                        .setIcon(R.drawable.ic_baseline_dashboard_24)

                    pmEditor.putBoolean(
                        getString(R.string.pref_option_is_list_home_adapter),
                        false
                    ).apply()

                    viewModel.updateRecycleView()

                } else {
                    changeAppearanceActionMenuItem
                        .setIcon(R.drawable.ic_baseline_view_agenda_24)

                    pmEditor.putBoolean(
                        getString(R.string.pref_option_is_list_home_adapter),
                        true
                    ).apply()

                    viewModel.updateRecycleView()
                }
            }

            true
        }

        return super.onCreateOptionsMenu(menu, inflater)
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

    companion object {
        private fun createChannel(channelId: String, channelName: String, activity: Activity) {
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
                notificationChannel.description =
                    activity.getString(R.string.plants_notification_description)

                val notificationManager = activity.getSystemService(
                    NotificationManager::class.java
                )
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

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

        fun calculateSpanCount(resources: Resources, activity: Activity): Int {
            val density = resources.displayMetrics.density
            val dpWidth = getScreenWidth(activity) / density
            val columns = (dpWidth / (170 + 20)).toInt()
            return columns
        }

        fun initRecycleViewWithGridAdapter(
            items: List<PlantItemGridCard>?,
            recyclerView: RecyclerView,
            activity: Activity
        ) {
            items?.let {
                val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
                    addAll(items)
                }

                recyclerView.apply {
                    val spanCount = calculateSpanCount(resources, activity)
                    layoutManager = GridLayoutManager(recyclerView.context, spanCount)
                    adapter = groupAdapter
                    runRecycleViewAnimation(this, R.anim.list_to_grid_layout_animation)
                }
            }
        }

        fun initRecycleViewWithLinearAdapter(
            items: List<PlantItemListCard>?,
            recyclerView: RecyclerView
        ) {
            items?.let {
                val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
                    addAll(items)
                }

                recyclerView.apply {
                    layoutManager = LinearLayoutManager(recyclerView.context)
                    adapter = groupAdapter
                    runRecycleViewAnimation(this, R.anim.grid_to_list_layout_animation)
                }
            }

        }

        fun runRecycleViewAnimation(recyclerView: RecyclerView, animationIntId: Int) {
            val context = recyclerView.context
            val controller =
                AnimationUtils.loadLayoutAnimation(context, animationIntId)
            recyclerView.layoutAnimation = controller
            recyclerView.adapter!!.notifyDataSetChanged()
            recyclerView.scheduleLayoutAnimation()
        }
    }
}