package com.goranatos.plantskeeper.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.goranatos.plantskeeper.data.entity.Plant


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


const val SPRAY_NEED_COLUMN = "spraying_need"
const val REPLANT_NEED_COLUMN = "replanting_need"
const val CUT_NEED_COLUMN = "cutting_need"
const val TURN_NEED_COLUMN = "turning_need"

const val IS_HIBERNATE_MODE_ON = "is_hibernate_mode_on"
const val LONG_TO_HIBERNATE_FROM_DATE_COLUMN = "to_hibernate_from_date"
const val LONG_TO_HIBERNATE_TILL_DATE_COLUMN = "to_hibernate_till_date"

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

/*                    //MIGRATIONS
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
                                "ALTER TABLE $TABLE_NAME ADD COLUMN $LONG_TO_HIBERNATE_FROM_DATE_COLUMN INTEGER"
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
                    }*/


                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PlantsDatabase::class.java,
                        DATABASE_NAME)
                     /*   .addMigrations(MIGRATION_1_2)
                        .addMigrations(MIGRATION_2_3)
                        .addMigrations(MIGRATION_3_4)*/
                        .build()
                }

                INSTANCE = instance
                return instance
            }
        }
    }


}


