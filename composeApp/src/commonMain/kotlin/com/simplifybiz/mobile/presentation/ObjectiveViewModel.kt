package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.ActionStepItem
import com.simplifybiz.mobile.data.ObjectiveEntity
import com.simplifybiz.mobile.data.TaskItem
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetObjectiveUseCase
import com.simplifybiz.mobile.domain.UpdateObjectiveUseCase
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Edits ONE objective identified by uuid. Pass uuid via Koin parametersOf().
 *
 * Empty uuid means "new objective" — a UUID is minted on first persist.
 *
 * Persistence model:
 * - Every state mutation writes to the LOCAL DB immediately. Closing the
 *   screen never loses data.
 * - Server pushes are debounced (~800ms) so a fast typist doesn't hammer the
 *   sync endpoint. Sync still fires on every leaf action; the debounce just
 *   coalesces bursts.
 */
internal class ObjectivesViewModel(
    private val getObjectiveUseCase: GetObjectiveUseCase,
    private val updateObjectiveUseCase: UpdateObjectiveUseCase,
    syncRepository: UniversalSyncRepository,
    private val initialUuid: String = ""
) : BaseSyncViewModel<ObjectiveEntity>(syncRepository) {

    private val _currentObjective = MutableStateFlow(ObjectiveEntity(uuid = initialUuid))
    val currentObjective = _currentObjective.asStateFlow()

    private var pushDebounceJob: Job? = null
    private var lastTypingTime: Long = 0L

    init {
        if (initialUuid.isNotBlank()) {
            // Subscribe to the row in DB so external sync updates flow in.
            viewModelScope.launch {
                getObjectiveUseCase.observeByUuid(initialUuid).collect { entity ->
                    if (entity != null && getTimeMillis() - lastTypingTime > 1500) {
                        _currentObjective.value = entity
                    }
                }
            }
            // Initial load
            viewModelScope.launch {
                val loaded = getObjectiveUseCase.getByUuid(initialUuid)
                if (loaded != null) _currentObjective.value = loaded
            }
        } else {
            // New objective — mint a uuid up front so child rows can reference it.
            _currentObjective.value = ObjectiveEntity(uuid = getTimeMillis().toString())
        }
    }

    /**
     * Single funnel for any state change. Writes to local DB synchronously
     * (well, on viewModelScope) and queues a debounced sync push.
     */
    private fun mutate(updated: ObjectiveEntity) {
        _currentObjective.value = updated
        lastTypingTime = getTimeMillis()

        // Local persist immediately so close/kill never drops data.
        viewModelScope.launch {
            updateObjectiveUseCase.execute(updated.copy(isDirty = true))
        }

        // Debounced server push — coalesces bursts.
        pushDebounceJob?.cancel()
        pushDebounceJob = viewModelScope.launch {
            delay(800)
            try { syncRepository.pushLocalChanges() } catch (_: Exception) { }
        }
    }

    /** Pushes immediately — for save/cancel dialog confirmations and explicit Save. */
    private fun mutateAndPushNow(updated: ObjectiveEntity) {
        _currentObjective.value = updated
        viewModelScope.launch {
            updateObjectiveUseCase.execute(updated.copy(isDirty = true))
            try { syncRepository.pushLocalChanges() } catch (_: Exception) { }
        }
    }

    // ------------------------------------------------------------------
    // Objective field updates
    // ------------------------------------------------------------------

    fun onObjectiveChange(text: String) =
        mutate(_currentObjective.value.copy(objectiveText = text))

    fun onPointPersonChange(text: String) =
        mutate(_currentObjective.value.copy(pointPerson = text))

    fun onDueDateChange(text: String) =
        mutateAndPushNow(_currentObjective.value.copy(dueDate = text))

    fun onDueTimeChange(text: String) =
        mutateAndPushNow(_currentObjective.value.copy(dueTime = text))

    fun onStatusChange(status: String) =
        mutateAndPushNow(_currentObjective.value.copy(completionStatus = status))

    // ------------------------------------------------------------------
    // Expected Outcomes (repeater)
    // ------------------------------------------------------------------

    fun addExpectedOutcome() =
        mutateAndPushNow(_currentObjective.value.copy(
            expectedOutcomes = _currentObjective.value.expectedOutcomes + ""
        ))

    fun updateExpectedOutcome(index: Int, text: String) {
        val list = _currentObjective.value.expectedOutcomes.toMutableList()
        if (index in list.indices) {
            list[index] = text
            mutate(_currentObjective.value.copy(expectedOutcomes = list))
        }
    }

    fun removeExpectedOutcome(index: Int) {
        val list = _currentObjective.value.expectedOutcomes.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            mutateAndPushNow(_currentObjective.value.copy(expectedOutcomes = list))
        }
    }

    // ------------------------------------------------------------------
    // Action Step CRUD
    // ------------------------------------------------------------------

    fun addActionStep(step: ActionStepItem) {
        val updated = _currentObjective.value.copy(
            actionSteps = _currentObjective.value.actionSteps + step
        )
        mutateAndPushNow(updated)
    }

    fun updateActionStep(step: ActionStepItem) {
        val updated = _currentObjective.value.copy(
            actionSteps = _currentObjective.value.actionSteps.map {
                if (it.id == step.id) step else it
            }
        )
        mutateAndPushNow(updated)
    }

    fun deleteActionStep(stepId: String) {
        val current = _currentObjective.value
        val target = current.actionSteps.firstOrNull { it.id == stepId }
        val newDeletedIds = if (target?.remoteId != null) {
            current.deletedActionStepRemoteIds + target.remoteId
        } else current.deletedActionStepRemoteIds
        val updated = current.copy(
            actionSteps = current.actionSteps.filterNot { it.id == stepId },
            deletedActionStepRemoteIds = newDeletedIds
        )
        mutateAndPushNow(updated)
    }

    // ------------------------------------------------------------------
    // Task CRUD (operates on tasks list inside a specific action step)
    // ------------------------------------------------------------------

    fun addTask(actionStepId: String, task: TaskItem) {
        val updated = _currentObjective.value.copy(
            actionSteps = _currentObjective.value.actionSteps.map { step ->
                if (step.id == actionStepId) step.copy(tasks = step.tasks + task) else step
            }
        )
        mutateAndPushNow(updated)
    }

    fun updateTask(actionStepId: String, task: TaskItem) {
        val updated = _currentObjective.value.copy(
            actionSteps = _currentObjective.value.actionSteps.map { step ->
                if (step.id == actionStepId) {
                    step.copy(tasks = step.tasks.map { if (it.id == task.id) task else it })
                } else step
            }
        )
        mutateAndPushNow(updated)
    }

    fun deleteTask(actionStepId: String, taskId: String) {
        val current = _currentObjective.value
        val updatedSteps = current.actionSteps.map { step ->
            if (step.id == actionStepId) {
                val target = step.tasks.firstOrNull { it.id == taskId }
                val newDeletedIds = if (target?.remoteId != null) {
                    step.deletedTaskRemoteIds + target.remoteId
                } else step.deletedTaskRemoteIds
                step.copy(
                    tasks = step.tasks.filterNot { it.id == taskId },
                    deletedTaskRemoteIds = newDeletedIds
                )
            } else step
        }
        mutateAndPushNow(current.copy(actionSteps = updatedSteps))
    }

    // ------------------------------------------------------------------
    // Submit (mark not draft)
    // ------------------------------------------------------------------

    fun submitObjective() {
        if (_currentObjective.value.objectiveText.isBlank()) {
            viewModelScope.launch { _validationMessage.send("Objective description is required.") }
            return
        }
        val submitted = _currentObjective.value.copy(status = "submitted")
        viewModelScope.launch {
            updateObjectiveUseCase.execute(submitted.copy(isDirty = true))
            try { syncRepository.pushLocalChanges() } catch (_: Exception) { }
            _saveSuccess.send(true)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                if (initialUuid.isNotBlank()) {
                    val refreshed = getObjectiveUseCase.getByUuid(initialUuid)
                    if (refreshed != null) _currentObjective.value = refreshed
                }
                _validationMessage.send("Update complete.")
            }
        }
    }
}