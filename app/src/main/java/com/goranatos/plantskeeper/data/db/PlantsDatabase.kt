package com.goranatos.plantskeeper.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.goranatos.plantskeeper.data.entity.Plant
import okhttp3.internal.Internal.instance


const val DATABASE_NAME = "Plants_database.db"
const val TABLE_NAME = "Plants_database"

const val ID_COLUMN = "id"
const val NAME_COLUMN = "name"
const val DESCRIPTION_COLUMN = "description"
const val IMAGE_COLUMN = "image"
const val IMAGE_PATH_COLUMN = "image_path"

//поливать и т.п. 1 раз во сколько дней?
const val WATER_NEED_COLUMN = "watering_need"
const val FERTILIZE_NEED_COLUMN = "fertilization_need"
const val SPRAY_NEED_COLUMN = "spraying_need"
const val REPLANT_NEED_COLUMN = "replanting_need"
const val CUT_NEED_COLUMN = "cutting_need"
const val TURN_NEED_COLUMN = "turning_need"

//начать поливать... с какого числа
const val START_WATER_FROM_COLUMN = "to_water_from"
const val START_FERTILIZE_FROM_COLUMN = "to_fertilize_from"
const val START_SPRAY_FROM_COLUMN = "to_spray_from"
const val START_REPLANT_FROM_COLUMN = "to_replant_from"
const val START_CUT_FROM_COLUMN = "to_cut_from"
const val START_TURN_FROM_COLUMN = "to_turn_from"

const val DATABASE_VERSION = 1

@Database(entities = [Plant::class], version = DATABASE_VERSION)
abstract class PlantsDatabase : RoomDatabase() {

    abstract fun plantsDatabaseDao(): PlantsDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: PlantsDatabase? = null

        private val LOCK = Any()

        operator fun invoke(context: Context) = INSTANCE ?: synchronized(LOCK) {
            INSTANCE ?: getInstance(context).also { INSTANCE = it }
        }

        fun getInstance(
            context: Context
        ): PlantsDatabase {
            synchronized(this) {
                var instance =
                    INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PlantsDatabase::class.java,
                        DATABASE_NAME
                    ).build()
                }

                INSTANCE = instance
                return instance
            }
        }
    }
}


