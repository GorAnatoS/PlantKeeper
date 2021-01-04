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


interface OnPlantImageItemClickedListener {
    fun onPlantImageClicked(uri: Uri)
}


class PlantImageItemCard(private val uri: Uri, val plantImageItemListener: OnPlantImageItemClickedListener) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.apply {

            viewHolder.itemView.imageViewPlant.setImageURI(uri)

            itemView.setOnClickListener{
                plantImageItemListener.onPlantImageClicked(uri)
            }
        }

    }

    override fun getLayout() = R.layout.item_plant_image
}