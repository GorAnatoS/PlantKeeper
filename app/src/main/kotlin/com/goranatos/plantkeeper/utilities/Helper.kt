package com.goranatos.plantkeeper.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.annotation.NonNull
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.ui.intro.IntroActivity


/**
 * Created by qsufff on 11/27/2020.
 */
class Helper {
    companion object {
        fun Fragment.hideKeyboard() {
            view?.let { activity?.hideKeyboard(it) }
        }

        fun Activity.hideKeyboard() {
            hideKeyboard(currentFocus ?: View(this))
        }

        fun Context.hideKeyboard(view: View) {
            val inputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun getScreenWidth(@NonNull activity: Activity): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                windowMetrics.bounds.width() - insets.left - insets.right
            } else {
                val displayMetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                displayMetrics.widthPixels
            }
        }

        fun calculateWidthOfView(context: Context): Int {
            val displayMetrics = context.resources.displayMetrics
            return displayMetrics.widthPixels
        }

        fun onFirstStartShowAppInfo(context: Context) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            preferences.getBoolean(
                SharedPreferencesRepositoryConstants.PREF_IS_FIRST_APPLICATION_START,
                true
            )

            val intent = Intent(context, IntroActivity::class.java)
            context.startActivity(intent)

            preferences.edit().putBoolean(
                SharedPreferencesRepositoryConstants.PREF_IS_FIRST_APPLICATION_START,
                false
            ).apply()

            MaterialAlertDialogBuilder(context)
                .setMessage(
                    HtmlCompat.fromHtml(
                        context.getString(R.string.first_start_info),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                )
                .setPositiveButton(context.getString(R.string.str_continue), null)
                .setCancelable(false)
                .show()
        }
    }
}