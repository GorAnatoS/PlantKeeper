package com.goranatos.plantskeeper.data.entity

import android.net.Uri
import com.goranatos.plantskeeper.R
//import com.goranatos.plantskeeper.ui.home.MyPlantsFragmentDirections

import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_plant.view.*

/**
 * Created by qsufff on 9/13/2020.
 */

interface OnPlantItemCardClickedListener {
    fun onPlantItemCardClicked(id: Int)
}

class PlantItemCard(private val content: Plant, val plantItemCardListener: OnPlantItemCardClickedListener) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.apply {
            viewHolder.containerView.tvTitle.text = content.str_name

            if (content.string_uri_image_path.isNullOrEmpty()){

            } else {
                viewHolder.containerView.imageViewPlant.setImageURI(Uri.parse(content.string_uri_image_path))
            }
            //при нажатии на карточку открываем подробное описание растения
            itemView.setOnClickListener{
                plantItemCardListener.onPlantItemCardClicked(content.int_id)
            }
        }

    }

    override fun getLayout() = R.layout.item_plant
}