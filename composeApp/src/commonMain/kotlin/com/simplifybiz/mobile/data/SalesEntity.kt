package com.simplifybiz.mobile.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
@Entity(tableName = "sales")
data class SalesEntity(

    // Funnel
    @ColumnInfo(name = "sales_funnel_stages")
    @SerialName("sales_funnel_stages")
    val salesFunnelStages: String = "",

    // Qualification
    @ColumnInfo(name = "lead_qualification_criteria")
    @SerialName("lead_qualification_criteria")
    val leadQualificationCriteria: String = "",

    // Channels
    @ColumnInfo(name = "sales_channels")
    @SerialName("sales_channels")
    val salesChannels: String = "",

    // Scripts
    @ColumnInfo(name = "elevator_pitch")
    @SerialName("elevator_pitch")
    val elevatorPitch: String = "",

    @ColumnInfo(name = "business_purpose")
    @SerialName("business_purpose")
    val businessPurpose: String = "",

    // Team
    @ColumnInfo(name = "sales_team_structure")
    @SerialName("sales_team_structure")
    val salesTeamStructure: String = "",

    // Goals
    @ColumnInfo(name = "sales_goals")
    @SerialName("sales_goals")
    val salesGoals: String = "",

    // Training
    @ColumnInfo(name = "sales_training_plan")
    @SerialName("sales_training_plan")
    val salesTrainingPlan: String = "",

    // Metrics
    @ColumnInfo(name = "sales_metrics")
    @SerialName("sales_metrics")
    val salesMetrics: String = "",

    // Price
    @ColumnInfo(name = "price_list")
    @SerialName("price_list")
    val priceList: String = "",

    // Objections process
    @ColumnInfo(name = "objection_process")
    @SerialName("objection_process")
    val objectionProcess: String = "",

    // Systems — reuses LeadershipSystemItem which already has the correct
    // @SerialName annotations (uuid, remote_id, system_or_application, etc).
    @ColumnInfo(name = "systems_used")
    @SerialName("systems_used")
    val systemsUsed: List<LeadershipSystemItem> = emptyList(),

    // Remote IDs of systems the user deleted locally that still exist on the server.
    // Mirrors LeadershipEntity.deletedSystemRemoteIds. Flushed on successful sync
    // via updateWithSyncReceipt(). Persisted to Room so deletes survive app restarts
    // before the sync runs.
    @ColumnInfo(name = "deleted_system_remote_ids")
    @SerialName("deleted_system_remote_ids")
    val deletedSystemRemoteIds: List<Int> = emptyList(),

    // Status
    @ColumnInfo(name = "implementation_status")
    @SerialName("implementation_status")
    val implementationStatus: String = "Not Started",

    @PrimaryKey
    override val uuid: String = "",

    @SerialName("remote_id")
    override val remoteId: Int? = null,

    @SerialName("is_dirty")
    override val isDirty: Boolean = false,

    val status: String = "draft"
) : Syncable {

    companion object {
        /**
         * Sync-specific JSON encoder.
         *
         * The default Json encoder uses encodeDefaults=false, which drops any
         * field whose value equals its declared default. That would silently
         * strip `systems_used: []` and `deleted_system_remote_ids: []` from the
         * outbound payload in exactly the cases we most need them — deleting
         * the last system, or a sync immediately after the list goes empty.
         *
         * Same pattern as Leadership / Marketing entities. Keeping the config
         * consistent across modules matters because the server's SyncHandler
         * branches on whether these keys are PRESENT, not on their content.
         */
        private val syncJson = Json {
            encodeDefaults = true
            explicitNulls = false
        }
    }

    override fun toSyncPayloads(): List<JsonObject> {
        val payload = syncJson.encodeToJsonElement(this).jsonObject.toMutableMap()
        payload["type"] = JsonPrimitive("sales")
        payload["id"] = JsonPrimitive(uuid)
        return listOf(JsonObject(payload))
    }

    /**
     * Handle sync receipts. Mirrors Leadership / Marketing exactly.
     *
     * When the parent sales receipt comes back:
     *   - stamp our own remote_id
     *   - clear dirty
     *   - clear the deletion queue (server has processed those deletions)
     */
    override fun updateWithSyncReceipt(receipt: SyncResponseItem): SalesEntity {
        return if (receipt.id == this.uuid) {
            this.copy(
                remoteId = receipt.remoteId,
                isDirty = false,
                deletedSystemRemoteIds = emptyList()
            )
        } else this
    }
}