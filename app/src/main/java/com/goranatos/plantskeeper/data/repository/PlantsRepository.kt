package com.goranatos.plantskeeper.data.repository


import com.goranatos.plantskeeper.data.entity.Plant
import kotlinx.coroutines.flow.Flow


interface PlantsRepository {
    suspend fun getAllMyPlants(): Flow<List<Plant>>
}