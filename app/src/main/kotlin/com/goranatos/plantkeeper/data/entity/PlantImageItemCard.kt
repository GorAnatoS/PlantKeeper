package com.goranatos.plantkeeper.data.entity

import android.net.Uri
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.databinding.ItemPlantImageBinding
import com.xwray.groupie.databinding.BindableItem

/**
 * Created by qsufff on 9/13/2020.
 */


interface OnPlantImageItemClickedListener {
    fun onPlantImageClicked(uri: Uri)
}


class PlantImageItemCard(
    private val uri: Uri,
    private val plantImageItemListener: OnPlantImageItemClickedListener
) : BindableItem<ItemPlantImageBinding>() {

    override fun bind(viewBinding: ItemPlantImageBinding, position: Int) {

        viewBinding.apply {

            viewBinding.imageViewPlant.setImageURI(uri)

            viewBinding.imageViewPlant.setOnClickListener {
                plantImageItemListener.onPlantImageClicked(uri)
            }
        }

    }

    override fun getLayout() = R.layout.item_plant_image
}