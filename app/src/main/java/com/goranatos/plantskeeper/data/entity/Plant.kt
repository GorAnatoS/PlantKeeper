package com.goranatos.plantskeeper.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goranatos.plantskeeper.data.db.*

@Entity(tableName = TABLE_NAME )
data class Plant(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN)
    val id: Int,

    @ColumnInfo(name = NAME_COLUMN)
    val name: String?,

    @ColumnInfo(name = DESCRIPTION_COLUMN)
    val desc: String?,

  /*  @ColumnInfo(name = IMAGE_COLUMN, typeAffinity = ColumnInfo.BLOB)
    val image: ByteArray?,*/


)


