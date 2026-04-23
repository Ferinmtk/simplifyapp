package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.MoneyEntity
import com.simplifybiz.mobile.data.MoneySystemItem
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetMoneyUseCase
import com.simplifybiz.mobile.domain.UpdateMoneyUseCase
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MoneyViewModel(
    private val getMoneyUseCase: GetMoneyUseCase,
    private val updateMoneyUseCase: UpdateMoneyUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<MoneyEntity>(syncRepository) {

    private val _uiState = MutableStateFlow(MoneyEntity())
    val uiState = _uiState.asStateFlow()

    private var syncDebounceJob: Job? = null

    init {
        // Subscribe to the local DB stream. We use isDirty (NOT a typing-time
        // guard) to decide whether to overwrite UI from the observer.
        //
        // Why isDirty and not lastTypingTime: typing-time aging-out is fine
        // for text fields because every keystroke refreshes it. Systems are
        // different — addSystem/removeSystem update the guard once, then 3s
        // later the guard opens and the next observer emission wipes the
        // systems list. isDirty stays true from the moment the user touches
        // anything until the sync receipt clears it, so it covers both
        // typing AND systems edits with one rule.
        viewModelScope.launch {
            getMoneyUseCase.observe().collect { entity ->
                if (!_uiState.value.isDirty) {
                    _uiState.value = entity ?: MoneyEntity()
                }
            }
        }

        loadData(
            localLoader = { getMoneyUseCase.execute() },
            onDataLoaded = { _uiState.value = it },
            remoteRefreshed = { refreshed ->
                if (!_uiState.value.isDirty) {
                    _uiState.value = refreshed
                }
            }
        )
    }

    private fun withUuid(entity: MoneyEntity): MoneyEntity {
        return if (entity.uuid.isBlank()) entity.copy(uuid = getTimeMillis().toString()) else entity
    }

    /**
     * Update UI, write to DB immediately (with isDirty=true so the observer
     * guard catches subsequent emissions), and queue a debounced network push.
     *
     * The immediate DB write is what makes the isDirty guard work. Without it,
     * when Room re-emits the row (which it does on any write — including a
     * remote sync writing a stale copy), the row would still show isDirty=false,
     * our guard would let it through, and the user's in-progress edits would be
     * wiped. Writing isDirty=true synchronously prevents that race.
     */
    private fun updateUiAndQueueSync(updated: MoneyEntity) {
        val dirty = withUuid(updated).copy(isDirty = true)
        _uiState.value = dirty

        viewModelScope.launch {
            updateMoneyUseCase.execute(dirty)
        }

        syncDebounceJob?.cancel()
        syncDebounceJob = viewModelScope.launch {
            delay(1500)
            saveData(
                currentData = dirty,
                localSaver = { updateMoneyUseCase.execute(it) },
                onDataSaved = { }
            )
        }
    }

    fun onRevenueTrackingSystemChange(v: String)   = updateUiAndQueueSync(_uiState.value.copy(revenueTrackingSystem = v))
    fun onRevenueMonitoringToolsChange(v: String)  = updateUiAndQueueSync(_uiState.value.copy(revenueMonitoringTools = v))
    fun onExpenseCategoriesChange(v: String)       = updateUiAndQueueSync(_uiState.value.copy(expenseCategories = v))
    fun onExpenseApprovalWorkflowChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(expenseApprovalWorkflow = v))
    fun onCostSavingStrategiesChange(v: String)    = updateUiAndQueueSync(_uiState.value.copy(costSavingStrategies = v))
    fun onCashReservePlanChange(v: String)         = updateUiAndQueueSync(_uiState.value.copy(cashReservePlan = v))
    fun onFinancialReportsChange(v: String)        = updateUiAndQueueSync(_uiState.value.copy(financialReports = v))
    fun onStakeholderReportingChange(v: String)    = updateUiAndQueueSync(_uiState.value.copy(stakeholderReporting = v))
    fun onComplianceRequirementsChange(v: String)  = updateUiAndQueueSync(_uiState.value.copy(complianceRequirements = v))
    fun onAuditScheduleChange(v: String)           = updateUiAndQueueSync(_uiState.value.copy(auditSchedule = v))
    fun onReinvestmentCriteriaChange(v: String)    = updateUiAndQueueSync(_uiState.value.copy(reinvestmentCriteria = v))
    fun onFundingOptionsChange(v: String)          = updateUiAndQueueSync(_uiState.value.copy(fundingOptions = v))
    fun onStatusChange(v: String)                  = updateUiAndQueueSync(_uiState.value.copy(statusQuoOfImplementation = v))

    fun addSystem(item: MoneySystemItem) {
        val updated = _uiState.value.systems + item
        updateUiAndQueueSync(_uiState.value.copy(systems = updated))
    }

    fun updateSystem(item: MoneySystemItem) {
        val updated = _uiState.value.systems.map { if (it.id == item.id) item else it }
        updateUiAndQueueSync(_uiState.value.copy(systems = updated))
    }

    fun removeSystem(id: String) {
        val current = _uiState.value
        val removed = current.systems.firstOrNull { it.id == id }
        val updatedSystems = current.systems.filterNot { it.id == id }

        // If the removed system was already synced to the server (has a
        // remote_id), queue it for server-side deletion. Otherwise the
        // preservation loop in MoneySystemsRepository::sync_systems would
        // revive it on the next push.
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

    fun saveDraft() {
        syncDebounceJob?.cancel()
        val entity = withUuid(_uiState.value).copy(status = "draft", isDirty = true)
        viewModelScope.launch { updateMoneyUseCase.execute(entity) }
    }

    fun submitMoney() {
        syncDebounceJob?.cancel()
        val entity = withUuid(_uiState.value).copy(status = "submitted", isDirty = true)
        saveData(
            currentData = entity,
            localSaver = { updateMoneyUseCase.execute(it) },
            isSubmit = true,
            onDataSaved = { }
        )
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                // Push dirty local data first so server has latest before pulling.
                syncRepository.pushLocalChanges()
                val refreshed = getMoneyUseCase.execute()
                _uiState.value = refreshed
                _validationMessage.send("Update complete.")
            }
        }
    }
}