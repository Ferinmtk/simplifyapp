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
@Entity(tableName = "research_and_development")
data class ResearchAndDevelopmentEntity(

    @ColumnInfo(name = "idea_collection_details")
    @SerialName("idea_collection_details")
    val ideaCollectionDetails: String = "",

    @ColumnInfo(name = "innovation_workshops_details")
    @SerialName("innovation_workshops_details")
    val innovationWorkshopsDetails: String = "",

    @ColumnInfo(name = "idea_evaluation_details")
    @SerialName("idea_evaluation_details")
    val ideaEvaluationDetails: String = "",

    @ColumnInfo(name = "swot_analysis_details")
    @SerialName("swot_analysis_details")
    val swotAnalysisDetails: String = "",

    @ColumnInfo(name = "prototype_dev_details")
    @SerialName("prototype_dev_details")
    val prototypeDevDetails: String = "",

    @ColumnInfo(name = "testing_plan_details")
    @SerialName("testing_plan_details")
    val testingPlanDetails: String = "",

    @ColumnInfo(name = "team_budget_details")
    @SerialName("team_budget_details")
    val teamBudgetDetails: String = "",

    @ColumnInfo(name = "project_management_tools_details")
    @SerialName("project_management_tools_details")
    val projectManagementToolsDetails: String = "",

    @ColumnInfo(name = "feedback_integration_details")
    @SerialName("feedback_integration_details")
    val feedbackIntegrationDetails: String = "",

    @ColumnInfo(name = "iteration_plan_details")
    @SerialName("iteration_plan_details")
    val iterationPlanDetails: String = "",

    @ColumnInfo(name = "roadmap_details")
    @SerialName("roadmap_details")
    val roadmapDetails: String = "",

    @ColumnInfo(name = "department_coordination_details")
    @SerialName("department_coordination_details")
    val departmentCoordinationDetails: String = "",

    @ColumnInfo(name = "innovations_details")
    @SerialName("innovations_details")
    val innovationsDetails: String = "",

    @ColumnInfo(name = "ip_docs_details")
    @SerialName("ip_docs_details")
    val ipDocsDetails: String = "",

    // Systems on R&D use the same shape and wire key as Leadership / Sales /
    // Operations — reuse LeadershipSystemItem. Wire key is 'systems_used';
    // server side is ResearchDevelopmentSystemsRepository (GPNF form 93,
    // parent field 12 on form 98).
    @ColumnInfo(name = "systems_used")
    @SerialName("systems_used")
    val systemsUsed: List<LeadershipSystemItem> = emptyList(),

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
    val status: String = "draft"

) : Syncable {

    companion object {
        // See LeadershipEntity.syncJson for the full rationale behind these
        // two flags — encodeDefaults keeps empty systems_used / deletion
        // lists on the wire so delete-the-last-row reaches the server;
        // explicitNulls avoids confusing the server's remote_id > 0 check.
        private val syncJson = Json {
            encodeDefaults = true
            explicitNulls = false
        }
    }

    override fun toSyncPayloads(): List<JsonObject> {
        val payload = syncJson.encodeToJsonElement(this).jsonObject.toMutableMap()
        payload["type"] = JsonPrimitive("research_and_development")
        payload["id"] = JsonPrimitive(uuid)
        return listOf(JsonObject(payload))
    }

    override fun updateWithSyncReceipt(receipt: SyncResponseItem): ResearchAndDevelopmentEntity {
        return if (receipt.id == this.uuid) {
            this.copy(
                remoteId = receipt.remoteId,
                isDirty = false,
                deletedSystemRemoteIds = emptyList()
            )
        } else this
    }
}