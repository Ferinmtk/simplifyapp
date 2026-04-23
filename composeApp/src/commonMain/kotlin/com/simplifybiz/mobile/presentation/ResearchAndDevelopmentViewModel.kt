package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.LeadershipSystemItem
import com.simplifybiz.mobile.data.ResearchAndDevelopmentEntity
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetResearchAndDevelopmentUseCase
import com.simplifybiz.mobile.domain.UpdateResearchAndDevelopmentUseCase
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ResearchAndDevelopmentViewModel(
    private val getRDUseCase: GetResearchAndDevelopmentUseCase,
    private val updateRDUseCase: UpdateResearchAndDevelopmentUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<ResearchAndDevelopmentEntity>(syncRepository) {

    private val _uiState = MutableStateFlow(ResearchAndDevelopmentEntity())
    val uiState = _uiState.asStateFlow()

    private var debounceJob: Job? = null
    private var lastTypingTime: Long = 0

    init {
        viewModelScope.launch {
            getRDUseCase.observe().collect { entity ->
                if (getTimeMillis() - lastTypingTime > 3000) {
                    _uiState.value = entity ?: ResearchAndDevelopmentEntity()
                }
            }
        }

        loadData(
            localLoader = { getRDUseCase.execute() },
            onDataLoaded = { _uiState.value = it },
            remoteRefreshed = { refreshed ->
                if (getTimeMillis() - lastTypingTime > 3000) {
                    _uiState.value = refreshed
                }
            }
        )
    }

    private fun updateUiAndQueuePersist(updatedEntity: ResearchAndDevelopmentEntity) {
        _uiState.value = updatedEntity
        lastTypingTime = getTimeMillis()

        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(1000)
            persist(updatedEntity)
        }
    }

    fun onIdeaCollectionChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(ideaCollectionDetails = v))
    fun onInnovationWorkshopsChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(innovationWorkshopsDetails = v))
    fun onIdeaEvaluationChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(ideaEvaluationDetails = v))
    fun onSwotAnalysisChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(swotAnalysisDetails = v))
    fun onPrototypeDevChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(prototypeDevDetails = v))
    fun onTestingPlanChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(testingPlanDetails = v))
    fun onTeamBudgetChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(teamBudgetDetails = v))
    fun onProjectManagementToolsChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(projectManagementToolsDetails = v))
    fun onFeedbackIntegrationChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(feedbackIntegrationDetails = v))
    fun onIterationPlanChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(iterationPlanDetails = v))
    fun onRoadmapChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(roadmapDetails = v))
    fun onDepartmentCoordinationChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(departmentCoordinationDetails = v))
    fun onInnovationsChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(innovationsDetails = v))
    fun onIpDocsChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(ipDocsDetails = v))
    fun onStatusChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(statusQuoOfImplementation = v))

    fun addSystem(item: LeadershipSystemItem) {
        val updated = _uiState.value.systemsUsed + item
        updateUiAndQueuePersist(_uiState.value.copy(systemsUsed = updated))
    }

    fun updateSystem(item: LeadershipSystemItem) {
        val updated = _uiState.value.systemsUsed.map { if (it.id == item.id) item else it }
        updateUiAndQueuePersist(_uiState.value.copy(systemsUsed = updated))
    }

    /**
     * Remove a system.
     *
     * Priority for the remote_id we include in deleted_system_remote_ids:
     *   1. _uiState's copy of the system (if it has remote_id)
     *   2. Room's copy matched by name + purpose (survives when the sync
     *      receipt didn't update _uiState's system IDs yet)
     *
     * Without the fallback, mobile-created systems whose server IDs haven't
     * been pulled back into _uiState will send an empty deletion list, the
     * server's preservation loop keeps the row alive, and the next GET
     * brings it back 3 seconds later.
     */
    fun removeSystem(id: String) {
        val current = _uiState.value
        val removed = current.systemsUsed.firstOrNull { it.id == id }
        val updatedSystems = current.systemsUsed.filterNot { it.id == id }

        viewModelScope.launch {
            var remoteIdToDelete = removed?.remoteId

            if (remoteIdToDelete == null && removed != null) {
                val roomState = runCatching { getRDUseCase.execute() }.getOrNull()
                remoteIdToDelete = roomState?.systemsUsed?.firstOrNull {
                    it.systemName.trim().equals(removed.systemName.trim(), ignoreCase = true) &&
                            it.purpose.trim().equals(removed.purpose.trim(), ignoreCase = true)
                }?.remoteId
            }

            val updatedDeletions = if (remoteIdToDelete != null) {
                current.deletedSystemRemoteIds + remoteIdToDelete
            } else {
                current.deletedSystemRemoteIds
            }

            updateUiAndQueuePersist(
                current.copy(
                    systemsUsed = updatedSystems,
                    deletedSystemRemoteIds = updatedDeletions
                )
            )
        }
    }

    private fun persist(entity: ResearchAndDevelopmentEntity, isSubmit: Boolean = false) {
        val finalEntity = if (entity.uuid.isBlank()) {
            entity.copy(uuid = getTimeMillis().toString(), isDirty = true)
        } else {
            entity.copy(isDirty = true)
        }

        saveData(
            currentData = finalEntity,
            localSaver = { updateRDUseCase.execute(it) },
            isSubmit = isSubmit,
            onDataSaved = { /* Managed by Flow observer in init */ }
        )
    }

    fun saveDraft() {
        debounceJob?.cancel()
        persist(_uiState.value.copy(status = "draft"))
    }

    fun submitRD() {
        debounceJob?.cancel()
        persist(_uiState.value.copy(status = "submitted"), isSubmit = true)
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                getRDUseCase.execute()
                _validationMessage.send("Update complete.")
            }
        }
    }
}