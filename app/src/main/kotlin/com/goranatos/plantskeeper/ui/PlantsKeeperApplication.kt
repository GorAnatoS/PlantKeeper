package com.goranatos.plantskeeper.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.goranatos.plantskeeper.data.db.PlantsDatabase
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import com.goranatos.plantskeeper.data.repository.PlantsRepositoryImpl
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModelFactory
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModelFactory
import com.jakewharton.threetenabp.AndroidThreeTen
import org.kodein.di.*
import org.kodein.di.android.x.androidXModule


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
    }
}

//passwords: 1-strong, 2 - usual