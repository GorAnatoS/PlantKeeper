package com.goranatos.plantkeeper.data.entity

import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat.getColor
import com.bumptech.glide.Glide
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.internal.TimeHelper
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.list_item_plant.view.*

/**
 * Created by qsufff on 9/13/2020.
 */


class PlantItemGridTodoCard(
    private val content: Plant
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.apply {

            viewHolder.containerView.tvTitle.text = content.str_name

            if (content.string_uri_image_path.isNullOrEmpty()) {

            } else {
                viewHolder.containerView.imageViewPlant.setImageURI(Uri.parse(content.string_uri_image_path))
                Glide
                    .with(viewHolder.containerView)
                    .load(Uri.parse(content.string_uri_image_path))
                    //.placeholder(R.drawable.loading_spinner)
                    .into(viewHolder.containerView.imageViewPlant)
            }


            var colorNormal = true
            if (content.is_water_need_on == 1) {
                if (TimeHelper.getDaysTillEventNotification(
                        System.currentTimeMillis(),
                        content.long_next_watering_date!!
                    ) <= 0
                )
                    colorNormal = false

                viewHolder.containerView.tvTillWateringVal.text =
                    containerView.context.getString(
                        R.string.days_till_event, TimeHelper.getDaysTillEventNotification(
                            System.currentTimeMillis(),
                            content.long_next_watering_date!!
                        ).toString()
                    )
            } else {
//                viewHolder.containerView.tvTillWatering.visibility = View.GONE
                viewHolder.containerView.tvTillWateringVal.visibility = View.GONE
            }

            if (content.is_fertilize_need_on == 1) {
                if (TimeHelper.getDaysTillEventNotification(
                        System.currentTimeMillis(),
                        content.long_next_fertilizing_date!!
                    ) <= 0
                )
                    colorNormal = false

                viewHolder.containerView.tvTillFertilizingVal.text =
                    containerView.context.getString(
                        R.string.days_till_event, TimeHelper.getDaysTillEventNotification(
                            System.currentTimeMillis(),
                            content.long_next_fertilizing_date!!
                        ).toString()
                    )
            } else {
//                viewHolder.containerView.tvTillFertilizing.visibility = View.GONE
                viewHolder.containerView.tvTillFertilizingVal.visibility = View.GONE
            }

            if (colorNormal) {
                viewHolder.containerView.plantCardView.setCardBackgroundColor(
                    getColor(viewHolder.containerView.context, R.color.card_normal)
                )
            } else {
                viewHolder.containerView.plantCardView.setCardBackgroundColor(
                    getColor(viewHolder.containerView.context, R.color.card_attention)
                )
            }
        }
    }

    override fun getLayout() = R.layout.grid_item_plant
}