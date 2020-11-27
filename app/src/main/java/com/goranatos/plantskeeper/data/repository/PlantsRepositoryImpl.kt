package com.goranatos.plantskeeper.data.repository

import androidx.annotation.WorkerThread
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.data.db.PlantsDatabaseDao
import kotlinx.coroutines.flow.Flow

class PlantsRepositoryImpl(
    private val plantsDatabaseDao: PlantsDatabaseDao
) : PlantsRepository {
    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(plant: Plant) {
        plantsDatabaseDao.insert(plant)
    }

    override suspend fun getAllMyPlants(): Flow<List<Plant>> {
        return plantsDatabaseDao.getAllMyPlants()
    }
}