package com.goranatos.plantkeeper

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.goranatos.plantkeeper.ui.settings.LanguagePrefs
import com.yariksoffice.lingver.Lingver
import com.yariksoffice.lingver.store.PreferenceLocaleStore
import dagger.hilt.android.HiltAndroidApp
import java.util.*

/**
 * Created by qsufff on 7/29/2020.
 */

@HiltAndroidApp
class PlantKeeperApplication : Application() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)

        setApplicationTheme()

        setApplicationLanguage()
    }

    private fun setApplicationTheme() {
        when (sharedPreferences.getBoolean(
            getString(R.string.pref_option_is_dark_theme_enabled),
            false
        )) {
            false -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
            true -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        }
    }

    @Suppress("UNUSED_VARIABLE")
    fun setApplicationLanguage() {
        when (sharedPreferences.getString(
            "pref_option_choose_language",
            "default"
        )) {
            "ru" -> {
                val store = PreferenceLocaleStore(this, Locale(LanguagePrefs.LANGUAGE_RUSSIAN))
                val lingver = Lingver.init(this, store)
            }

            "en" -> {
                val store = PreferenceLocaleStore(this, Locale(LanguagePrefs.LANGUAGE_ENGLISH))
                val lingver = Lingver.init(this, store)
            }

            "es" -> {
                val store = PreferenceLocaleStore(this, Locale(LanguagePrefs.LANGUAGE_SPANISH))
                val lingver = Lingver.init(this, store)
            }

            else -> {
                val store = PreferenceLocaleStore(this, Locale.getDefault())
                val lingver = Lingver.init(this, store)
            }
        }
    }
}

// TODO: 5/2/2021 1.0.4 add other animations, customize dark theme

// TODO: 11/22/2020 In second version add

////TODO 2021/05/10 14:56 || new architecture

//todo onStartMessageAdd

//help
//about_application option
//const val SPRAY_NEED_COLUMN = "spraying_need"
//const val REPLANT_NEED_COLUMN = "replanting_need"
//const val CUT_NEED_COLUMN = "cutting_need"
//const val TURN_NEED_COLUMN = "turning_need"

//passwords: 1-strong, 2 - usual