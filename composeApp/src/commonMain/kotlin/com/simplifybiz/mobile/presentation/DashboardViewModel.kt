package com.simplifybiz.mobile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.SessionManager
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.data.UserDao
import com.simplifybiz.mobile.data.UserEntity
import com.simplifybiz.mobile.domain.GetLeadershipUseCase
import com.simplifybiz.mobile.domain.GetMarketingUseCase
import com.simplifybiz.mobile.domain.GetMoneyUseCase
import com.simplifybiz.mobile.domain.GetObjectiveUseCase
import com.simplifybiz.mobile.domain.GetOperationsUseCase
import com.simplifybiz.mobile.domain.GetPeopleUseCase
import com.simplifybiz.mobile.domain.GetResearchAndDevelopmentUseCase
import com.simplifybiz.mobile.domain.GetRiskUseCase
import com.simplifybiz.mobile.domain.GetSalesUseCase
import com.simplifybiz.mobile.domain.GetStrategyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.simplifybiz.mobile.util.ioDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class DashboardViewModel(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val syncRepository: UniversalSyncRepository,
    private val getStrategyUseCase: GetStrategyUseCase,
    private val getLeadershipUseCase: GetLeadershipUseCase,
    private val getMarketingUseCase: GetMarketingUseCase,
    private val getSalesUseCase: GetSalesUseCase,
    private val getOperationsUseCase: GetOperationsUseCase,
    private val getPeopleUseCase: GetPeopleUseCase,
    private val getMoneyUseCase: GetMoneyUseCase,
    private val getObjectiveUseCase: GetObjectiveUseCase,
    private val getRDUseCase: GetResearchAndDevelopmentUseCase,
    private val getRiskUseCase: GetRiskUseCase
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user = _user.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError = _syncError.asStateFlow()

    private val _strategyStatus = MutableStateFlow("empty")
    val strategyStatus = _strategyStatus.asStateFlow()

    private val _leadershipStatus = MutableStateFlow("empty")
    val leadershipStatus = _leadershipStatus.asStateFlow()

    private val _marketingStatus = MutableStateFlow("empty")
    val marketingStatus = _marketingStatus.asStateFlow()

    private val _salesStatus = MutableStateFlow("empty")
    val salesStatus = _salesStatus.asStateFlow()

    private val _operationsStatus = MutableStateFlow("empty")
    val operationsStatus = _operationsStatus.asStateFlow()

    private val _peopleStatus = MutableStateFlow("empty")
    val peopleStatus = _peopleStatus.asStateFlow()

    private val _moneyStatus = MutableStateFlow("empty")
    val moneyStatus = _moneyStatus.asStateFlow()

    private val _objectiveStatus = MutableStateFlow("empty")
    val objectiveStatus = _objectiveStatus.asStateFlow()

    private val _rdStatus = MutableStateFlow("empty")
    val rdStatus = _rdStatus.asStateFlow()

    private val _riskStatus = MutableStateFlow("empty")
    val riskStatus = _riskStatus.asStateFlow()

    init {
        // Step 1: Show local cached data immediately (fast — no network)
        loadLocalData()
        // Step 2: Sync with server in background, then refresh UI
        syncAndRefresh()
    }

    /**
     * Read from local Room DB only. Called immediately on init
     * so the dashboard renders instantly without waiting for network.
     */
    private fun loadLocalData() {
        viewModelScope.launch {
            applyLoadResult(fetchLocalData())
        }
    }

    /**
     * Pull all modules from server, save to DB, then reload local data.
     * forcePull = true ensures remote data always overwrites stale local cache
     * UNLESS the local data has unsaved changes (isDirty = true) — the sync
     * layer handles that automatically.
     */
    /** Called from DashboardScreen via LaunchedEffect — triggers a full sync and refresh. */
    fun loadData() = syncAndRefresh()

    fun syncAndRefresh() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null

            try {
                withContext(ioDispatcher) {
                    syncRepository.syncAll(forcePull = true)
                }
                // Reload UI with freshly synced data
                applyLoadResult(fetchLocalData())
            } catch (e: Exception) {
                _syncError.value = "Sync failed. Check your connection."
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private suspend fun fetchLocalData(): DashboardLoadResult = withContext(ioDispatcher) {
        DashboardLoadResult(
            user = runCatching { userDao.getUser() }.getOrNull(),
            strategyStatus = runCatching { getStrategyUseCase.execute() }.getOrNull()
                .let { normalizeStatus(it?.uuid ?: "", it?.status ?: "") },
            leadershipStatus = runCatching { getLeadershipUseCase.execute() }.getOrNull()
                .let { normalizeStatus(it?.uuid ?: "", it?.status ?: "") },
            marketingStatus = runCatching { getMarketingUseCase.execute() }.getOrNull()
                .let { normalizeStatus(it?.uuid ?: "", it?.status ?: "") },
            salesStatus = runCatching { getSalesUseCase.execute() }.getOrNull()
                .let { normalizeStatus(it?.uuid ?: "", it?.status ?: "") },
            operationsStatus = runCatching { getOperationsUseCase.execute() }.getOrNull()
                .let { normalizeStatus(it?.uuid ?: "", it?.status ?: "") },
            peopleStatus = runCatching { getPeopleUseCase.execute() }.getOrNull()
                .let { normalizeStatus(it?.uuid ?: "", it?.status ?: "") },
            moneyStatus = runCatching { getMoneyUseCase.execute() }.getOrNull()
                .let { normalizeStatus(it?.uuid ?: "", it?.status ?: "") },
            objectiveStatus = runCatching { getObjectiveUseCase.execute() }.getOrNull()
                .let { normalizeStatus(it?.uuid ?: "", it?.status ?: "") },
            rdStatus = runCatching { getRDUseCase.execute() }.getOrNull()
                .let { normalizeStatus(it?.uuid ?: "", it?.status ?: "") },
            riskStatus = runCatching { getRiskUseCase.execute() }.getOrNull()
                .let { normalizeStatus(it?.uuid ?: "", it?.status ?: "") }
        )
    }

    private fun applyLoadResult(result: DashboardLoadResult) {
        _user.value = result.user
        _strategyStatus.value = result.strategyStatus
        _leadershipStatus.value = result.leadershipStatus
        _marketingStatus.value = result.marketingStatus
        _salesStatus.value = result.salesStatus
        _operationsStatus.value = result.operationsStatus
        _peopleStatus.value = result.peopleStatus
        _moneyStatus.value = result.moneyStatus
        _objectiveStatus.value = result.objectiveStatus
        _rdStatus.value = result.rdStatus
        _riskStatus.value = result.riskStatus
    }

    private fun normalizeStatus(uuid: String, status: String): String {
        if (uuid.isBlank()) return "empty"
        return if (status.trim().lowercase() == "submitted") "submitted" else "draft"
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                runCatching { userDao.clearUser() }
                runCatching { sessionManager.clearSession() }
            }
            onComplete()
        }
    }

    private data class DashboardLoadResult(
        val user: UserEntity?,
        val strategyStatus: String,
        val leadershipStatus: String,
        val marketingStatus: String,
        val salesStatus: String,
        val operationsStatus: String,
        val peopleStatus: String,
        val moneyStatus: String,
        val objectiveStatus: String,
        val rdStatus: String,
        val riskStatus: String
    )
}