package com.goranatos.plantkeeper.di

import android.content.Context
import androidx.room.Room
import com.goranatos.plantkeeper.PlantKeeperApplication
import com.goranatos.plantkeeper.data.db.DATABASE_NAME
import com.goranatos.plantkeeper.data.db.PlantDatabaseDao
import com.goranatos.plantkeeper.data.db.PlantsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * Created by qsufff on 5/15/2021.
 */

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {
    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): PlantKeeperApplication {
        return app as PlantKeeperApplication
    }


    @Singleton
    @Provides
    fun providePlantDatabase(@ApplicationContext context: Context): PlantsDatabase {
        return Room.databaseBuilder(
            context,
            PlantsDatabase::class.java,
            DATABASE_NAME
        )

            .build()
    }

    @Singleton
    @Provides
    fun providePlantDatabaseDAO(plantDatabase: PlantsDatabase): PlantDatabaseDao {
        return plantDatabase.plantsDatabaseDao()
    }
}