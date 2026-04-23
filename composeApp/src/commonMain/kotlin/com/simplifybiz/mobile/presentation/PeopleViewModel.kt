package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.PeopleEntity
import com.simplifybiz.mobile.data.PeopleSystemItem
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetPeopleUseCase
import com.simplifybiz.mobile.domain.UpdatePeopleUseCase
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PeopleViewModel(
    private val getPeopleUseCase: GetPeopleUseCase,
    private val updatePeopleUseCase: UpdatePeopleUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<PeopleEntity>(syncRepository) {

    private val _uiState = MutableStateFlow(PeopleEntity())
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
        // a single rule. Same pattern Operations uses.
        viewModelScope.launch {
            getPeopleUseCase.observe().collect { entity ->
                if (!_uiState.value.isDirty) {
                    _uiState.value = entity ?: PeopleEntity()
                }
            }
        }

        // Same isDirty guard for the remote-refresh callback. Without this,
        // BaseSyncViewModel.loadData would fall through to onDataLoaded for the
        // remote-refresh result, which blindly assigns to _uiState with no guard.
        // That overwrites in-flight typing AND in-flight systems edits.
        loadData(
            localLoader = { getPeopleUseCase.execute() },
            onDataLoaded = { _uiState.value = it },
            remoteRefreshed = { refreshed ->
                if (!_uiState.value.isDirty) {
                    _uiState.value = refreshed
                }
            }
        )
    }

    private fun withUuid(entity: PeopleEntity): PeopleEntity {
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
     * the user's edits are safe until the sync receipt clears dirty in
     * updateWithSyncReceipt.
     */
    private fun updateUiAndQueueSync(updated: PeopleEntity) {
        val dirty = withUuid(updated).copy(isDirty = true)
        _uiState.value = dirty

        // Persist isDirty=true to Room immediately so the observer guard works.
        viewModelScope.launch {
            updatePeopleUseCase.execute(dirty)
        }

        // Debounce the network push.
        syncDebounceJob?.cancel()
        syncDebounceJob = viewModelScope.launch {
            delay(1500)
            saveData(
                currentData = dirty,
                localSaver = { updatePeopleUseCase.execute(it) },
                onDataSaved = { }
            )
        }
    }

    fun onJobRolesChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(jobRolesAndSkills = v))
    fun onRecruitmentChannelsChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(recruitmentChannels = v))
    fun onHiringProcessChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(hiringProcess = v))
    fun onOnboardingPlanChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(onboardingPlan = v))
    fun onOnboardingTrainingChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(onboardingTraining = v))
    fun onTrainingProgramsChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(trainingPrograms = v))
    fun onMentorshipProgramsChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(mentorshipPrograms = v))
    fun onPerformanceGoalsChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(performanceGoals = v))
    fun onPerformanceReviewScheduleChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(performanceReviewSchedule = v))
    fun onCompensationPackagesChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(compensationPackages = v))
    fun onRetentionInitiativesChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(retentionInitiatives = v))
    fun onSuccessionPlanChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(successionPlan = v))
    fun onLeadershipDevelopmentPlansChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(leadershipDevelopmentPlans = v))
    fun onEmployeeEngagementMethodsChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(employeeEngagementMethods = v))
    fun onFeedbackActionPlanChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(feedbackActionPlan = v))
    fun onStatusChange(v: String) = updateUiAndQueueSync(_uiState.value.copy(statusQuoOfImplementation = v))

    fun addSystem(item: PeopleSystemItem) {
        val updated = _uiState.value.systems + item
        updateUiAndQueueSync(_uiState.value.copy(systems = updated))
    }

    fun updateSystem(item: PeopleSystemItem) {
        val updated = _uiState.value.systems.map { if (it.id == item.id) item else it }
        updateUiAndQueueSync(_uiState.value.copy(systems = updated))
    }

    /**
     * Remove a system by its local id.
     *
     * If the removed system was already synced (has a remote_id), add that
     * remote_id to deletedSystemRemoteIds so the next sync tells the server
     * to hard-delete the row. Without this, PeopleSystemsRepository's
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
        viewModelScope.launch { updatePeopleUseCase.execute(entity) }
    }

    fun submitPeople() {
        syncDebounceJob?.cancel()
        val entity = withUuid(_uiState.value).copy(status = "submitted", isDirty = true)
        saveData(
            currentData = entity,
            localSaver = { updatePeopleUseCase.execute(it) },
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
                val refreshed = getPeopleUseCase.execute()
                _uiState.value = refreshed
                _validationMessage.send("Update complete.")
            }
        }
    }
}