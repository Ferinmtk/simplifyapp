package com.simplifybiz.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
@Entity(tableName = "leadership")
data class LeadershipEntity(
    @ColumnInfo(name = "strategy_review_schedule")
    @SerialName("strategy_review_schedule")
    val strategyReviewSchedule: String = "",

    @ColumnInfo(name = "decision_framework")
    @SerialName("decision_framework")
    val decisionFramework: String = "",

    @ColumnInfo(name = "decision_tools")
    @SerialName("decision_tools")
    val decisionTools: String = "",

    @ColumnInfo(name = "delegation_plan")
    @SerialName("delegation_plan")
    val delegationPlan: String = "",

    @ColumnInfo(name = "expectation_setting")
    @SerialName("expectation_setting")
    val expectationSetting: String = "",

    @ColumnInfo(name = "communication_channels")
    @SerialName("communication_channels")
    val communicationChannels: String = "",

    @ColumnInfo(name = "leadership_training")
    @SerialName("leadership_training")
    val leadershipTraining: String = "",

    @ColumnInfo(name = "coaching_programs")
    @SerialName("coaching_programs")
    val coachingPrograms: String = "",

    @ColumnInfo(name = "leadership_kpis")
    @SerialName("leadership_kpis")
    val leadershipKpis: String = "",

    @ColumnInfo(name = "feedback_mechanism")
    @SerialName("feedback_mechanism")
    val feedbackMechanism: String = "",

    @ColumnInfo(name = "change_management_framework")
    @SerialName("change_management_framework")
    val changeManagementFramework: String = "",

    @ColumnInfo(name = "status_quo_of_implementation")
    @SerialName("status_quo_of_implementation")
    val statusQuoOfImplementation: String = "Not Started",

    @ColumnInfo(name = "systems_used")
    @SerialName("systems_used")
    val systemsUsed: List<LeadershipSystemItem> = emptyList(),

    // Remote IDs of systems the user deleted locally that still exist on the server.
    // Mirrors StrategyEntity.deletedTargetMarketIds — gets flushed on successful sync.
    // Persisted so deletes survive app restarts before the sync runs.
    @ColumnInfo(name = "deleted_system_remote_ids")
    @SerialName("deleted_system_remote_ids")
    val deletedSystemRemoteIds: List<Int> = emptyList(),

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
         * Dedicated Json instance for outbound sync payloads.
         *
         * encodeDefaults = true — CRITICAL. Without this, kotlinx.serialization drops
         * any field whose value equals its declared default. That causes empty lists
         * (systems_used = emptyList()) to vanish from the JSON, which in turn causes
         * the server's `isset($item['systems_used'])` guard to skip the deletion
         * code path entirely. Symptoms: mobile "delete last system" never persists
         * to WordPress.
         *
         * explicitNulls = false — drops null values so that `remote_id: null`
         * doesn't confuse the server's `$item['remote_id'] > 0` check for new items.
         */
        private val syncJson = Json {
            encodeDefaults = true
            explicitNulls = false
        }
    }

    /**
     * Build the outbound sync payload.
     *
     * Uses the module-local syncJson to guarantee systems_used and
     * deleted_system_remote_ids are always present on the wire, even when empty.
     * The server reads deleted_system_remote_ids off the leadership payload and
     * passes it to sync_systems().
     */
    override fun toSyncPayloads(): List<JsonObject> {
        val payload = syncJson.encodeToJsonElement(this).jsonObject.toMutableMap()
        payload["type"] = JsonPrimitive("leadership")
        payload["id"] = JsonPrimitive(uuid)
        return listOf(JsonObject(payload))
    }

    /**
     * Handle sync receipts.
     *
     * When the parent leadership receipt comes back:
     *   - stamp our own remote_id
     *   - clear dirty
     *   - clear the deletion queue (server has processed those deletions)
     *
     * Individual system receipts (if the server ever emits them) would land
     * here too; for now the server folds system processing inside the
     * leadership response, so this is a no-op for child items.
     */
    override fun updateWithSyncReceipt(receipt: SyncResponseItem): LeadershipEntity {
        return if (receipt.id == this.uuid) {
            this.copy(
                remoteId = receipt.remoteId,
                isDirty = false,
                deletedSystemRemoteIds = emptyList()
            )
        } else this
    }
}