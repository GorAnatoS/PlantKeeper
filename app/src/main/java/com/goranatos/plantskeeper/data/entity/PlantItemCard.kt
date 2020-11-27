package com.goranatos.plantskeeper.data.entity

import com.goranatos.plantskeeper.R

import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_plant.view.*

/**
 * Created by qsufff on 9/13/2020.
 */

class PlantItemCard(private val content: Plant) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {


        viewHolder.apply {
            viewHolder.containerView.tvTitle.text = content.name
        }

    }

    override fun getLayout() = R.layout.item_plant
}