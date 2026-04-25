package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.ObjectiveEntity
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.DeleteObjectiveUseCase
import com.simplifybiz.mobile.domain.GetObjectiveUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Browses the full list of objectives. Reactive — any local change (sync,
 * edit on another screen, deletion) reflects immediately.
 */
internal class ObjectivesListViewModel(
    private val getObjectiveUseCase: GetObjectiveUseCase,
    private val deleteObjectiveUseCase: DeleteObjectiveUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<ObjectiveEntity>(syncRepository) {

    val objectives: StateFlow<List<ObjectiveEntity>> =
        getObjectiveUseCase.observeAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        // Pull fresh list on entry so a brand-new device sees server state.
        viewModelScope.launch {
            try { syncRepository.fetchRemoteChanges(force = false) } catch (_: Exception) {}
        }
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                _validationMessage.send("Updated.")
            }
        }
    }

    fun deleteObjective(uuid: String) {
        viewModelScope.launch {
            deleteObjectiveUseCase.execute(uuid)
            // No server push for local-only delete. Server-side delete must be
            // done from the webapp.
        }
    }
}