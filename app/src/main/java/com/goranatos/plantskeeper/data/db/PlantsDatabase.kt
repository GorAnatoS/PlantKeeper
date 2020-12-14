package com.goranatos.plantskeeper.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.goranatos.plantskeeper.data.entity.Plant


const val DATABASE_NAME = "Plants_database.db"
const val TABLE_NAME = "Plants_database"

const val ID_COLUMN = "id"
const val NAME_COLUMN = "name"
const val DESCRIPTION_COLUMN = "description"
const val IMAGE_COLUMN = "image"
const val IMAGE_PATH_COLUMN = "image_path"

//поливать и т.п. 1 раз во сколько дней?
const val IS_WATER_NEED_ON_COLUMN = "is_water_need_on"
const val WATER_NEED_COLUMN = "watering_need"
const val FERTILIZE_NEED_COLUMN = "fertilization_need"
const val SPRAY_NEED_COLUMN = "spraying_need"
const val REPLANT_NEED_COLUMN = "replanting_need"
const val CUT_NEED_COLUMN = "cutting_need"
const val TURN_NEED_COLUMN = "turning_need"

const val IS_HIBERNATE_MODE_ON = "is_hibernate_mode_on"
const val HIBERNATE_MODE_DATE_START = "hibernate_mode_start_date"
const val HIBERNATE_MODE_DATE_FINISH = "hibernate_mode_finish_date"

//начать поливать... с какого числа
const val START_WATER_FROM_COLUMN = "to_water_from"
const val START_FERTILIZE_FROM_COLUMN = "to_fertilize_from"
const val START_SPRAY_FROM_COLUMN = "to_spray_from"
const val START_REPLANT_FROM_COLUMN = "to_replant_from"
const val START_CUT_FROM_COLUMN = "to_cut_from"
const val START_TURN_FROM_COLUMN = "to_turn_from"

const val DATABASE_VERSION = 4

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

                    //MIGRATIONS
                    val MIGRATION_1_2 : Migration = object : Migration(1, 2) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL(
                                "ALTER TABLE $TABLE_NAME"
                                        + " ADD COLUMN $IS_HIBERNATE_MODE_ON INTEGER DEFAULT 0 NOT NULL"
                            )
                        }
                    }

                    val MIGRATION_2_3 : Migration = object : Migration(2, 3) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL(
                                "ALTER TABLE $TABLE_NAME ADD COLUMN $HIBERNATE_MODE_DATE_START INTEGER"
                            )

                            database.execSQL(
                                "ALTER TABLE $TABLE_NAME ADD COLUMN $HIBERNATE_MODE_DATE_FINISH INTEGER"
                            )
                        }
                    }

                    val MIGRATION_3_4 : Migration = object : Migration(3, 4) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL(
                                "ALTER TABLE $TABLE_NAME ADD COLUMN $IS_HIBERNATE_MODE_ON INTEGER DEFAULT 0 NOT NULL"
                            )
                        }
                    }


                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PlantsDatabase::class.java,
                        DATABASE_NAME)
                        .addMigrations(MIGRATION_1_2)
                        .addMigrations(MIGRATION_2_3)
                        .addMigrations(MIGRATION_3_4)
                        .build()
                }

                INSTANCE = instance
                return instance
            }
        }
    }


}


