package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.StrategyEntity
import com.simplifybiz.mobile.data.TargetMarketItem
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetStrategyUseCase
import com.simplifybiz.mobile.domain.UpdateStrategyUseCase
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class StrategyViewModel(
    private val getStrategyUseCase: GetStrategyUseCase,
    private val updateStrategyUseCase: UpdateStrategyUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<StrategyEntity>(syncRepository) {

    private val _uiState = MutableStateFlow(StrategyEntity())
    val uiState = _uiState.asStateFlow()

    private var debounceJob: Job? = null
    private var lastTypingTime: Long = 0

    init {
        // Reactive observation of the Single Source of Truth
        viewModelScope.launch {
            getStrategyUseCase.observe().collect { entity ->
                // Prevent overwriting active input
                if (getTimeMillis() - lastTypingTime > 3000) {
                    _uiState.value = entity ?: StrategyEntity()
                }
            }
        }

        loadData(
            localLoader = { getStrategyUseCase.execute() },
            onDataLoaded = { _uiState.value = it }
        )
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                getStrategyUseCase.execute()
                _validationMessage.send("Update complete.")
            }
        }
    }

    private fun updateUiAndQueuePersist(updatedEntity: StrategyEntity) {
        _uiState.value = updatedEntity
        lastTypingTime = getTimeMillis()

        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(1500)
            persist(updatedEntity)
        }
    }

    fun onPurposeChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(purpose = text))
    fun onValuesChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(coreValues = text))
    fun onSolutionsChange(text: String) = updateUiAndQueuePersist(_uiState.value.copy(solutions = text))

    fun onLeadNameChange(section: String, firstName: String? = null, lastName: String? = null) {
        val current = _uiState.value
        val updated = when (section) {
            "vision" -> current.copy(leadershipFirst = firstName ?: current.leadershipFirst, leadershipLast = lastName ?: current.leadershipLast)
            "marketing" -> current.copy(marketingFirst = firstName ?: current.marketingFirst, marketingLast = lastName ?: current.marketingLast)
            "sales" -> current.copy(salesFirst = firstName ?: current.salesFirst, salesLast = lastName ?: current.salesLast)
            "operations" -> current.copy(operationsFirst = firstName ?: current.operationsFirst, operationsLast = lastName ?: current.operationsLast)
            "systems" -> current.copy(systemsFirst = firstName ?: current.systemsFirst, systemsLast = lastName ?: current.systemsLast)
            else -> current
        }
        updateUiAndQueuePersist(updated)
    }

    fun addTargetMarket(item: TargetMarketItem) {
        val updatedList = _uiState.value.targetMarkets + item
        updateUiAndQueuePersist(_uiState.value.copy(targetMarkets = updatedList))
    }

    fun updateTargetMarket(item: TargetMarketItem) {
        val updatedList = _uiState.value.targetMarkets.map { if (it.id == item.id) item else it }
        updateUiAndQueuePersist(_uiState.value.copy(targetMarkets = updatedList))
    }

    fun removeTargetMarket(id: String) {
        val current = _uiState.value
        val itemToDelete = current.targetMarkets.find { it.id == id }
        val updatedList = current.targetMarkets.filterNot { it.id == id }
        val updatedDeletions = current.deletedTargetMarketIds.toMutableList()
        itemToDelete?.remoteId?.let { updatedDeletions.add(it) }
        updateUiAndQueuePersist(current.copy(targetMarkets = updatedList, deletedTargetMarketIds = updatedDeletions))
    }

    fun toggleLeadSection(section: String, isVisible: Boolean) {
        val current = _uiState.value
        val updated = when (section) {
            "marketing" -> current.copy(hasMarketingLead = isVisible)
            "sales" -> current.copy(hasSalesLead = isVisible)
            "operations" -> current.copy(hasOperationsLead = isVisible)
            "systems" -> current.copy(hasSystemsLead = isVisible)
            else -> current
        }
        updateUiAndQueuePersist(updated)
    }

    fun onBudgetChange(marketing: Int? = null, sales: Int? = null, ops: Int? = null, admin: Int? = null) {
        val current = _uiState.value
        val updated = current.copy(
            budgetMarketing = marketing ?: current.budgetMarketing,
            budgetSales = sales ?: current.budgetSales,
            budgetOperations = ops ?: current.budgetOperations,
            budgetAdmin = admin ?: current.budgetAdmin
        )
        updateUiAndQueuePersist(updated)
    }

    private fun persist(entity: StrategyEntity, isSubmit: Boolean = false) {
        val preparedEntity = if (entity.uuid.isBlank()) {
            entity.copy(uuid = getTimeMillis().toString(), isDirty = true)
        } else {
            entity.copy(isDirty = true)
        }

        saveData(
            currentData = preparedEntity,
            localSaver = { updateStrategyUseCase.execute(it) },
            isSubmit = isSubmit,
            onDataSaved = { /* Managed by Flow observer in init */ }
        )
    }

    fun saveDraft() = persist(_uiState.value.copy(status = "draft"))

    fun submitStrategy() {
        val current = _uiState.value
        if (current.purpose.isBlank()) {
            viewModelScope.launch { _validationMessage.send("Purpose is required.") }
            return
        }
        persist(current.copy(status = "submitted"), isSubmit = true)
    }
}