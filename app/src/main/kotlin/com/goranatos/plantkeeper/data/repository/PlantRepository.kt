package com.goranatos.plantkeeper.data.repository

import androidx.annotation.WorkerThread
import com.goranatos.plantkeeper.data.db.PlantDatabaseDao
import com.goranatos.plantkeeper.data.entity.Plant
import kotlinx.coroutines.flow.Flow

class PlantRepository(
    private val plantDatabaseDao: PlantDatabaseDao
) : PlantRepositoryInterface {
    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insertPlant(plant: Plant) {
        plantDatabaseDao.insert(plant)
    }

    override suspend fun getPlant(id: Int): Plant {
        return plantDatabaseDao.getPlant(id)
    }

    override suspend fun updatePlant(plant: Plant) {
        return plantDatabaseDao.update(plant)
    }

    override suspend fun deletePlant(plant: Plant) {
        plantDatabaseDao.delete(plant)
    }

    override suspend fun deletePlantWithId(id: Int) {
        plantDatabaseDao.deletePlantWithId(id)
    }

    override suspend fun getAllMyPlants(): Flow<List<Plant>> {
        return plantDatabaseDao.getAllMyPlants()
    }
}