package com.goranatos.plantkeeper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.goranatos.plantkeeper.data.entity.Plant

const val DATABASE_NAME = "Plants_database.db"
const val TABLE_NAME = "Plants_database"

const val ID_COLUMN = "id"
const val STRING_NAME_COLUMN = "string_name"
const val STRING_DESCRIPTION_COLUMN = "string_description"
const val STRING_IMAGE_PATH_COLUMN = "string_image_path"

//поливать и т.п. 1 раз во сколько дней?
const val IS_WATER_NEED_ON_COLUMN = "is_water_need_on"
const val LONG_NEXT_WATERING_DATE_COLUMN = "long_next_watering_date"

const val INT_WATERING_FREQUENCY_NORMAL_COLUMN = "int_watering_frequency_normal"
const val INT_WATERING_FREQUENCY_IN_HIBERNATE_COLUMN = "int_watering_frequency_in_hibernate"
const val IS_WATERING_HIBERNATE_MODE_ON_COLUMN = "is_watering_hibernate_mode_on"


const val IS_FERTILIZE_NEED_ON_COLUMN = "is_fertilize_need_on"
const val LONG_NEXT_FERTILIZING_DATE_COLUMN = "long_next_fertilizing_date"

const val INT_FERTILIZING_FREQUENCY_NORMAL_COLUMN = "int_fertilizing_frequency_normal"
const val INT_FERTILIZING_FREQUENCY_IN_HIBERNATE_COLUMN = "int_fertilizing_frequency_in_hibernate"
const val IS_FERTILIZING_HIBERNATE_MODE_ON_COLUMN = "is_fertilizing_hibernate_mode_on"

const val IS_HIBERNATE_MODE_ON = "is_hibernate_mode_on"
const val LONG_TO_HIBERNATE_FROM_DATE_COLUMN = "to_hibernate_from_date"
const val LONG_TO_HIBERNATE_TILL_DATE_COLUMN = "to_hibernate_till_date"

const val DATABASE_VERSION = 1

@Database(entities = [Plant::class], version = DATABASE_VERSION)
abstract class PlantsDatabase : RoomDatabase() {
    abstract fun plantsDatabaseDao(): PlantDatabaseDao
}


