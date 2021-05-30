package com.goranatos.plantkeeper.data.entity

import android.net.Uri
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.getColor
import com.bumptech.glide.Glide
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.databinding.GridItemPlantBinding
import com.goranatos.plantkeeper.utilities.TimeHelper
import com.xwray.groupie.databinding.BindableItem

/**
 * Created by qsufff on 9/13/2020.
 */


class PlantItemGridCard(
    private val content: Plant,
    private val plantItemCardListener: OnPlantItemCardClickedListener,
    private val plantItemCardLongListener: OnPlantItemCardLongClickedListener
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
            //при нажатии на карточку открываем подробное описание растения
            viewBinding.plantCardView.setOnClickListener {
                plantItemCardListener.onPlantItemCardClicked(content.int_id)
            }

            viewBinding.plantCardView.setOnLongClickListener {
                plantItemCardLongListener.onPlantItemCardLongClicked(
                    content.int_id,
                    PlantItemCardMenu.NOT_SELECTED.menuCode
                )

                val pop = PopupMenu(viewBinding.plantCardView.context, it)
                pop.inflate(R.menu.my_plants_on_plant_long_clicked_menu)

                pop.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.context_menu_edit_plant -> {
                            plantItemCardLongListener.onPlantItemCardLongClicked(
                                content.int_id,
                                PlantItemCardMenu.EDIT_MENU.menuCode
                            )
                        }
                        R.id.context_menu_delete_plant -> {
                            plantItemCardLongListener.onPlantItemCardLongClicked(
                                content.int_id,
                                PlantItemCardMenu.DELETE_MENU.menuCode
                            )
                        }
                    }
                    true
                }
                pop.show()
                true
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
//                viewHolder.containerView.plantCardView.setCardBackgroundColor(
//                    getColor(viewHolder.containerView.context, R.color.card_normal)
//                )
            } else {
                viewBinding.plantCardView.setCardBackgroundColor(
                    getColor(viewBinding.plantCardView.context, R.color.card_view_bg_attention)
                )
            }
        }
    }

    override fun getLayout() = R.layout.grid_item_plant
}