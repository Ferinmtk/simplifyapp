package com.simplifybiz.mobile.data

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
@Entity(tableName = "objectives")
data class ObjectiveEntity(
    @SerialName("objective_text") val objectiveText: String = "",
    @SerialName("point_person") val pointPerson: String = "",
    @SerialName("due_date") val dueDate: String = "",
    @SerialName("due_time") val dueTime: String = "",
    @SerialName("completion_status") val completionStatus: String = "Not Started",

    @SerialName("action_steps") val actionSteps: List<ActionStepItem> = emptyList(),
    @SerialName("deleted_action_step_remote_ids") val deletedActionStepRemoteIds: List<Int> = emptyList(),

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
        val objectiveMap = Json.encodeToJsonElement(this).jsonObject.toMutableMap()

        objectiveMap["type"] = JsonPrimitive("objectives")
        objectiveMap["id"] = JsonPrimitive(uuid)
        objectiveMap.remove("action_steps")

        list.add(JsonObject(objectiveMap))

        actionSteps.forEach { step ->
            val stepJson = Json.encodeToJsonElement(step).jsonObject.toMutableMap()
            stepJson["type"] = JsonPrimitive("actionsteps")
            stepJson["parent_objective_uuid"] = JsonPrimitive(uuid)
            list.add(JsonObject(stepJson))
        }
        return list
    }

    override fun updateWithSyncReceipt(receipt: SyncResponseItem): ObjectiveEntity {
        if (receipt.id == this.uuid) {
            return this.copy(remoteId = receipt.remoteId, isDirty = false, deletedActionStepRemoteIds = emptyList())
        }
        val updatedSteps = this.actionSteps.map { step ->
            if (step.id == receipt.id) step.copy(remoteId = receipt.remoteId) else step
        }
        return this.copy(actionSteps = updatedSteps)
    }
}

@Serializable
data class ActionStepItem(
    @SerialName("uuid") val id: String,
    @SerialName("remote_id") val remoteId: Int? = null,
    @SerialName("task_name") val taskName: String = "",
    @SerialName("point_person") val pointPerson: String = "",
    @SerialName("due_date") val dueDate: String = "",
    @SerialName("due_time") val dueTime: String = "",
    @SerialName("status") val status: String = "Not Started",
    @SerialName("date_completed") val dateCompleted: String = ""
)
