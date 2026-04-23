package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.ObjectiveEntity
import com.simplifybiz.mobile.data.ActionStepItem
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetObjectiveUseCase
import com.simplifybiz.mobile.domain.UpdateObjectiveUseCase
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ObjectivesViewModel(
    private val getObjectiveUseCase: GetObjectiveUseCase,
    private val updateObjectiveUseCase: UpdateObjectiveUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<ObjectiveEntity>(syncRepository) {

    private val _currentObjective = MutableStateFlow(ObjectiveEntity())
    val currentObjective = _currentObjective.asStateFlow()

    private var debounceJob: Job? = null
    private var lastTypingTime: Long = 0

    init {
        // Reactive Observation: Subscribe to the Single Source of Truth
        viewModelScope.launch {
            getObjectiveUseCase.observe().collect { entity ->
                // Anti-flicker: Prevent remote sync from overwriting active user input
                if (getTimeMillis() - lastTypingTime > 3000) {
                    _currentObjective.value = entity ?: ObjectiveEntity()
                }
            }
        }

        loadData(
            localLoader = { getObjectiveUseCase.execute() },
            onDataLoaded = { _currentObjective.value = it }
        )
    }

    private fun updateUiAndQueuePersist(updated: ObjectiveEntity) {
        _currentObjective.value = updated
        lastTypingTime = getTimeMillis()
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(1000)
            persist(updated)
        }
    }

    // --- Field Updates ---
    fun onObjectiveChange(text: String) = updateUiAndQueuePersist(_currentObjective.value.copy(objectiveText = text))
    fun onPointPersonChange(text: String) = updateUiAndQueuePersist(_currentObjective.value.copy(pointPerson = text))
    fun onDueDateChange(text: String) = updateUiAndQueuePersist(_currentObjective.value.copy(dueDate = text))
    fun onStatusChange(status: String) = updateUiAndQueuePersist(_currentObjective.value.copy(completionStatus = status))

    // --- Action Step Logic ---
    fun addActionStep(step: ActionStepItem) {
        val updatedSteps = _currentObjective.value.actionSteps + step
        updateUiAndQueuePersist(_currentObjective.value.copy(actionSteps = updatedSteps))
    }

    fun updateActionStep(step: ActionStepItem) {
        val updatedSteps = _currentObjective.value.actionSteps.map { if (it.id == step.id) step else it }
        updateUiAndQueuePersist(_currentObjective.value.copy(actionSteps = updatedSteps))
    }

    fun deleteActionStep(stepId: String) {
        val current = _currentObjective.value
        val updatedList = current.actionSteps.filterNot { it.id == stepId }
        updateUiAndQueuePersist(current.copy(actionSteps = updatedList))
    }

    // --- Persistence Logic ---
    private fun persist(entity: ObjectiveEntity, isSubmit: Boolean = false) {
        val prepared = if (entity.uuid.isBlank()) {
            entity.copy(uuid = getTimeMillis().toString(), isDirty = true)
        } else {
            entity.copy(isDirty = true)
        }
        saveData(
            currentData = prepared,
            localSaver = { updateObjectiveUseCase.execute(it) },
            isSubmit = isSubmit,
            onDataSaved = { /* Managed by Flow observer in init */ }
        )
    }

    fun saveDraft() = persist(_currentObjective.value.copy(status = "draft"))

    fun submitObjective() {
        if (_currentObjective.value.objectiveText.isBlank()) {
            viewModelScope.launch { _validationMessage.send("Objective description is required.") }
            return
        }
        persist(_currentObjective.value.copy(status = "submitted"), isSubmit = true)
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                getObjectiveUseCase.execute()
                _validationMessage.send("Update complete.")
            }
        }
    }
}