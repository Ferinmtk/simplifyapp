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
@Entity(tableName = "operations")
data class OperationsEntity(

    @ColumnInfo(name = "workflow_descriptions")
    @SerialName("workflow_descriptions")
    val workflowDescriptions: String = "",

    @ColumnInfo(name = "key_activities_dependencies")
    @SerialName("key_activities_dependencies")
    val keyActivitiesDependencies: String = "",

    @ColumnInfo(name = "required_resources")
    @SerialName("required_resources")
    val requiredResources: String = "",

    @ColumnInfo(name = "inventory_management_protocols")
    @SerialName("inventory_management_protocols")
    val inventoryManagementProtocols: String = "",

    @ColumnInfo(name = "quality_standards")
    @SerialName("quality_standards")
    val qualityStandards: String = "",

    @ColumnInfo(name = "quality_control_methods")
    @SerialName("quality_control_methods")
    val qualityControlMethods: String = "",

    @ColumnInfo(name = "standard_operating_procedures")
    @SerialName("standard_operating_procedures")
    val standardOperatingProcedures: String = "",

    @ColumnInfo(name = "troubleshooting_guide")
    @SerialName("troubleshooting_guide")
    val troubleshootingGuide: String = "",

    @ColumnInfo(name = "performance_metrics")
    @SerialName("performance_metrics")
    val performanceMetrics: String = "",

    @ColumnInfo(name = "monitoring_tools")
    @SerialName("monitoring_tools")
    val monitoringTools: String = "",

    @ColumnInfo(name = "scalability_steps")
    @SerialName("scalability_steps")
    val scalabilitySteps: String = "",

    @ColumnInfo(name = "automation_outsourcing_plan")
    @SerialName("automation_outsourcing_plan")
    val automationOutsourcingPlan: String = "",

    @ColumnInfo(name = "operational_risk")
    @SerialName("operational_risk")
    val operationalRisk: String = "",

    @ColumnInfo(name = "contingency_plans")
    @SerialName("contingency_plans")
    val contingencyPlans: String = "",

    // Systems — reuses LeadershipSystemItem which already has the correct
    // @SerialName annotations (uuid, remote_id, system_or_application, etc).
    @SerialName("systems")
    val systems: List<LeadershipSystemItem> = emptyList(),

    // Remote IDs of systems the user deleted locally that still exist on the server.
    // Mirrors LeadershipEntity.deletedSystemRemoteIds. Flushed on successful sync
    // via updateWithSyncReceipt(). Persisted to Room so deletes survive app restarts
    // before the sync runs.
    @ColumnInfo(name = "deleted_system_remote_ids")
    @SerialName("deleted_system_remote_ids")
    val deletedSystemRemoteIds: List<Int> = emptyList(),

    @ColumnInfo(name = "status_quo_of_implementation")
    @SerialName("status_quo_of_implementation")
    val statusQuoOfImplementation: String = "Not Started",

    @PrimaryKey
    @SerialName("uuid")
    override val uuid: String = "",

    @SerialName("remote_id")
    override val remoteId: Int? = null,

    @SerialName("is_dirty")
    override val isDirty: Boolean = false,

    @ColumnInfo(name = "status")
    @SerialName("status")
    val status: String = "draft"
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
         * Same pattern as Leadership / Marketing / Sales.
         */
        private val syncJson = Json {
            encodeDefaults = true
            explicitNulls = false
        }
    }

    override fun toSyncPayloads(): List<JsonObject> {
        val payload = syncJson.encodeToJsonElement(this).jsonObject.toMutableMap()
        payload["type"] = JsonPrimitive("operations")
        payload["id"] = JsonPrimitive(uuid)
        return listOf(JsonObject(payload))
    }

    /**
     * Handle sync receipts. Mirrors Leadership / Marketing / Sales exactly.
     *
     * When the parent operations receipt comes back:
     *   - stamp our own remote_id
     *   - clear dirty
     *   - clear the deletion queue (server has processed those deletions)
     */
    override fun updateWithSyncReceipt(receipt: SyncResponseItem): Syncable {
        return if (receipt.id == uuid) {
            copy(
                remoteId = receipt.remoteId,
                isDirty = false,
                deletedSystemRemoteIds = emptyList()
            )
        } else this
    }
}