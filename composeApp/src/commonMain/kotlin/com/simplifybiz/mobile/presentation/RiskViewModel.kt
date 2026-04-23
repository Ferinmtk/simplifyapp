package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.LeadershipSystemItem
import com.simplifybiz.mobile.data.RiskEntity
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetRiskUseCase
import com.simplifybiz.mobile.domain.UpdateRiskUseCase
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class RiskViewModel(
    private val getRiskUseCase: GetRiskUseCase,
    private val updateRiskUseCase: UpdateRiskUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<RiskEntity>(syncRepository) {

    private val _uiState = MutableStateFlow(RiskEntity())
    val uiState = _uiState.asStateFlow()

    private var debounceJob: Job? = null
    private var lastTypingTime: Long = 0

    init {
        viewModelScope.launch {
            getRiskUseCase.observe().collect { entity ->
                if (System.currentTimeMillis() - lastTypingTime > 3000) {
                    _uiState.value = entity ?: RiskEntity()
                }
            }
        }

        loadData(
            localLoader = { getRiskUseCase.execute() },
            onDataLoaded = { _uiState.value = it },
            remoteRefreshed = { refreshed ->
                if (System.currentTimeMillis() - lastTypingTime > 3000) {
                    _uiState.value = refreshed
                }
            }
        )
    }

    private fun updateUiAndQueuePersist(updatedEntity: RiskEntity) {
        _uiState.value = updatedEntity
        lastTypingTime = System.currentTimeMillis()

        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(1000)
            persist(updatedEntity)
        }
    }

    fun onBusinessStructureChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(businessStructure = v))
    fun onBusinessRegistrationChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(businessRegistration = v))
    fun onComplianceRequirementsChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(complianceRequirements = v))
    fun onComplianceMonitoringChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(complianceMonitoring = v))
    fun onContractTemplatesChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(contractTemplates = v))
    fun onContractReviewProcessChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(contractReviewProcess = v))
    fun onLegalCounselChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(legalCounsel = v))
    fun onIntellectualPropertyStrategyChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(intellectualPropertyStrategy = v))
    fun onIpRegistrationPlanChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(ipRegistrationPlan = v))
    fun onDataPrivacyPoliciesChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(dataPrivacyPolicies = v))
    fun onEmployeeLegalAgreementsChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(employeeLegalAgreements = v))
    fun onRiskAssessmentChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(riskAssessment = v))
    fun onRiskMitigationPlanChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(riskMitigationPlan = v))
    fun onDisputeResolutionProcessChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(disputeResolutionProcess = v))
    fun onStatusChange(v: String) = updateUiAndQueuePersist(_uiState.value.copy(statusQuoOfImplementation = v))

    fun addSystem(item: LeadershipSystemItem) {
        val updated = _uiState.value.systemsUsed + item
        updateUiAndQueuePersist(_uiState.value.copy(systemsUsed = updated))
    }

    fun updateSystem(item: LeadershipSystemItem) {
        val updated = _uiState.value.systemsUsed.map { if (it.id == item.id) item else it }
        updateUiAndQueuePersist(_uiState.value.copy(systemsUsed = updated))
    }

    /** See ResearchAndDevelopmentViewModel.removeSystem for rationale. */
    fun removeSystem(id: String) {
        val current = _uiState.value
        val removed = current.systemsUsed.firstOrNull { it.id == id }
        val updatedSystems = current.systemsUsed.filterNot { it.id == id }

        viewModelScope.launch {
            var remoteIdToDelete = removed?.remoteId

            if (remoteIdToDelete == null && removed != null) {
                val roomState = runCatching { getRiskUseCase.execute() }.getOrNull()
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

    private fun persist(entity: RiskEntity, isSubmit: Boolean = false) {
        val finalEntity = if (entity.uuid.isBlank()) {
            entity.copy(uuid = getTimeMillis().toString(), isDirty = true)
        } else {
            entity.copy(isDirty = true)
        }

        saveData(
            currentData = finalEntity,
            localSaver = { updateRiskUseCase.execute(it) },
            isSubmit = isSubmit,
            onDataSaved = { }
        )
    }

    fun saveDraft() {
        debounceJob?.cancel()
        persist(_uiState.value.copy(status = "draft"))
    }

    fun submitRisk() {
        debounceJob?.cancel()
        persist(_uiState.value.copy(status = "submitted"), isSubmit = true)
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                getRiskUseCase.execute()
                _validationMessage.send("Update complete.")
            }
        }
    }
}