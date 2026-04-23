package com.simplifybiz.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

@Serializable
@Entity(tableName = "strategies")
data class StrategyEntity(
    @SerialName("why_start_business") val purpose: String = "",
    @SerialName("core_values") val coreValues: String = "",
    @SerialName("target_markets") val targetMarkets: List<TargetMarketItem> = emptyList(),
    @SerialName("deleted_target_market_remote_ids") val deletedTargetMarketIds: List<Int> = emptyList(),
    @SerialName("solutions") val solutions: String = "",
    @SerialName("budget_marketing") val budgetMarketing: Int = 0,
    @SerialName("budget_sales") val budgetSales: Int = 0,
    @SerialName("budget_operations") val budgetOperations: Int = 0,
    @SerialName("budget_admin") val budgetAdmin: Int = 0,

    @SerialName("leadership_first") val leadershipFirst: String = "",
    @SerialName("leadership_last") val leadershipLast: String = "",
    @SerialName("marketing_first") val marketingFirst: String = "",
    @SerialName("marketing_last") val marketingLast: String = "",
    @SerialName("sales_first") val salesFirst: String = "",
    @SerialName("sales_last") val salesLast: String = "",
    @SerialName("operations_first") val operationsFirst: String = "",
    @SerialName("operations_last") val operationsLast: String = "",
    @SerialName("systems_first") val systemsFirst: String = "",
    @SerialName("systems_last") val systemsLast: String = "" ,

    @SerialName("has_marketing_lead") val hasMarketingLead: Boolean = false,
    @SerialName("has_sales_lead") val hasSalesLead: Boolean = false,
    @SerialName("has_operations_lead") val hasOperationsLead: Boolean = false,
    @SerialName("has_systems_lead") val hasSystemsLead: Boolean = false,

    @PrimaryKey
    @SerialName("uuid")
    override val uuid: String = "",

    @SerialName("remote_id")
    override val remoteId: Int? = null,

    @SerialName("is_dirty")
    override val isDirty: Boolean = false,

    @SerialName("status")
    val status: String = "draft"
) : Syncable {

    override fun toSyncPayloads(): List<JsonObject> {
        val list = mutableListOf<JsonObject>()
        val strategyJson = Json.encodeToJsonElement(this).jsonObject.toMutableMap()

        strategyJson["type"] = JsonPrimitive("strategy")
        strategyJson["id"] = JsonPrimitive(uuid)
        // Ensure child deletions are sent in the main payload
        strategyJson["deleted_target_market_remote_ids"] = Json.encodeToJsonElement(deletedTargetMarketIds)
        strategyJson.remove("target_markets")

        list.add(JsonObject(strategyJson))

        targetMarkets.forEach { tm ->
            list.add(buildJsonObject {
                put("type", "target_market")
                put("uuid", tm.id)
                put("remote_id", tm.remoteId)
                put("market_name", tm.name)
                put("demographics", tm.demographics)
                put("behaviors", tm.behaviors)
                put("opportunities", tm.opportunities)
            })
        }
        return list
    }

    override fun updateWithSyncReceipt(receipt: SyncResponseItem): StrategyEntity {
        if (receipt.id == this.uuid) {
            return this.copy(remoteId = receipt.remoteId, isDirty = false, deletedTargetMarketIds = emptyList())
        }
        val updatedMarkets = this.targetMarkets.map { tm ->
            if (tm.id == receipt.id) tm.copy(remoteId = receipt.remoteId) else tm
        }
        return this.copy(targetMarkets = updatedMarkets)
    }
}

@Serializable
data class TargetMarketItem(
    @SerialName("uuid") val id: String,
    @SerialName("remote_id") val remoteId: Int? = null,
    @SerialName("market_name") val name: String = "",
    @SerialName("demographics") val demographics: String = "",
    @SerialName("behaviors") val behaviors: String = "",
    @SerialName("opportunities") val opportunities: String = ""
)
