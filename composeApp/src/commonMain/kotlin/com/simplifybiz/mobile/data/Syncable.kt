package com.simplifybiz.mobile.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// contract for all entities to support universal sync
interface Syncable {
    val uuid: String
    val remoteId: Int?
    val isDirty: Boolean

    // converts entity into list of json objects for php backend
    fun toSyncPayloads(): List<JsonObject>

    // updates entity or its children with new remote ids from server
    fun updateWithSyncReceipt(receipt: SyncResponseItem): Syncable
}

@Serializable
data class SyncResponseItem(
    val id: String,
    @SerialName("remote_id") val remoteId: Int,
    val status: String
)
