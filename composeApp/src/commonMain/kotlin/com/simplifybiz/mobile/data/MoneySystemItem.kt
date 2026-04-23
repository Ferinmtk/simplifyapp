package com.simplifybiz.mobile.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mobile representation of a single system row inside the Money module.
 *
 * Schema matches the other five modules exactly. See the comment on
 * LeadershipSystemItem / PeopleSystemItem for rationale on @SerialName("uuid").
 *
 * Wire format:
 *   {
 *     "uuid": "...",           // local id on mobile; server echoes it back
 *     "remote_id": 1234,       // GF entry ID on server, null until first sync
 *     "system_or_application": "...",
 *     "purpose": "...",
 *     "status": "Not Started" | "In Progress" | "Fully Implemented"
 *   }
 */
@Serializable
data class MoneySystemItem(

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