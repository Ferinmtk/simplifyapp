package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.OperationsEntity
import com.simplifybiz.mobile.data.LeadershipSystemItem
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetOperationsUseCase
import com.simplifybiz.mobile.domain.UpdateOperationsUseCase
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class OperationsViewModel(
    private val getOperationsUseCase: GetOperationsUseCase,
    private val updateOperationsUseCase: UpdateOperationsUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<OperationsEntity>(syncRepository) {

    private val _uiState = MutableStateFlow(OperationsEntity())
    val uiState = _uiState.asStateFlow()

    private var syncDebounceJob: Job? = null

    init {
        // Subscribe to the local DB stream. We use isDirty (NOT a typing-time
        // guard) to decide whether to overwrite UI from the observer.
        //
        // Why isDirty and not lastTypingTime: typing-time aging-out is fine for
        // text fields because every keystroke refreshes it. Systems are different
        // — addSystem/removeSystem update lastTypingTime once, then 3 seconds later
        // the guard opens and the next observer emission wipes the systems list.
        // isDirty stays true from the moment the user touches anything until the
        // sync receipt clears it, so it covers both typing AND systems edits with
        // a single rule.
        viewModelScope.launch {
            getOperationsUseCase.observe().collect { entity ->
                if (!_uiState.value.isDirty) {
                    _uiState.value = entity ?: OperationsEntity()
                }
            }
        }

        // Same isDirty guard for the remote-refresh callback.
        loadData(
            localLoader = { getOperationsUseCase.execute() },
            onDataLoaded = { _uiState.value = it },
            remoteRefreshed = { refreshed ->
                if (!_uiState.value.isDirty) {
                    _uiState.value = refreshed
                }
            }
        )
    }

    private fun withUuid(entity: OperationsEntity): OperationsEntity {
        return if (entity.uuid.isBlank()) entity.copy(uuid = getTimeMillis().toString()) else entity
    }

    /**
     * Update UI, write to DB immediately, and queue a debounced network push.
     *
     * The immediate DB write is what makes the isDirty guard above actually work.
     * Without it, when the DB observer re-emits the row (which it does on any
     * write to the table — including a remote sync writing a stale copy), the
     * row in Room would still show isDirty=false, our guard would let it through,
     * and the user's in-progress edits would be wiped.
     *
     * By writing isDirty=true to Room synchronously here, every subsequent
     * observer emission carries the dirty flag, the guard catches them, and
     * the user's edits are safe until the sync receipt comes back and clears
     * dirty in updateWithSyncReceipt.
     */
    private fun updateUiAndQueueSync(updated: OperationsEntity) {
        val dirty = withUuid(updated).copy(isDirty = true)
        _uiState.value = dirty

        // Persist isDirty=true to Room immediately so the observer guard works.
        viewModelScope.launch {
            updateOperationsUseCase.execute(dirty)
        }

        // Debounce the network push.
        syncDebounceJob?.cancel()
        syncDebounceJob = viewModelScope.launch {
            delay(1500)
            saveData(
                currentData = dirty,
                localSaver = { updateOperationsUseCase.execute(it) },
                onDataSaved = { }
            )
        }
    }

    fun onWorkflowDescriptionsChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(workflowDescriptions = text))
    fun onKeyActivitiesChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(keyActivitiesDependencies = text))
    fun onRequiredResourcesChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(requiredResources = text))
    fun onInventoryManagementChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(inventoryManagementProtocols = text))
    fun onQualityStandardsChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(qualityStandards = text))
    fun onQualityControlMethodsChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(qualityControlMethods = text))
    fun onStandardOperatingProceduresChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(standardOperatingProcedures = text))
    fun onTroubleshootingGuideChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(troubleshootingGuide = text))
    fun onPerformanceMetricsChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(performanceMetrics = text))
    fun onMonitoringToolsChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(monitoringTools = text))
    fun onScalabilityStepsChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(scalabilitySteps = text))
    fun onAutomationOutsourcingPlanChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(automationOutsourcingPlan = text))
    fun onOperationalRiskChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(operationalRisk = text))
    fun onContingencyPlansChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(contingencyPlans = text))
    fun onStatusChange(status: String) = updateUiAndQueueSync(_uiState.value.copy(statusQuoOfImplementation = status))

    fun addSystem(item: LeadershipSystemItem) {
        val updated = _uiState.value.systems + item
        updateUiAndQueueSync(_uiState.value.copy(systems = updated))
    }

    fun updateSystem(item: LeadershipSystemItem) {
        val updated = _uiState.value.systems.map { if (it.id == item.id) item else it }
        updateUiAndQueueSync(_uiState.value.copy(systems = updated))
    }

    /**
     * Remove a system by its local id.
     *
     * If the removed system was already synced (has a remote_id), add that
     * remote_id to deletedSystemRemoteIds so the next sync tells the server
     * to hard-delete the row. Without this, OperationsSystemsRepository's
     * preservation loop would revive it on the next push.
     */
    fun removeSystem(id: String) {
        val current = _uiState.value
        val removed = current.systems.firstOrNull { it.id == id }
        val updatedSystems = current.systems.filterNot { it.id == id }

        val updatedDeletions = if (removed?.remoteId != null) {
            current.deletedSystemRemoteIds + removed.remoteId
        } else {
            current.deletedSystemRemoteIds
        }

        updateUiAndQueueSync(
            current.copy(
                systems = updatedSystems,
                deletedSystemRemoteIds = updatedDeletions
            )
        )
    }

    /**
     * Explicit save-draft — cancel the pending debounce so we only fire one sync.
     */
    fun saveDraft() {
        syncDebounceJob?.cancel()
        val entity = withUuid(_uiState.value).copy(status = "draft", isDirty = true)
        viewModelScope.launch { updateOperationsUseCase.execute(entity) }
    }

    fun submitOperations() {
        val current = _uiState.value
        if (current.workflowDescriptions.isBlank()) {
            viewModelScope.launch { _validationMessage.send("Workflow Description is required.") }
            return
        }
        syncDebounceJob?.cancel()
        val entity = withUuid(current).copy(status = "submitted", isDirty = true)
        saveData(
            currentData = entity,
            localSaver = { updateOperationsUseCase.execute(it) },
            isSubmit = true,
            onDataSaved = { }
        )
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                // Push dirty local data first so the server has the latest before
                // we force-pull. Otherwise force-pull would clobber unsaved edits.
                syncRepository.pushLocalChanges()
                val refreshed = getOperationsUseCase.execute()
                _uiState.value = refreshed
                _validationMessage.send("Update complete.")
            }
        }
    }
}