package com.goranatos.plantskeeper.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goranatos.plantskeeper.data.db.*

@Entity(tableName = TABLE_NAME)
data class Plant(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN)
    val id: Int,

    @ColumnInfo(name = STRING_NAME_COLUMN)
    var name: String?,

    @ColumnInfo(name = STRING_DESCRIPTION_COLUMN)
    val desc: String?,

    @ColumnInfo(name = STRING_IMAGE_PATH_COLUMN)
    var image_path: String?,

    @ColumnInfo(name = IS_WATER_NEED_ON_COLUMN)
    var is_water_need_on: Int = 0,

    @ColumnInfo(name = LONG_TO_WATER_FROM_DATE_COLUMN)
    var long_to_water_from_date: Long?,

    @ColumnInfo(name = IS_HIBERNATE_IN_WATERING_ON_COLUMN)
    var is_hibernate_in_watering_on: Int = 0,
    @ColumnInfo(name = INT_WATERING_FREQUENCY_NORMAL_COLUMN)
    var watering_frequency_normal: Int = -1,
    @ColumnInfo(name = INT_WATERING_FREQUENCY_IN_HIBERNATE_COLUMN)
    var watering_frequency_in_hibernate: Int = -1,


    @ColumnInfo(name = IS_HIBERNATE_MODE_ON)
    var is_hibernate_on: Int = 0,
    @ColumnInfo(name = LONG_TO_HIBERNATE_FROM_DATE_COLUMN)
    var long_to_hibernate_from_date: Long?,
    @ColumnInfo(name = LONG_TO_HIBERNATE_TILL_DATE_COLUMN)
    var long_to_hibernate_till_date: Long?,


// TODO: 12/7/2020 + дата начинать полив с х числа + дата начало периода спячки + дата конец периода спячки + режим спячки есть ? булеан 
)

enum class IS(val value: Int) {
    YES(0),
    NO(1),
}


