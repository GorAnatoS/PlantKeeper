package com.goranatos.plantskeeper.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goranatos.plantskeeper.data.db.*

@Entity(tableName = TABLE_NAME)
data class Plant(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN)
    val int_id: Int,

    @ColumnInfo(name = STRING_NAME_COLUMN)
    var str_name: String?,

    @ColumnInfo(name = STRING_DESCRIPTION_COLUMN)
    val str_desc: String?,

    @ColumnInfo(name = STRING_IMAGE_PATH_COLUMN)
    var string_uri_image_path: String?,

    @ColumnInfo(name = IS_WATER_NEED_ON_COLUMN)
    var is_water_need_on: Int = 0,

    @ColumnInfo(name = LONG_NEXT_WATERING_DATE_COLUMN)
    var long_next_watering_date: Long?,

    @ColumnInfo(name = IS_WATERING_HIBERNATE_MODE_ON_COLUMN)
    var is_watering_hibernate_mode_on: Int = 0,
    @ColumnInfo(name = INT_WATERING_FREQUENCY_NORMAL_COLUMN)
    var int_watering_frequency_normal: Int?,
    @ColumnInfo(name = INT_WATERING_FREQUENCY_IN_HIBERNATE_COLUMN)
    var int_watering_frequency_in_hibernate: Int?,

    @ColumnInfo(name = IS_HIBERNATE_MODE_ON)
    var is_hibernate_mode_on: Int = 0,
    @ColumnInfo(name = LONG_TO_HIBERNATE_FROM_DATE_COLUMN)
    var long_to_hibernate_from_date: Long?,
    @ColumnInfo(name = LONG_TO_HIBERNATE_TILL_DATE_COLUMN)
    var long_to_hibernate_till_date: Long?,
)

//Где Is - 0 значит нет, 1 - да
//null - значит не установлено значение

enum class IS(val value: Int) {
    YES(0),
    NO(1),
}


