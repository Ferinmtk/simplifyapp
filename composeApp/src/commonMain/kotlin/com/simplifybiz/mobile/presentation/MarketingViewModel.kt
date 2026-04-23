package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.MarketingEntity
import com.simplifybiz.mobile.data.MarketingSystemItem
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetMarketingUseCase
import com.simplifybiz.mobile.domain.UpdateMarketingUseCase
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MarketingViewModel(
    private val getMarketingUseCase: GetMarketingUseCase,
    private val updateMarketingUseCase: UpdateMarketingUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<MarketingEntity>(syncRepository) {

    private val _uiState = MutableStateFlow(MarketingEntity())
    val uiState = _uiState.asStateFlow()

    private var syncDebounceJob: Job? = null

    init {
        viewModelScope.launch {
            getMarketingUseCase.observe().collect { entity ->
                // Only update UI from observer if local has no unsaved changes
                if (!_uiState.value.isDirty) {
                    _uiState.value = entity ?: MarketingEntity()
                }
            }
        }

        loadData(
            localLoader = { getMarketingUseCase.execute() },
            onDataLoaded = { _uiState.value = it },
            remoteRefreshed = { refreshed ->
                // Only overwrite UI with remote if no unsaved local changes
                if (!_uiState.value.isDirty) {
                    _uiState.value = refreshed
                }
            }
        )
    }

    private fun withUuid(entity: MarketingEntity): MarketingEntity {
        return if (entity.uuid.isBlank()) entity.copy(uuid = getTimeMillis().toString()) else entity
    }

    private fun updateUiAndQueueSync(updatedEntity: MarketingEntity) {
        // Always ensure uuid exists and isDirty = true
        val dirty = withUuid(updatedEntity).copy(isDirty = true)
        _uiState.value = dirty

        // Save to DB immediately so isDirty=true is persisted before any sync runs
        viewModelScope.launch {
            updateMarketingUseCase.execute(dirty)
        }

        // Debounce the network push
        syncDebounceJob?.cancel()
        syncDebounceJob = viewModelScope.launch {
            delay(1500)
            saveData(
                currentData = dirty,
                localSaver = { updateMarketingUseCase.execute(it) },
                onDataSaved = { }
            )
        }
    }

    fun onMarketingObjectivesChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(marketingObjectives = text))
    fun onMarketingChannelsRationaleChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(marketingChannelsRationale = text))
    fun onMarketingBudgetChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(marketingBudget = text))
    fun onOneOffCostChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(oneOffCost = text))
    fun onRecurringCostChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(recurringCost = text))
    fun onBudgetFrequencyChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(budgetFrequency = text))
    fun onBrandCoreMessageChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(brandCoreMessage = text))
    fun onBrandToneChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(brandTone = text))
    fun onContentTypesChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(contentTypes = text))
    fun onSuccessMetricsChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(successMetricsKeyMetrics = text))
    fun onOwnersTimeCommitmentChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(ownersTimeCommitment = text))
    fun onOwnersSkillsChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(ownersSkills = text))
    fun onOwnersOutsourcingNeedsChange(text: String) = updateUiAndQueueSync(_uiState.value.copy(ownersOutsourcingNeeds = text))
    fun onProcessStatusChange(status: String) = updateUiAndQueueSync(_uiState.value.copy(processStatus = status))

    fun addSystem(systemOrApplication: String, purpose: String, status: String) {
        val item = MarketingSystemItem(
            id = getTimeMillis().toString(),
            systemOrApplication = systemOrApplication.trim(),
            purpose = purpose.trim(),
            status = status
        )
        updateUiAndQueueSync(_uiState.value.copy(systems = _uiState.value.systems + item))
    }

    fun updateSystem(item: MarketingSystemItem) {
        updateUiAndQueueSync(_uiState.value.copy(systems = _uiState.value.systems.map { if (it.id == item.id) item else it }))
    }

    /**
     * Remove a system by its local id.
     *
     * Mirrors LeadershipViewModel.removeSystem exactly:
     *
     * If the removed system was already synced (has a remote_id), we add that
     * remote_id to deletedSystemRemoteIds so the next sync tells the server
     * to hard-delete the row. Without this, MarketingSystemsRepository's
     * preservation loop would revive it on the next push — exact same bug
     * Leadership had before we fixed it.
     *
     * If the system was only ever local (remote_id == null), we just drop it
     * — nothing on the server to clean up.
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
     *
     * Before this, a user who types and taps Save Draft within 1.5s would
     * produce two writes: this one, then the debounce. The UniversalSyncRepository
     * mutex prevents data corruption but the second POST is pure redundant work.
     */
    fun saveDraft() {
        syncDebounceJob?.cancel()
        val entity = withUuid(_uiState.value).copy(status = "draft", isDirty = true)
        viewModelScope.launch { updateMarketingUseCase.execute(entity) }
    }

    /**
     * Explicit submit — cancel the pending debounce. Same rationale as saveDraft.
     */
    fun submitMarketing() {
        syncDebounceJob?.cancel()
        val entity = withUuid(_uiState.value).copy(status = "submitted", isDirty = true)
        saveData(
            currentData = entity,
            localSaver = { updateMarketingUseCase.execute(it) },
            isSubmit = true,
            onDataSaved = { }
        )
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                // Push dirty local data first so server has latest before we force pull
                syncRepository.pushLocalChanges()
                // Now force pull is safe — our changes are already on the server
                val refreshed = getMarketingUseCase.execute()
                _uiState.value = refreshed
                _validationMessage.send("Update complete.")
            }
        }
    }
}