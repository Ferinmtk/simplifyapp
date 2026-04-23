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

/**
 * File-private Json encoder for Money sync payloads.
 *
 * Lives at file-top-level instead of inside a `companion object {}` because
 * the kotlinx.serialization K2 compiler plugin has a bug where user-defined
 * companions on @Serializable data classes can collide with the plugin's
 * generated KSerializer companion:
 *
 *   IllegalStateException: Plugin generated companion object for class ...
 *   but it is already present in class
 *
 * Functionally identical to the companion-object pattern used elsewhere —
 * just safer. If we ever need to unify this across entities, a top-level
 * shared `syncJson` in a common file would work.
 *
 * Why the custom encoder at all: the default Json instance uses
 * encodeDefaults=false, which drops any field equal to its declared default.
 * That silently strips `systems: []` and `deleted_system_remote_ids: []`
 * from the outbound payload in the exact cases we need them — deleting the
 * last system, or syncing immediately after the list goes empty.
 */
private val moneySyncJson = Json {
    encodeDefaults = true
    explicitNulls = false
}

@Serializable
@Entity(tableName = "money")
data class MoneyEntity(

    // ── File upload URLs (read-only from mobile) ─────────────────────────────
    // Managed via the web form only. Mobile receives the URLs and renders
    // them as tappable "view" cards, but never edits or uploads.
    // These keys are STRIPPED from the outbound payload in toSyncPayloads
    // so we can never accidentally clear the URL on the server by sending
    // an empty string.
    //
    // Property names end in `Url` to make the read-only intent loud at call
    // sites. Wire keys use `_url` suffix matching server's to_mobile_array.

    @ColumnInfo(name = "annual_budget_url")
    @SerialName("annual_budget_url")
    val annualBudgetUrl: String = "",

    @ColumnInfo(name = "departmental_budget_url")
    @SerialName("departmental_budget_url")
    val departmentalBudgetUrl: String = "",

    @ColumnInfo(name = "cash_flow_forecast_url")
    @SerialName("cash_flow_forecast_url")
    val cashFlowForecastUrl: String = "",

    // ── Editable text fields ─────────────────────────────────────────────────

    @ColumnInfo(name = "revenue_tracking_system")
    @SerialName("revenue_tracking_system")
    val revenueTrackingSystem: String = "",

    @ColumnInfo(name = "revenue_monitoring_tools")
    @SerialName("revenue_monitoring_tools")
    val revenueMonitoringTools: String = "",

    @ColumnInfo(name = "expense_categories")
    @SerialName("expense_categories")
    val expenseCategories: String = "",

    @ColumnInfo(name = "expense_approval_workflow")
    @SerialName("expense_approval_workflow")
    val expenseApprovalWorkflow: String = "",

    @ColumnInfo(name = "cost_saving_strategies")
    @SerialName("cost_saving_strategies")
    val costSavingStrategies: String = "",

    @ColumnInfo(name = "cash_reserve_plan")
    @SerialName("cash_reserve_plan")
    val cashReservePlan: String = "",

    // NEW: Financial Reports. Existed on server form 97 (field 9) but was
    // missing entirely from the mobile side. Added here as a normal text field.
    @ColumnInfo(name = "financial_reports")
    @SerialName("financial_reports")
    val financialReports: String = "",

    @ColumnInfo(name = "stakeholder_reporting")
    @SerialName("stakeholder_reporting")
    val stakeholderReporting: String = "",

    @ColumnInfo(name = "compliance_requirements")
    @SerialName("compliance_requirements")
    val complianceRequirements: String = "",

    @ColumnInfo(name = "audit_schedule")
    @SerialName("audit_schedule")
    val auditSchedule: String = "",

    @ColumnInfo(name = "reinvestment_criteria")
    @SerialName("reinvestment_criteria")
    val reinvestmentCriteria: String = "",

    @ColumnInfo(name = "funding_options")
    @SerialName("funding_options")
    val fundingOptions: String = "",

    @SerialName("systems")
    val systems: List<MoneySystemItem> = emptyList(),

    // Remote IDs of systems the user deleted locally. Flushed on successful
    // sync via updateWithSyncReceipt.
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

    override fun toSyncPayloads(): List<JsonObject> {
        val payload = moneySyncJson.encodeToJsonElement(this).jsonObject.toMutableMap()

        // Strip URL fields from outbound payload. These are web-managed;
        // mobile has no business writing them. If we left them in, a sync
        // immediately after loading a fresh (empty) entity would send empty
        // strings — and even though the server currently doesn't read them
        // in load_from_mobile, relying on that is fragile. Strip at source.
        payload.remove("annual_budget_url")
        payload.remove("departmental_budget_url")
        payload.remove("cash_flow_forecast_url")

        payload["type"] = JsonPrimitive("money")
        payload["id"] = JsonPrimitive(uuid)
        return listOf(JsonObject(payload))
    }

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