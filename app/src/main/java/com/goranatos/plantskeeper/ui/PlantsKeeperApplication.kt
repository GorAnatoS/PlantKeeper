package com.goranatos.plantskeeper.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.goranatos.plantskeeper.data.db.PlantsDatabase
import com.goranatos.plantskeeper.data.db.PlantsDatabaseDao
import com.goranatos.plantskeeper.data.repository.PlantsRepository
import com.goranatos.plantskeeper.data.repository.PlantsRepositoryImpl
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModelFactory
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
        bind() from provider { MyPlantsViewModelFactory(instance()) }

        }



/*    override val kodein = Kodein.lazy {







        *//*bind() from singleton { MoexDatabase(instance()) }
        bind() from singleton { instance<MoexDatabase>().moexDao() }
        bind<ConnectivityInterceptor>() with singleton { ConnectivityInterceptorImpl(instance()) }
        bind() from singleton { MoexApiService(instance()) }
        bind<MoexNetworkDataSource>() with singleton { MoexNetworkDataSourceImpl(instance()) }
        bind<MoexRepository>() with singleton { MoexRepositoryImpl(instance(), instance()) }
        bind() from provider { MoexViewModelFactory(instance()) }

        bind() from singleton { YahooApiService(instance()) }
        bind<YahooNetworkDataSource>() with singleton {YahooNetworkDataSourceImpl(instance()) }
        //bind() from provider { AnaliticsViewModelFactory(instance()) }  private val viewModelFactory: MoexViewModelFactory by instance()

        bind() from provider { RecommendationsViewModelFactory(instance(), instance()) }*//*

    }*/

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        // TODO: 11/22/2020 night mode off
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

//passwords: 1-strong, 2 - usual