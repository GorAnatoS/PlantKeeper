package com.goranatos.plantskeeper.data.entity

import androidx.navigation.findNavController
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.ui.home.MyPlantsFragmentDirections
//import com.goranatos.plantskeeper.ui.home.MyPlantsFragmentDirections

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

            //при нажатии на карточку открываем подробное описание растения
            itemView.setOnClickListener{
                it.findNavController().navigate(MyPlantsFragmentDirections.actionMyPlantsFragmentToPlantAddAndInfo(content.id))
            }
        }

    }

    override fun getLayout() = R.layout.item_plant
}