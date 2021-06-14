package com.goranatos.plantkeeper.ui.todo

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.data.entity.*
import com.goranatos.plantkeeper.databinding.FragmentTodoBinding
import com.goranatos.plantkeeper.ui.base.ScopedFragment
import com.goranatos.plantkeeper.ui.myplants.MyPlantsFragment
import com.goranatos.plantkeeper.ui.myplants.MyPlantsFragment.Companion.calculateSpanCount
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@AndroidEntryPoint
class TodoFragment : ScopedFragment() {
    private val viewModel by viewModels<TodoViewModel>()

    lateinit var binding: FragmentTodoBinding

    private var selectedDate: Int = -1

    private lateinit var changeAppearanceActionMenuItem: MenuItem

    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initTodoViewModel()
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
                R.layout.fragment_todo,
                container,
                false
            )

        setOnChipClickListener()

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.todo_menu, menu)

        changeAppearanceActionMenuItem = menu.findItem(R.id.action_change_appearance)

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
                } else {
                    changeAppearanceActionMenuItem
                        .setIcon(R.drawable.ic_baseline_view_agenda_24)

                    pmEditor.putBoolean(
                        getString(R.string.pref_option_is_list_home_adapter),
                        true
                    ).apply()
                }

                viewModel.updateCurrentIntDateWhen()
            }

            true
        }

        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setOnChipClickListener() {
        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            val localeDate = when (getString(R.string.current_lang)) {
                getString(R.string.spanish_lang) -> {
                    // TODO: 5/13/2021  
                    SimpleDateFormat("MMMM dd", Locale.forLanguageTag("es-ES"))
                }
                getString(R.string.russian_lang) -> {
                    SimpleDateFormat("d MMM", Locale(getString(R.string.ru)))
                }
                getString(R.string.english_lang) -> {
                    SimpleDateFormat("MMM d", Locale.ENGLISH)
                }
                else -> {
                    SimpleDateFormat("MMM d", Locale.ENGLISH)
                }
            }

            when (checkedId) {

                binding.chipToday.id -> {
                    selectedDate = SELECTED_DATE.TODAY.code
                    binding.tvWhen.visibility = View.VISIBLE
                    binding.tvWhen.text = localeDate.format(Date())
                    viewModel.setIntDateWhen(DateWhen.TODAY.code)
                    viewModel.setDateInMls(Calendar.getInstance().timeInMillis)
                }
                binding.chipTomorrow.id -> {
                    selectedDate = SELECTED_DATE.TOMORROW.code
                    binding.tvWhen.visibility = View.VISIBLE
                    val locDate = LocalDate.now().plusDays(1)
                    binding.tvWhen.text = localeDate.format(
                        Date.from(
                            locDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                        )
                    )
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    viewModel.setIntDateWhen(DateWhen.TOMORROW.code)
                    viewModel.setDateInMls(cal.timeInMillis)
                    binding.fab.visibility = View.GONE

                }
                binding.chipWeek.id -> {
                    selectedDate = SELECTED_DATE.WEEK.code
                    binding.tvWhen.visibility = View.VISIBLE
                    // TODO: 5/12/2021 - 
                    binding.tvWhen.text =
                        localeDate.format(
                            Date.from(
                                LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
                            )
                        ) + " - " + localeDate.format(
                            Date.from(
                                LocalDate.now().plusDays(7).atStartOfDay(
                                    ZoneId.systemDefault()
                                ).toInstant()
                            )
                        )
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, 7)
                    viewModel.setIntDateWhen(DateWhen.WEEK.code)
                    viewModel.setDateInMls(cal.timeInMillis)
                    binding.fab.visibility = View.GONE
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chipToday.isChecked = true

        viewModel.allPlants.observe(viewLifecycleOwner, {
            viewModel.dateInMls.observe(viewLifecycleOwner, {
                viewModel.getMatchPlantList().let { plantList ->
                    if (plantList.isNotEmpty()) {
                        preferences.getBoolean(
                            getString(R.string.pref_option_is_list_home_adapter), true
                        ).also { isListHomeAdapter ->
                            if (calculateSpanCount(resources, requireActivity()) > 1) {
                                if (isListHomeAdapter) {
                                    initRecycleViewWithLinearAdapterForTodoPlantCard(
                                        plantList.toTodoListItemPlantCards(),
                                        binding.recyclerView
                                    )
                                } else {
                                    initRecycleViewWithGridAdapterForTodoPlantCard(
                                        plantList.toTodoGridItemPlantCards(),
                                        binding.recyclerView,
                                        requireActivity()
                                    )
                                }
                            }
                        }
                        binding.noPlantsToWaterTextView.visibility = View.GONE
                        if (viewModel.dateWhenIntCode == DateWhen.TODAY.code) binding.fab.visibility =
                            View.VISIBLE
                    } else {
                        binding.apply {
                            recyclerView.adapter = null
                            fab.visibility = View.GONE
                            noPlantsToWaterTextView.visibility = View.VISIBLE
                        }
                    }
                }
            })
        })

        binding.fab.setOnClickListener {
            viewModel.completePlantTasks()

            Snackbar.make(
                requireView(),
                getString(R.string.today_tasks_completed),
                Snackbar.LENGTH_SHORT
            ).show()

        }
    }

    private fun List<Plant>.toTodoListItemPlantCards(): List<PlantItemLinearTodoCard> {
        return this.map {
            PlantItemLinearTodoCard(
                it,
                todoOnPlantItemCardClickedListener,
                if (selectedDate == SELECTED_DATE.TODAY.code) todoOnPlantItemCardLongClickedListener else null,
            )
        }
    }

    private fun List<Plant>.toTodoGridItemPlantCards(): List<PlantItemGridTodoCard> {
        return this.map {
            PlantItemGridTodoCard(
                it,
                todoOnPlantItemCardClickedListener,
                if (selectedDate == SELECTED_DATE.TODAY.code) todoOnPlantItemCardLongClickedListener else null,
            )
        }
    }

    private val todoOnPlantItemCardClickedListener = object : TodoOnPlantItemCardClickedListener {
        override fun onPlantItemCardClicked(id: Int) {
            findNavController().navigate(
                TodoFragmentDirections.actionNavigationTodoToPlantInfoFragmentDialog(id)
            )
        }
    }

    private val todoOnPlantItemCardLongClickedListener =
        object : TodoOnPlantItemCardLongClickedListener {
            override fun onPlantItemCardLongClicked(id: Int, menuCode: Int) {
                when (menuCode) {
                    TodoPlantItemCardMenu.WATERED_MENU.menuCode -> {
                        launch(Dispatchers.IO) {
                            viewModel.updateWateredData(id)
                        }
                    }
                    TodoPlantItemCardMenu.FERTILIZED_MENU.menuCode -> {
                        launch(Dispatchers.IO) {
                            viewModel.updateFertilizedData(id)
                        }
                    }
                    TodoPlantItemCardMenu.NOT_SELECTED.menuCode -> {
                    }
                }
            }
        }

    enum class SELECTED_DATE(val code: Int) {
        TODAY(0),
        TOMORROW(1),
        WEEK(2)
    }

    companion object {
        fun initRecycleViewWithLinearAdapterForTodoPlantCard(
            itemLinears: List<PlantItemLinearTodoCard>,
            recyclerView: RecyclerView
        ) {
            val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
                addAll(itemLinears)
            }

            recyclerView.apply {
                layoutManager = LinearLayoutManager(recyclerView.context)
                adapter = groupAdapter
                MyPlantsFragment.runRecycleViewAnimation(this, R.anim.grid_to_list_layout_animation)
            }
        }

        fun initRecycleViewWithGridAdapterForTodoPlantCard(
            items: List<PlantItemGridTodoCard>,
            recyclerView: RecyclerView,
            activity: Activity
        ) {
            val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
                addAll(items)
            }

            recyclerView.apply {
                val spanCount = calculateSpanCount(resources, activity)
                layoutManager = GridLayoutManager(recyclerView.context, spanCount)
                adapter = groupAdapter
                MyPlantsFragment.runRecycleViewAnimation(this, R.anim.list_to_grid_layout_animation)
            }
        }
    }
}
// TODO: 5/12/2021 menu -> change on its own 