package com.goranatos.plantkeeper.di

import com.goranatos.plantkeeper.data.db.PlantDatabaseDao
import com.goranatos.plantkeeper.data.repository.PlantRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * Created by qsufff on 5/15/2021.
 */

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun providePlantRepository(plantDatabaseDao: PlantDatabaseDao): PlantRepository {
        return PlantRepository(plantDatabaseDao)
    }
}