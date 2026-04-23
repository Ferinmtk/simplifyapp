package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.LeadershipEntity
import com.simplifybiz.mobile.data.LeadershipSystemItem
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetLeadershipUseCase
import com.simplifybiz.mobile.domain.UpdateLeadershipUseCase
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class LeadershipViewModel(
    private val getLeadershipUseCase: GetLeadershipUseCase,
    private val updateLeadershipUseCase: UpdateLeadershipUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<LeadershipEntity>(syncRepository) {

    private val _uiState = MutableStateFlow(LeadershipEntity())
    val uiState = _uiState.asStateFlow()

    private var debounceJob: Job? = null
    private var lastTypingTime: Long = 0

    init {
        // 1. Reactive Observation: Subscribe to Database stream
        viewModelScope.launch {
            getLeadershipUseCase.observe().collect { entity ->
                // Anti-flicker: Only update if user hasn't typed in 3 seconds
                if (getTimeMillis() - lastTypingTime > 3000) {
                    _uiState.value = entity ?: LeadershipEntity()
                }
            }
        }

        // 2. Load local data immediately, then sync remote.
        loadData(
            localLoader = { getLeadershipUseCase.execute() },
            onDataLoaded = { _uiState.value = it },
            remoteRefreshed = { refreshed ->
                if (getTimeMillis() - lastTypingTime > 3000) {
                    _uiState.value = refreshed
                }
            }
        )
    }

    private fun updateUiAndQueuePersist(updatedEntity: LeadershipEntity) {
        _uiState.value = updatedEntity
        lastTypingTime = getTimeMillis()

        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(1000)
            persist(updatedEntity)
        }
    }

    fun onStrategyReviewScheduleChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(strategyReviewSchedule = v))
    fun onDecisionFrameworkChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(decisionFramework = v))
    fun onDecisionToolsChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(decisionTools = v))
    fun onDelegationPlanChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(delegationPlan = v))
    fun onExpectationSettingChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(expectationSetting = v))
    fun onCommunicationChannelsChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(communicationChannels = v))
    fun onLeadershipTrainingChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(leadershipTraining = v))
    fun onCoachingProgramsChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(coachingPrograms = v))
    fun onLeadershipKpisChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(leadershipKpis = v))
    fun onFeedbackMechanismChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(feedbackMechanism = v))
    fun onChangeManagementFrameworkChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(changeManagementFramework = v))
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
     * Remove a system by its local id.
     *
     * If the removed system was already synced (has a remote_id), we add that
     * remote_id to deletedSystemRemoteIds so the next sync tells the server
     * to hard-delete the row. Without this the server's preservation loop
     * would revive it on the next push.
     *
     * If the system was only ever local (remote_id == null), we just drop it
     * — nothing on the server to clean up.
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

    private fun persist(entity: LeadershipEntity, isSubmit: Boolean = false) {
        val finalEntity = if (entity.uuid.isBlank()) {
            entity.copy(uuid = getTimeMillis().toString(), isDirty = true)
        } else {
            entity.copy(isDirty = true)
        }

        saveData(
            currentData = finalEntity,
            localSaver = { updateLeadershipUseCase.execute(it) },
            isSubmit = isSubmit,
            onDataSaved = { /* Managed by Flow observer in init */ }
        )
    }

    /**
     * Explicit save — cancel any pending debounced persist so we only fire once.
     *
     * Without the cancel, a user who adds a system and taps Save within 1s
     * produces two POST /sync calls: one from here, one from the debounce
     * that was already queued. The mutex in UniversalSyncRepository now
     * serializes them, but there's no point sending both — the second is
     * redundant work.
     */
    fun saveDraft() {
        debounceJob?.cancel()
        persist(_uiState.value.copy(status = "draft"))
    }

    /**
     * Explicit submit — same rationale as saveDraft: cancel the pending
     * debounced persist so Submit fires exactly one sync.
     */
    fun submitLeadership() {
        debounceJob?.cancel()
        persist(_uiState.value.copy(status = "submitted"), isSubmit = true)
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                getLeadershipUseCase.execute()
                _validationMessage.send("Update complete.")
            }
        }
    }
}