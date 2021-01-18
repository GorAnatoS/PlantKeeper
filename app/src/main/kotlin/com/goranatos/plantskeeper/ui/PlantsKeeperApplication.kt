package com.goranatos.plantskeeper.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.db.PlantsDatabase
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import com.goranatos.plantskeeper.data.repository.PlantsRepositoryImpl
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModelFactory
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModelFactory
import com.goranatos.plantskeeper.ui.settings.LANGUAGE_PREFS
import com.yariksoffice.lingver.Lingver
import com.yariksoffice.lingver.store.PreferenceLocaleStore
import org.kodein.di.*
import org.kodein.di.android.x.androidXModule
import java.util.*


/**
 * Created by qsufff on 7/29/2020.
 */


class PlantsKeeperApplication : Application(), DIAware {

    override val di by DI.lazy {
        import(androidXModule(this@PlantsKeeperApplication))

        bind() from singleton { PlantsDatabase(instance()) }
        bind() from singleton { instance<PlantsDatabase>().plantsDatabaseDao() }
        bind<PlantsRepository>() with singleton { PlantsRepositoryImpl(instance()) }
        bind() from singleton { MyPlantsViewModelFactory(instance(), instance()) }
        bind() from factory { plantId: Int -> PlantDetailViewModelFactory(instance(), plantId) }
    }


    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setApplicationLanguage()
    }

    @Suppress("UNUSED_VARIABLE")
    fun setApplicationLanguage() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)

        when (sharedPreferences.getString(
            getString(R.string.pref_option_choose_language),
            getString(R.string.en)
        )) {
            "ru" -> {
                val store = PreferenceLocaleStore(this, Locale(LANGUAGE_PREFS.LANGUAGE_RUSSIAN))
                val lingver = Lingver.init(this, store)

                Lingver.getInstance().setLocale(
                    this, LANGUAGE_PREFS.LANGUAGE_RUSSIAN,
                    LANGUAGE_PREFS.LANGUAGE_RUSSIAN_COUNTRY
                )

            }

            "en" -> {
                val store = PreferenceLocaleStore(this, Locale(LANGUAGE_PREFS.LANGUAGE_ENGLISH))
                val lingver = Lingver.init(this, store)

                Lingver.getInstance().setLocale(
                    this, LANGUAGE_PREFS.LANGUAGE_ENGLISH,
                    LANGUAGE_PREFS.LANGUAGE_ENGLISH_COUNTRY
                )
            }
        }
    }
}

//passwords: 1-strong, 2 - usual