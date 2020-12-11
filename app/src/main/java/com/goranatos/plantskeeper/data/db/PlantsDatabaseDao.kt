package com.goranatos.plantskeeper.data.db

import androidx.lifecycle.LiveData
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

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(plant: Plant)

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllMyPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $ID_COLUMN = :id")
    fun getPlant(id : Int): LiveData<Plant>

    @Query("SELECT * FROM $TABLE_NAME WHERE $ID_COLUMN = :id")
    fun getJustPlant(id : Int): Plant

    @Delete
    fun delete(plant: Plant)
}