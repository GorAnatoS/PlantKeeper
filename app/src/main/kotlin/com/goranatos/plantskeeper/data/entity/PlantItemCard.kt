package com.goranatos.plantskeeper.data.entity

//import com.goranatos.plantskeeper.ui.home.MyPlantsFragmentDirections

import android.net.Uri
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.getColor
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.internal.TimeHelper
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_plant.view.*

/**
 * Created by qsufff on 9/13/2020.
 */

interface OnPlantItemCardClickedListener {
    fun onPlantItemCardClicked(id: Int)
}

interface OnPlantItemCardLongClickedListener {
    fun onPlantItemCardLongClicked(id: Int, menuCode: Int)
}

class PlantItemCard(
    private val content: Plant,
    private val plantItemCardListener: OnPlantItemCardClickedListener,
    private val plantItemCardLongListener: OnPlantItemCardLongClickedListener
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.apply {

            viewHolder.containerView.tvTitle.text = content.str_name

            if (content.string_uri_image_path.isNullOrEmpty()) {

            } else {
                viewHolder.containerView.imageViewPlant.setImageURI(Uri.parse(content.string_uri_image_path))
            }
            //при нажатии на карточку открываем подробное описание растения
            itemView.setOnClickListener {
                plantItemCardListener.onPlantItemCardClicked(content.int_id)
            }

            itemView.setOnLongClickListener {
                plantItemCardLongListener.onPlantItemCardLongClicked(content.int_id, PlantItemCardMenu.NOT_SELECTED.menuCode)

                val pop = PopupMenu(viewHolder.containerView.context, it)
                pop.inflate(R.menu.plant_menu_on_long_click)

                pop.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.context_menu_edit_plant -> {
                            plantItemCardLongListener.onPlantItemCardLongClicked(content.int_id, PlantItemCardMenu.EDIT_MENU.menuCode)
                        }
                        R.id.context_menu_delete_plant -> {
                            plantItemCardLongListener.onPlantItemCardLongClicked(content.int_id, PlantItemCardMenu.DELETE_MENU.menuCode)
                        }
                    }
                    true
                }
                pop.show()
                true
            }

            if (content.is_water_need_on == 1) {
                if (TimeHelper.getDaysTillEventNotification(System.currentTimeMillis(), content.long_next_watering_date!!) <= 0) viewHolder.containerView.plantCardView.setCardBackgroundColor(
                    getColor(viewHolder.containerView.context, R.color.card_attention_color)
                )
                else viewHolder.containerView.plantCardView.setCardBackgroundColor(
                    getColor(
                        viewHolder.containerView.context,
                        R.color.card_normal_color
                    )
                )

                viewHolder.containerView.tvTillWateringVal.text =
                    TimeHelper.getDaysTillEventNotification(System.currentTimeMillis(), content.long_next_watering_date!!).toString()
            } else {
                viewHolder.containerView.plantCardView.setCardBackgroundColor(
                    getColor(
                        viewHolder.containerView.context,
                        R.color.card_normal_color
                    )
                )

                viewHolder.containerView.tvTillWatering.visibility = View.GONE
                viewHolder.containerView.tvTillWateringVal.visibility = View.GONE
            }
        }
    }

    override fun getLayout() = R.layout.item_plant
}

enum class PlantItemCardMenu(val menuCode: Int){
    NOT_SELECTED(-1),
    EDIT_MENU(0),
    DELETE_MENU(1)
}