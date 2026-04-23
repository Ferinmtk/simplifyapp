package com.simplifybiz.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
@Entity(tableName = "marketing")
data class MarketingEntity(
    @SerialName("uuid")
    @PrimaryKey
    override val uuid: String = "",

    @SerialName("remote_id")
    override val remoteId: Int? = null,

    @SerialName("is_dirty")
    override val isDirty: Boolean = false,

    @SerialName("status")
    val status: String = "draft",

    @SerialName("marketing_objectives")
    val marketingObjectives: String = "",

    @SerialName("marketing_channels_rationale")
    val marketingChannelsRationale: String = "",

    @SerialName("marketing_budget")
    val marketingBudget: String = "",

    @SerialName("one_off_cost")
    val oneOffCost: String = "",

    @SerialName("recurring_cost")
    val recurringCost: String = "",

    @SerialName("budget_frequency")
    val budgetFrequency: String = "",

    @SerialName("brand_core_message")
    val brandCoreMessage: String = "",

    @SerialName("brand_tone")
    val brandTone: String = "",

    @SerialName("content_types")
    val contentTypes: String = "",

    @SerialName("success_metrics_key_metrics")
    val successMetricsKeyMetrics: String = "",

    @SerialName("owners_time_commitment")
    val ownersTimeCommitment: String = "",

    @SerialName("owners_skills")
    val ownersSkills: String = "",

    @SerialName("owners_outsourcing_needs")
    val ownersOutsourcingNeeds: String = "",

    @SerialName("systems")
    val systems: List<MarketingSystemItem> = emptyList(),

    // Remote IDs of systems the user deleted locally that still exist on the server.
    // Mirrors LeadershipEntity.deletedSystemRemoteIds. Flushed on successful sync
    // via updateWithSyncReceipt(). Persisted to Room so deletes survive app restarts
    // before the sync runs.
    @SerialName("deleted_system_remote_ids")
    val deletedSystemRemoteIds: List<Int> = emptyList(),

    @SerialName("process_status")
    val processStatus: String = "Not Started",

    @Transient
    val idealCustomer: String = "",

    @Transient
    val offerStrategy: String = "",

    @Transient
    val leadCapture: String = "",

    @Transient
    val kpisTracking: String = "",

    @Transient
    val contentStrategy: String = "",

    @Transient
    val channels: List<MarketingChannelItem> = emptyList(),

    @Transient
    val statusQuoOfImplementation: String = "Not Started"
) : Syncable {

    companion object {
        /**
         * Sync-specific JSON encoder.
         *
         * The default Json encoder uses encodeDefaults=false, which drops any
         * field whose value equals its declared default. That would silently
         * strip `systems: []` and `deleted_system_remote_ids: []` from the
         * outbound payload in exactly the cases we most need them — deleting
         * the last system, or a sync immediately after the list goes empty.
         *
         * Leadership hit this exact bug (see LeadershipEntity.toSyncPayloads)
         * and uses the same encodeDefaults=true / explicitNulls=false config.
         * Keeping these consistent across modules matters because the server's
         * SyncHandler branches on whether these keys are present, not on their
         * content.
         */
        private val syncJson = Json {
            encodeDefaults = true
            explicitNulls = false
        }
    }

    override fun toSyncPayloads(): List<JsonObject> {
        val payload = syncJson.encodeToJsonElement(this).jsonObject.toMutableMap()
        payload["type"] = JsonPrimitive("marketing")
        payload["id"] = JsonPrimitive(uuid)

        // Defensive: @Transient fields should already be excluded by the
        // serializer, but remove them explicitly to guard against future
        // changes to their annotations.
        payload.remove("idealCustomer")
        payload.remove("offerStrategy")
        payload.remove("leadCapture")
        payload.remove("kpisTracking")
        payload.remove("contentStrategy")
        payload.remove("channels")
        payload.remove("statusQuoOfImplementation")

        return listOf(JsonObject(payload))
    }

    /**
     * Handle sync receipts. Mirrors LeadershipEntity exactly.
     *
     * When the parent marketing receipt comes back:
     *   - stamp our own remote_id
     *   - clear dirty
     *   - clear the deletion queue (server has processed those deletions)
     */
    override fun updateWithSyncReceipt(receipt: SyncResponseItem): MarketingEntity {
        return if (receipt.id == this.uuid) {
            this.copy(
                remoteId = receipt.remoteId,
                isDirty = false,
                deletedSystemRemoteIds = emptyList()
            )
        } else this
    }
}

@Serializable
data class MarketingChannelItem(
    val id: String,
    val channelName: String = "",
    val purpose: String = ""
)