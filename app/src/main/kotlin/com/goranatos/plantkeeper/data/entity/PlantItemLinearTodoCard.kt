package com.goranatos.plantkeeper.data.entity

import android.net.Uri
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.getColor
import com.bumptech.glide.Glide
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.databinding.ListItemPlantBinding
import com.goranatos.plantkeeper.utilities.PlantHelper
import com.goranatos.plantkeeper.utilities.TimeHelper
import com.xwray.groupie.databinding.BindableItem

/**
 * Created by qsufff on 9/13/2020.
 */


interface TodoOnPlantItemCardClickedListener {
    fun onPlantItemCardClicked(id: Int)
}


interface TodoOnPlantItemCardLongClickedListener {
    fun onPlantItemCardLongClicked(id: Int, menuCode: Int)
}

class PlantItemLinearTodoCard(
    private val content: Plant,
    private val todoOnPlantItemCardListener: TodoOnPlantItemCardClickedListener?,
    private val todoOnPlantItemCardLongListener: TodoOnPlantItemCardLongClickedListener?
) : BindableItem<ListItemPlantBinding>() {


    override fun bind(viewBinding: ListItemPlantBinding, position: Int) {

        viewBinding.apply {

            viewBinding.tvTitle.text = content.str_name

            if (content.string_uri_image_path.isNullOrEmpty()) {

            } else {
                viewBinding.imageViewPlant.setImageURI(Uri.parse(content.string_uri_image_path))
                Glide
                    .with(viewBinding.imageViewPlant)
                    .load(Uri.parse(content.string_uri_image_path))
                    //.placeholder(R.drawable.loading_spinner)
                    .into(viewBinding.imageViewPlant)
            }

            var colorNormal = true
            if (content.is_water_need_on == 1) {
                if (TimeHelper.getDaysTillEventNotification(
                        System.currentTimeMillis(),
                        content.long_next_watering_date!!
                    ) <= 0
                )
                    colorNormal = false

                viewBinding.tvTillWateringVal.text =
                    viewBinding.plantCardView.context.getString(
                        R.string.days_till_event, TimeHelper.getDaysTillEventNotification(
                            System.currentTimeMillis(),
                            content.long_next_watering_date!!
                        ).toString()
                    )
            } else {
                viewBinding.tvTillWatering.visibility = View.GONE
                viewBinding.tvTillWateringVal.visibility = View.GONE
            }

            if (content.is_fertilize_need_on == 1) {
                if (TimeHelper.getDaysTillEventNotification(
                        System.currentTimeMillis(),
                        content.long_next_fertilizing_date!!
                    ) <= 0
                )
                    colorNormal = false

                viewBinding.tvTillFertilizingVal.text =
                    viewBinding.plantCardView.context.getString(
                        R.string.days_till_event, TimeHelper.getDaysTillEventNotification(
                            System.currentTimeMillis(),
                            content.long_next_fertilizing_date!!
                        ).toString()
                    )
            } else {
                viewBinding.tvTillFertilizing.visibility = View.GONE
                viewBinding.tvTillFertilizingVal.visibility = View.GONE
            }

            if (colorNormal) {
//                viewHolder.containerView.plantCardView.setCardBackgroundColor(
//                    getColor(viewHolder.containerView.context, R.color.card_normal)
//                )
            } else {
                viewBinding.plantCardView.setCardBackgroundColor(
                    getColor(viewBinding.plantCardView.context, R.color.card_view_bg_attention)
                )
            }

            if (todoOnPlantItemCardListener != null) {
                viewBinding.plantCardView.setOnClickListener {
                    todoOnPlantItemCardListener.onPlantItemCardClicked(content.int_id)
                }
            }

            todoOnPlantItemCardLongListener?.let {
                viewBinding.plantCardView.setOnLongClickListener {
                    todoOnPlantItemCardLongListener.onPlantItemCardLongClicked(
                        content.int_id,
                        TodoPlantItemCardMenu.NOT_SELECTED.menuCode
                    )

                    val pop = PopupMenu(viewBinding.plantCardView.context, it)
                    pop.inflate(R.menu.todo_on_plant_long_clicked_menu)

                    pop.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.context_menu_watered -> {
                                todoOnPlantItemCardLongListener.onPlantItemCardLongClicked(
                                    content.int_id,
                                    TodoPlantItemCardMenu.WATERED_MENU.menuCode
                                )
                            }
                            R.id.context_menu_fertilized -> {
                                todoOnPlantItemCardLongListener.onPlantItemCardLongClicked(
                                    content.int_id,
                                    TodoPlantItemCardMenu.FERTILIZED_MENU.menuCode
                                )
                            }
                        }
                        true
                    }

                    if (!PlantHelper.isWaterTodayNeeded(content)) pop.menu.findItem(R.id.context_menu_watered).isVisible =
                        false
                    if (!PlantHelper.isFertilizeTodayNeeded(content)) pop.menu.findItem(R.id.context_menu_fertilized).isVisible =
                        false

                    pop.show()
                    true
                }
            }
        }
    }

    override fun getLayout() = R.layout.list_item_plant
}

enum class TodoPlantItemCardMenu(val menuCode: Int) {
    NOT_SELECTED(-1),
    WATERED_MENU(0),
    FERTILIZED_MENU(1)
}