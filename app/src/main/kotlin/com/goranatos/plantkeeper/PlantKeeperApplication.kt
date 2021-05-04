package com.goranatos.plantkeeper

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.goranatos.plantkeeper.data.db.PlantsDatabase
import com.goranatos.plantkeeper.data.repository.PlantsRepository
import com.goranatos.plantkeeper.data.repository.PlantsRepositoryImpl
import com.goranatos.plantkeeper.ui.home.MyPlantsViewModelFactory
import com.goranatos.plantkeeper.ui.plantDetail.PlantDetailViewModelFactory
import com.goranatos.plantkeeper.ui.settings.LanguagePrefs
import com.yariksoffice.lingver.Lingver
import com.yariksoffice.lingver.store.PreferenceLocaleStore
import org.kodein.di.*
import org.kodein.di.android.x.androidXModule
import java.util.*

/**
 * Created by qsufff on 7/29/2020.
 */

class PlantKeeperApplication : Application(), DIAware {

    override val di by DI.lazy {
        import(androidXModule(this@PlantKeeperApplication))

        bind() from singleton { PlantsDatabase(instance()) }
        bind() from singleton { instance<PlantsDatabase>().plantsDatabaseDao() }
        bind<PlantsRepository>() with singleton { PlantsRepositoryImpl(instance()) }
        bind() from singleton { MyPlantsViewModelFactory(instance(), instance()) }
        bind() from factory { plantId: Int -> PlantDetailViewModelFactory(instance(), plantId) }
    }

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
            getString(R.string.pref_option_choose_language),
            getString(R.string.en)
        )) {
            getString(R.string.ru) -> {
                val store = PreferenceLocaleStore(this, Locale(LanguagePrefs.LANGUAGE_RUSSIAN))
                val lingver = Lingver.init(this, store)
            }

            getString(R.string.en) -> {
                val store = PreferenceLocaleStore(this, Locale(LanguagePrefs.LANGUAGE_ENGLISH))
                val lingver = Lingver.init(this, store)
            }

            getString(R.string.es) -> {
                val store = PreferenceLocaleStore(this, Locale(LanguagePrefs.LANGUAGE_SPANISH))
                val lingver = Lingver.init(this, store)
            }
        }
    }
}

// TODO: 5/2/2021 1.0.4 add other animations, customize dark theme

// TODO: 11/22/2020 In second version add
//help
//about_application option
//const val SPRAY_NEED_COLUMN = "spraying_need"
//const val REPLANT_NEED_COLUMN = "replanting_need"
//const val CUT_NEED_COLUMN = "cutting_need"
//const val TURN_NEED_COLUMN = "turning_need"

//passwords: 1-strong, 2 - usual