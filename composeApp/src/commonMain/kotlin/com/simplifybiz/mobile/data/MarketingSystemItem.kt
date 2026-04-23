// Systems sent to / received from WP. id is a stable client UUID.
// remoteId is the server-assigned Gravity Forms entry ID — null until
// the server returns a receipt for a newly-created system.
//
// The server uses remoteId to look up the existing row on updates and
// deletions. Without it, we can't tell the server which server-side row
// to delete when a user removes a system on mobile.
//
// Mirrors LeadershipSystemItem exactly so both modules share the same
// serialization shape on the wire. The server's MarketingSystemsRepository
// and LeadershipSystemsRepository both expect the same keys because both
// point at the same SYSTEMS form (93).

package com.simplifybiz.mobile.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarketingSystemItem(
    // Stable client-side id. Serialized as "uuid" to match how Strategy's
    // TargetMarketItem and LeadershipSystemItem wire up on the server.
    @SerialName("uuid")
    val id: String = "",

    // Server-assigned ID, echoed back via sync receipts. Null until the
    // first successful push. Stored so we can later tell the server
    // "delete this exact row".
    @SerialName("remote_id")
    val remoteId: Int? = null,

    // WP field: "System or Application"
    @SerialName("system_or_application")
    val systemOrApplication: String = "",

    // WP field: "Purpose"
    @SerialName("purpose")
    val purpose: String = "",

    // WP field: "Status"
    @SerialName("status")
    val status: String = "Not Started"
)