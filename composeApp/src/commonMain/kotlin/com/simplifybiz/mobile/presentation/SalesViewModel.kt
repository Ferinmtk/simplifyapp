package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.SalesEntity
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetSalesUseCase
import com.simplifybiz.mobile.domain.UpdateSalesUseCase
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.simplifybiz.mobile.data.LeadershipSystemItem

internal class SalesViewModel(
    private val getSalesUseCase: GetSalesUseCase,
    private val updateSalesUseCase: UpdateSalesUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<SalesEntity>(syncRepository) {

    private val _uiState = MutableStateFlow(SalesEntity())
    val uiState = _uiState.asStateFlow()

    private var debounceJob: Job? = null
    private var lastTypingTime: Long = 0

    init {
        // REACTIVE FLOW: Subscribe to the Single Source of Truth
        viewModelScope.launch {
            getSalesUseCase.observe().collect { entity ->
                // Anti-flicker: Only update if user isn't actively typing
                if (getTimeMillis() - lastTypingTime > 3000) {
                    _uiState.value = entity ?: SalesEntity()
                }
            }
        }

        loadData(
            localLoader = { getSalesUseCase.execute() },
            onDataLoaded = { initialData -> _uiState.value = initialData }
        )
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                getSalesUseCase.execute()
                _validationMessage.send("Update complete.")
            }
        }
    }

    private fun updateUiAndQueuePersist(updated: SalesEntity) {
        _uiState.value = updated
        lastTypingTime = getTimeMillis()

        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(1000)
            persist(updated)
        }
    }

    fun onFunnelChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(salesFunnelStages = text))
    fun onQualificationChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(leadQualificationCriteria = text))
    fun onChannelsChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(salesChannels = text))
    fun onPitchChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(elevatorPitch = text))
    fun onBusinessPurposeChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(businessPurpose = text))
    fun onTeamStructureChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(salesTeamStructure = text))
    fun onGoalsChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(salesGoals = text))
    fun onTrainingPlanChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(salesTrainingPlan = text))
    fun onMetricsChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(salesMetrics = text))
    fun onPriceListChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(priceList = text))
    fun onObjectionProcessChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(objectionProcess = text))
    fun onImplementationStatusChange(value: String) = updateUiAndQueuePersist(_uiState.value.copy(implementationStatus = value))

    private fun persist(entity: SalesEntity, isSubmit: Boolean = false) {
        val prepared = if (entity.uuid.isBlank()) {
            entity.copy(uuid = getTimeMillis().toString(), isDirty = true)
        } else {
            entity.copy(isDirty = true)
        }

        saveData(
            currentData = prepared,
            localSaver = { updateSalesUseCase.execute(it) },
            isSubmit = isSubmit,
            onDataSaved = { /* Managed by Flow observer in init */ }
        )
    }

    /**
     * Explicit save-draft — cancel the pending debounce so we only fire one sync.
     *
     * Same rationale as Leadership / Marketing: without this, a user who types
     * and taps Save Draft within 1s produces two writes (this one + the debounce).
     * UniversalSyncRepository's mutex prevents data corruption but the second
     * POST is pure redundant work.
     */
    fun saveDraft() {
        debounceJob?.cancel()
        persist(_uiState.value.copy(status = "draft"))
    }

    fun submitSales() {
        val current = _uiState.value
        if (current.salesFunnelStages.isBlank()) {
            viewModelScope.launch { _validationMessage.send("Sales funnel is required.") }
            return
        }
        debounceJob?.cancel()
        persist(current.copy(status = "submitted"), isSubmit = true)
    }

    fun addSystem(item: LeadershipSystemItem) {
        val updated = _uiState.value.systemsUsed + item
        updateUiAndQueuePersist(_uiState.value.copy(systemsUsed = updated))
    }

    fun updateSystem(item: LeadershipSystemItem) {
        val updated = _uiState.value.systemsUsed.map { if (it.id == item.id) item else it }
        updateUiAndQueuePersist(_uiState.value.copy(systemsUsed = updated))
    }

    /**
     * Remove a system by its local id.
     *
     * Mirrors Leadership / Marketing removeSystem exactly:
     *
     * If the removed system was already synced (has a remote_id), add that
     * remote_id to deletedSystemRemoteIds so the next sync tells the server
     * to hard-delete the row. Without this, SalesSystemsRepository's
     * preservation loop would revive it on the next push.
     *
     * If the system was only ever local (remote_id == null), just drop it —
     * nothing on the server to clean up.
     */
    fun removeSystem(id: String) {
        val current = _uiState.value
        val removed = current.systemsUsed.firstOrNull { it.id == id }
        val updatedSystems = current.systemsUsed.filterNot { it.id == id }

        val updatedDeletions = if (removed?.remoteId != null) {
            current.deletedSystemRemoteIds + removed.remoteId
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