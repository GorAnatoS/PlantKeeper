package com.goranatos.plantkeeper.utilities

import android.content.Context
import android.content.DialogInterface
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.data.entity.Plant


/**
 * Created by qsufff on 5/21/2021.
 */
class PlantHelper {
    companion object {

        fun isWaterTodayNeeded(plant: Plant): Boolean {
            return plant.is_water_need_on == 1 && TimeHelper.getDaysTillEventNotification(
                System.currentTimeMillis(),
                plant.long_next_watering_date!!
            ) <= 0
        }


        fun isFertilizeTodayNeeded(plant: Plant): Boolean {
            return plant.is_fertilize_need_on == 1 && TimeHelper.getDaysTillEventNotification(
                System.currentTimeMillis(),
                plant.long_next_fertilizing_date!!
            ) <= 0
        }

        fun deletePlantItemFromDB(
            context: Context,
            actionOnDelete: () -> Unit,
            view: View
        ) {
            MaterialAlertDialogBuilder(context)
                .setTitle(context.resources.getString(R.string.delete_plant_from_db))
                .setMessage(context.resources.getString(R.string.are_you_sure_to_delete_the_plant_from_db))
                .setNeutralButton(context.resources.getString(R.string.cancel)) { _, _ ->

                }
                .setPositiveButton(
                    context.resources.getString(R.string.delete_item),
                    DialogInterface.OnClickListener { dialog, which ->

                        actionOnDelete()

                        //Do nothing here because we override this button later to change the close behaviour.
                        //However, we still need this because on older versions of Android unless we
                        //pass a handler the button doesn't get instantiated


                        Snackbar.make(
                            view,
                            context.getString(R.string.deleted),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    })
                .show()
        }


    }
}