package com.simplifybiz.mobile.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mobile representation of a single system row inside the People module.
 *

 * Wire format:
 *   {
 *     "uuid": "...",           // local id on mobile; server echoes it back
 *     "remote_id": 1234,       // GF entry ID on server, null until first sync
 *     "system_or_application": "...",
 *     "purpose": "...",
 *     "status": "Not Started" | "Doing" | "In Progress"
 *   }
 *
 * IMPORTANT: the id property uses @SerialName("uuid"), NOT "id". Kotlinx
 * Serialization with ignoreUnknownKeys=true silently drops fields whose
 * JSON keys it doesn't recognize, so if the server ever emits "id" instead
 * of "uuid" (or vice versa), every system arrives with id="" and delete
 * operations target the wrong row. The server-side repo was written to
 * emit "uuid" to match this class.
 */
@Serializable
data class PeopleSystemItem(
    @SerialName("uuid")
    val id: String = "",

    @SerialName("remote_id")
    val remoteId: Int? = null,

    @SerialName("system_or_application")
    val systemOrApplication: String = "",

    @SerialName("purpose")
    val purpose: String = "",

    @SerialName("status")
    val status: String = "Not Started"
)