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
@Entity(tableName = "people")
data class PeopleEntity(

    @ColumnInfo(name = "job_roles_and_skills")
    @SerialName("job_roles_and_skills")
    val jobRolesAndSkills: String = "",

    @ColumnInfo(name = "recruitment_channels")
    @SerialName("recruitment_channels")
    val recruitmentChannels: String = "",

    @ColumnInfo(name = "hiring_process")
    @SerialName("hiring_process")
    val hiringProcess: String = "",

    @ColumnInfo(name = "onboarding_plan")
    @SerialName("onboarding_plan")
    val onboardingPlan: String = "",

    @ColumnInfo(name = "onboarding_training")
    @SerialName("onboarding_training")
    val onboardingTraining: String = "",

    @ColumnInfo(name = "training_programs")
    @SerialName("training_programs")
    val trainingPrograms: String = "",

    // Orphan `programs` field removed. It had no server mapping, no UI binding,
    // and was a half-finished refactor leftover. Removing it while we're here
    // to reduce confusion — if anything ever actually needed it, it would have
    // broken already.

    @ColumnInfo(name = "mentorship_programs")
    @SerialName("mentorship_programs")
    val mentorshipPrograms: String = "",

    @ColumnInfo(name = "performance_goals")
    @SerialName("performance_goals")
    val performanceGoals: String = "",

    @ColumnInfo(name = "performance_review_schedule")
    @SerialName("performance_review_schedule")
    val performanceReviewSchedule: String = "",

    @ColumnInfo(name = "compensation_packages")
    @SerialName("compensation_packages")
    val compensationPackages: String = "",

    @ColumnInfo(name = "retention_initiatives")
    @SerialName("retention_initiatives")
    val retentionInitiatives: String = "",

    @ColumnInfo(name = "succession_plan")
    @SerialName("succession_plan")
    val successionPlan: String = "",

    @ColumnInfo(name = "leadership_development_plans")
    @SerialName("leadership_development_plans")
    val leadershipDevelopmentPlans: String = "",

    @ColumnInfo(name = "employee_engagement_methods")
    @SerialName("employee_engagement_methods")
    val employeeEngagementMethods: String = "",

    @ColumnInfo(name = "feedback_action_plan")
    @SerialName("feedback_action_plan")
    val feedbackActionPlan: String = "",

    @SerialName("systems")
    val systems: List<PeopleSystemItem> = emptyList(),

    // Remote IDs of systems the user deleted locally that still exist on the server.
    // Mirrors the same field on Leadership / Marketing / Sales / Operations entities.
    // Flushed on successful sync via updateWithSyncReceipt(). Persisted to Room so
    // deletes survive app restarts before the sync runs.
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
         * Same pattern as Leadership / Marketing / Sales / Operations.
         */
        private val syncJson = Json {
            encodeDefaults = true
            explicitNulls = false
        }
    }

    override fun toSyncPayloads(): List<JsonObject> {
        val payload = syncJson.encodeToJsonElement(this).jsonObject.toMutableMap()
        payload["type"] = JsonPrimitive("people")
        payload["id"] = JsonPrimitive(uuid)
        return listOf(JsonObject(payload))
    }

    /**
     * Handle sync receipts. Mirrors the other four modules exactly.
     *
     * When the parent people receipt comes back:
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