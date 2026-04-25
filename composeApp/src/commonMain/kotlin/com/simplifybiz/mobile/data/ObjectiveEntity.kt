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

/**
 * Webapp alignment (April 2026):
 *
 *   Objective (form 103)
 *     └── ActionStep (form 81, parent_objective_uuid)
 *           └── Task (form 78, parent_action_step_uuid)
 *
 * Mobile now stores MANY objectives (was singleton). Each objective owns its
 * own action_steps list, and each action step owns its own tasks list.
 */
@Serializable
@Entity(tableName = "objectives")
data class ObjectiveEntity(
    @SerialName("objective_text") val objectiveText: String = "",

    @SerialName("expected_outcomes") val expectedOutcomes: List<String> = emptyList(),

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

        // 1. Objective itself (form 103) — strip nested children before sending
        val objectiveMap = Json.encodeToJsonElement(this).jsonObject.toMutableMap()
        objectiveMap["type"] = JsonPrimitive("objectives")
        objectiveMap["id"] = JsonPrimitive(uuid)
        objectiveMap.remove("action_steps")
        list.add(JsonObject(objectiveMap))

        // 2. Each Action Step (form 81) — parent links to objective uuid
        actionSteps.forEach { step ->
            val stepJson = Json.encodeToJsonElement(step).jsonObject.toMutableMap()
            stepJson["type"] = JsonPrimitive("actionsteps")
            stepJson["parent_objective_uuid"] = JsonPrimitive(uuid)
            stepJson.remove("tasks")
            list.add(JsonObject(stepJson))

            // 3. Each Task (form 78) — parent links to action step uuid
            step.tasks.forEach { task ->
                val taskJson = Json.encodeToJsonElement(task).jsonObject.toMutableMap()
                taskJson["type"] = JsonPrimitive("tasks")
                taskJson["parent_action_step_uuid"] = JsonPrimitive(step.id)
                list.add(JsonObject(taskJson))
            }
        }
        return list
    }

    override fun updateWithSyncReceipt(receipt: SyncResponseItem): ObjectiveEntity {
        // Receipt for the objective itself
        if (receipt.id == this.uuid) {
            return this.copy(
                remoteId = receipt.remoteId,
                isDirty = false,
                deletedActionStepRemoteIds = emptyList()
            )
        }

        // Receipt for one of the children — could be an action step or a task
        val updatedSteps = this.actionSteps.map { step ->
            when {
                step.id == receipt.id -> step.copy(
                    remoteId = receipt.remoteId,
                    deletedTaskRemoteIds = emptyList()
                )
                else -> {
                    val updatedTasks = step.tasks.map { task ->
                        if (task.id == receipt.id) task.copy(remoteId = receipt.remoteId) else task
                    }
                    if (updatedTasks != step.tasks) step.copy(tasks = updatedTasks) else step
                }
            }
        }
        return this.copy(actionSteps = updatedSteps)
    }
}

@Serializable
data class ActionStepItem(
    @SerialName("uuid") val id: String,
    @SerialName("remote_id") val remoteId: Int? = null,

    @SerialName("action_step") val name: String = "",
    @SerialName("due_date") val dueDate: String = "",
    @SerialName("due_time") val dueTime: String = "",
    @SerialName("status") val status: String = "Not Started",
    @SerialName("date_completed") val dateCompleted: String = "",

    @SerialName("tasks") val tasks: List<TaskItem> = emptyList(),
    @SerialName("deleted_task_remote_ids") val deletedTaskRemoteIds: List<Int> = emptyList()
)

@Serializable
data class TaskItem(
    @SerialName("uuid") val id: String,
    @SerialName("remote_id") val remoteId: Int? = null,

    @SerialName("task") val taskText: String = "",
    @SerialName("point_person") val pointPerson: String = "",
    @SerialName("due_date") val dueDate: String = "",
    @SerialName("due_time") val dueTime: String = "",
    @SerialName("status") val status: String = "Not Started",
    @SerialName("date_completed") val dateCompleted: String = ""
)