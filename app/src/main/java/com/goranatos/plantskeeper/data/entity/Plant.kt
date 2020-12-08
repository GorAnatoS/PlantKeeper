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

    @ColumnInfo(name = NAME_COLUMN)
    val name: String?,

    @ColumnInfo(name = DESCRIPTION_COLUMN)
    val desc: String?,

    @ColumnInfo(name = IMAGE_PATH_COLUMN)
    var image_path: String?,

    @ColumnInfo(name = WATER_NEED_COLUMN)
    val water_need: String?, //для примера - 1\3 - одначает что частота полива 1 раз в день в обычное время и 1 раз в 3 дня в период спячки\зимнее --------- empty or null - not need

    @ColumnInfo(name = IS_HIBERNATE_MODE_ON)
    val is_hibernate_on: Int = 0,

    @ColumnInfo(name = HIBERNATE_MODE_DATE_START)
    val hibernate_mode_date_start: Long?,

    @ColumnInfo(name = HIBERNATE_MODE_DATE_FINISH)
    val hibernate_mode_date_finish: Long?,

// TODO: 12/7/2020 + дата начинать полив с х числа + дата начало периода спячки + дата конец периода спячки + режим спячки есть ? булеан 
)


