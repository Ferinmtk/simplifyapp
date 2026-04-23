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
@Entity(tableName = "risks")
data class RiskEntity(

    @ColumnInfo(name = "business_structure")
    @SerialName("business_structure")
    val businessStructure: String = "",

    @ColumnInfo(name = "business_registration")
    @SerialName("business_registration")
    val businessRegistration: String = "",

    @ColumnInfo(name = "compliance_requirements")
    @SerialName("compliance_requirements")
    val complianceRequirements: String = "",

    @ColumnInfo(name = "compliance_monitoring")
    @SerialName("compliance_monitoring")
    val complianceMonitoring: String = "",

    @ColumnInfo(name = "contract_templates")
    @SerialName("contract_templates")
    val contractTemplates: String = "",

    @ColumnInfo(name = "contract_review_process")
    @SerialName("contract_review_process")
    val contractReviewProcess: String = "",

    @ColumnInfo(name = "legal_counsel")
    @SerialName("legal_counsel")
    val legalCounsel: String = "",

    @ColumnInfo(name = "intellectual_property_strategy")
    @SerialName("intellectual_property_strategy")
    val intellectualPropertyStrategy: String = "",

    @ColumnInfo(name = "ip_registration_plan")
    @SerialName("ip_registration_plan")
    val ipRegistrationPlan: String = "",

    @ColumnInfo(name = "data_privacy_policies")
    @SerialName("data_privacy_policies")
    val dataPrivacyPolicies: String = "",

    @ColumnInfo(name = "employee_legal_agreements")
    @SerialName("employee_legal_agreements")
    val employeeLegalAgreements: String = "",

    @ColumnInfo(name = "risk_assessment")
    @SerialName("risk_assessment")
    val riskAssessment: String = "",

    @ColumnInfo(name = "risk_mitigation_plan")
    @SerialName("risk_mitigation_plan")
    val riskMitigationPlan: String = "",

    @ColumnInfo(name = "dispute_resolution_process")
    @SerialName("dispute_resolution_process")
    val disputeResolutionProcess: String = "",

    // Systems on Risk use the same shape and wire key as Leadership / Sales /
    // Operations — reuse LeadershipSystemItem rather than defining a third
    // item class with identical fields. Wire key is 'systems_used'; server
    // side is RiskSystemsRepository (GPNF form 93, parent field 8 on form 100).
    @ColumnInfo(name = "systems_used")
    @SerialName("systems_used")
    val systemsUsed: List<LeadershipSystemItem> = emptyList(),

    // Remote IDs of systems the user deleted locally that still exist on the
    // server. Mirrors LeadershipEntity.deletedSystemRemoteIds — gets flushed
    // on successful sync. Persisted so deletes survive app restarts before
    // the sync runs.
    @ColumnInfo(name = "deleted_system_remote_ids")
    @SerialName("deleted_system_remote_ids")
    val deletedSystemRemoteIds: List<Int> = emptyList(),

    @ColumnInfo(name = "status_quo_of_implementation")
    @SerialName("status_quo_of_implementation")
    val statusQuoOfImplementation: String = "Not Started",

    @PrimaryKey
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
         * Dedicated Json instance for outbound sync payloads.
         *
         * encodeDefaults = true — CRITICAL. Without this, kotlinx.serialization
         * drops any field whose value equals its declared default. That causes
         * empty lists (systems_used = emptyList()) to vanish from the JSON,
         * which in turn causes the server's `isset($item['systems_used'])`
         * guard to skip the deletion code path entirely. Symptoms: mobile
         * "delete last system" never persists to WordPress.
         *
         * explicitNulls = false — drops null values so that `remote_id: null`
         * doesn't confuse the server's `$item['remote_id'] > 0` check for new
         * items.
         *
         * Matches LeadershipEntity.syncJson verbatim; the comment is repeated
         * here rather than extracted because this is load-bearing behavior
         * and a shared helper would make it too easy to miss the invariant
         * when reading just this file.
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
     * deleted_system_remote_ids are always present on the wire, even when
     * empty. The server reads deleted_system_remote_ids off the risk payload
     * and passes it to sync_systems().
     */
    override fun toSyncPayloads(): List<JsonObject> {
        val payload = syncJson.encodeToJsonElement(this).jsonObject.toMutableMap()
        payload["type"] = JsonPrimitive("risk")
        payload["id"] = JsonPrimitive(uuid)
        return listOf(JsonObject(payload))
    }

    /**
     * Handle sync receipts. When the risk receipt comes back, stamp the
     * remote_id, clear dirty, and clear the deletion queue (server has
     * processed those deletions).
     */
    override fun updateWithSyncReceipt(receipt: SyncResponseItem): RiskEntity {
        return if (receipt.id == this.uuid) {
            this.copy(
                remoteId = receipt.remoteId,
                isDirty = false,
                deletedSystemRemoteIds = emptyList()
            )
        } else this
    }
}