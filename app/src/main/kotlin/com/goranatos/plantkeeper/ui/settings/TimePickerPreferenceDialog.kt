package com.goranatos.plantkeeper.ui.settings

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceDialogFragmentCompat
import com.goranatos.plantkeeper.internal.TimeHelper.Companion.minutesFromMidnightToHourlyTime


/**
 * Created by qsufff on 1/2/2021.
 */
class TimePickerPreferenceDialog : PreferenceDialogFragmentCompat() {

    private lateinit var timepicker: TimePicker

    override fun onCreateDialogView(context: Context?): View {
        timepicker = TimePicker(context)
        return timepicker
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)

        val minutesAfterMidnight = (preference as TimePickerPreference)
            .getPersistedMinutesFromMidnight()
        timepicker.setIs24HourView(true)
        timepicker.hour = minutesAfterMidnight / 60
        timepicker.minute = minutesAfterMidnight % 60
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDialogClosed(positiveResult: Boolean) {
        // Save settings
        if (positiveResult) {
            val minutesAfterMidnight = (timepicker.hour * 60) + timepicker.minute
            (preference as TimePickerPreference).persistMinutesFromMidnight(minutesAfterMidnight)
            preference.summary = minutesFromMidnightToHourlyTime(minutesAfterMidnight)

        }
    }

    companion object {
        fun newInstance(key: String): TimePickerPreferenceDialog {
            val fragment = TimePickerPreferenceDialog()
            val bundle = Bundle(1)
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle

            return fragment
        }
    }
}