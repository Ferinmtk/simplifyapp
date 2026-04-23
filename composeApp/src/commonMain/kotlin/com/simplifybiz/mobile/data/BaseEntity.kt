package com.simplifybiz.mobile.data

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class BaseEntity {

    @PrimaryKey
    @ColumnInfo(name = "uuid")
    open var uuid: String = ""

    @ColumnInfo(name = "remote_id")
    @SerialName("remote_id")
    open var remoteId: Int = 0

    @ColumnInfo(name = "type")
    open var type: String = ""

    @ColumnInfo(name = "status")
    open var status: String = "draft"

    @ColumnInfo(name = "user_id")
    @SerialName("user_id")
    open var userId: Int = 0
}
