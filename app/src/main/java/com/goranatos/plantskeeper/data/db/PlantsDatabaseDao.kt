package com.goranatos.plantskeeper.data.db

import androidx.room.*
import com.goranatos.plantskeeper.data.entity.Plant
import kotlinx.coroutines.flow.Flow


/**
 * Created by qsufff on 9/13/2020.
 */
@Dao
interface PlantsDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: Plant)

    @Update
    fun update(plant: Plant)

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllMyPlants(): Flow<List<Plant>>

    @Delete
    fun delete(plant: Plant)
}