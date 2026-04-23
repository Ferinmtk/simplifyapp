// Systems sent to / received from WP. id is a stable client UUID.
// remoteId is the server-assigned Gravity Forms entry ID — null until
// the server returns a receipt for a newly-created system.
//
// The server uses remoteId to look up the existing row on updates and
// deletions. Without it, we can't tell the server which server-side row
// to delete when a user removes a system on mobile.

package com.simplifybiz.mobile.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeadershipSystemItem(
    // Stable client-side id. Serialized as "uuid" to match how Strategy's
    // TargetMarketItem wires up on the server — the sync handler uses the
    // same lookup logic across child types.
    @SerialName("uuid")
    val id: String = "",

    // Server-assigned ID, echoed back via sync receipts. Null until the
    // first successful push. Stored so we can later tell the server
    // "delete this exact row".
    @SerialName("remote_id")
    val remoteId: Int? = null,

    // WP field: "System or Application"
    @SerialName("system_or_application")
    val systemName: String = "",

    // WP field: "Purpose"
    @SerialName("purpose")
    val purpose: String = "",

    // WP field: "Status"
    @SerialName("status")
    val status: String = "Not Started"
)