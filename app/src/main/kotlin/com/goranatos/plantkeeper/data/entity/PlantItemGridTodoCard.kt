package com.goranatos.plantkeeper.data.entity

import android.net.Uri
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.databinding.GridItemPlantBinding
import com.goranatos.plantkeeper.utilities.PlantHelper
import com.goranatos.plantkeeper.utilities.TimeHelper
import com.xwray.groupie.databinding.BindableItem

/**
 * Created by qsufff on 9/13/2020.
 */


class PlantItemGridTodoCard(
    private val content: Plant,
    private val todoOnPlantItemCardListener: TodoOnPlantItemCardClickedListener?,
    private val todoOnPlantItemCardLongListener: TodoOnPlantItemCardLongClickedListener?
) : BindableItem<GridItemPlantBinding>() {

    override fun bind(viewBinding: GridItemPlantBinding, position: Int) {

        viewBinding.apply {

            viewBinding.tvTitle.text = content.str_name

            if (content.string_uri_image_path.isNullOrEmpty()) {

            } else {
                viewBinding.imageViewPlant.setImageURI(Uri.parse(content.string_uri_image_path))
                Glide
                    .with(viewBinding.plantCardView)
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
//                viewHolder.containerView.tvTillWatering.visibility = View.GONE
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
//                viewHolder.containerView.tvTillFertilizing.visibility = View.GONE
                viewBinding.tvTillFertilizingVal.visibility = View.GONE
            }

            if (colorNormal) {
                viewBinding.plantCardView.setCardBackgroundColor(
                    MaterialColors.getColor(
                        viewBinding.plantCardView.context,
                        R.attr.card_view_bg_normal,
                        "Error"
                    )
                )
            } else {
                viewBinding.plantCardView.setCardBackgroundColor(
                    MaterialColors.getColor(
                        viewBinding.plantCardView.context,
                        R.attr.card_view_bg_need_care,
                        "Error"
                    )
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

    override fun getLayout() = R.layout.grid_item_plant
}