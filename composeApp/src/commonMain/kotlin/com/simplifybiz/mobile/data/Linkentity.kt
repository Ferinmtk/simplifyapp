package com.simplifybiz.mobile.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LinkItem(
    @SerialName("remote_id") val remoteId: Int = 0,
    @SerialName("title") val title: String = "",   // GF field 5 - Link Description
    @SerialName("url") val url: String = ""         // GF field 4 - Link URL
)

@Serializable
@Entity(tableName = "links")
data class LinkEntity(
    @ColumnInfo(name = "items")
    @SerialName("items")
    val items: List<LinkItem> = emptyList(),

    @PrimaryKey
    override val uuid: String = "links_singleton",

    @SerialName("remote_id")
    override val remoteId: Int? = null,

    // Links are pull-only — never dirty, never pushed
    override val isDirty: Boolean = false

) : Syncable {

    // Pull-only — nothing to push
    override fun toSyncPayloads(): List<JsonObject> = emptyList()

    // Pull-only — no receipt processing needed
    override fun updateWithSyncReceipt(receipt: SyncResponseItem): LinkEntity = this
}