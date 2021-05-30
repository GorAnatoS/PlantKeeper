package com.goranatos.plantkeeper.data.db

import androidx.room.*
import com.goranatos.plantkeeper.data.entity.Plant
import kotlinx.coroutines.flow.Flow

/**
 * Created by qsufff on 9/13/2020.
 */
@Dao
interface PlantDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: Plant)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(plant: Plant)

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllMyPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $ID_COLUMN = :id")
    fun getPlant(id: Int): Plant

    @Query("DELETE FROM $TABLE_NAME WHERE $ID_COLUMN = :id")
    fun deletePlantWithId(id: Int)

    @Delete
    fun delete(plant: Plant)
}