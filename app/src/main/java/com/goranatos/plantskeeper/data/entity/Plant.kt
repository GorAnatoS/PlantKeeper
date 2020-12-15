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

    @ColumnInfo(name = STRING_WATERING_FREQUENCY_COLUMN)
    var string_watering_frequency: String?, //для примера - 1\3 - одначает что частота полива 1 раз в день в обычное время и 1 раз в 3 дня в период спячки\зимнее --------- empty or null - not need
    //123/123/Y
    //1/1/N 1/null/N null/null/n

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


