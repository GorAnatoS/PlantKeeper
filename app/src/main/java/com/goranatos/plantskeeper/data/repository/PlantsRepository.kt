package com.goranatos.plantskeeper.data.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.goranatos.plantskeeper.data.entity.Plant
import kotlinx.coroutines.flow.Flow


interface PlantsRepository {
    suspend fun getAllMyPlants(): Flow<List<Plant>>
    suspend fun insertPlant(plant: Plant)
    suspend fun getPlant(id: Int): LiveData<Plant>
    suspend fun getJustPlant(id: Int): Plant
    suspend fun updatePlant(plant: Plant)
    suspend fun deletePlant(plant: Plant)
}