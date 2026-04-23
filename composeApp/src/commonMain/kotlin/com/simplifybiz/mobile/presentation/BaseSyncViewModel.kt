package com.simplifybiz.mobile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.Syncable
import com.simplifybiz.mobile.data.UniversalSyncRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal abstract class BaseSyncViewModel<T : Syncable>(
    protected val syncRepository: UniversalSyncRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    protected val _saveSuccess = Channel<Boolean>()
    val saveSuccess = _saveSuccess.receiveAsFlow()

    protected val _validationMessage = Channel<String>()
    val validationMessage = _validationMessage.receiveAsFlow()

    protected fun loadData(
        localLoader: suspend () -> T,
        onDataLoaded: (T) -> Unit,
        remoteRefreshed: ((T) -> Unit)? = null,
        forceInitialFetch: Boolean = false
    ) {
        viewModelScope.launch {
            val localData = localLoader()
            onDataLoaded(localData)

            try {
                syncRepository.fetchRemoteChanges(force = forceInitialFetch)
                val refreshedData = localLoader()

                if (remoteRefreshed != null) {
                    remoteRefreshed(refreshedData)
                } else {
                    onDataLoaded(refreshedData)
                }
            } catch (_: Exception) { }
        }
    }

    protected suspend fun runForcedSync(onComplete: suspend () -> Unit) {
        _isRefreshing.value = true
        try {
            syncRepository.syncAll(forcePull = true)
            onComplete()
        } finally {
            _isRefreshing.value = false
        }
    }

    protected fun saveData(
        currentData: T,
        localSaver: suspend (T) -> Unit,
        isSubmit: Boolean = false,
        onDataSaved: (T) -> Unit
    ) {
        viewModelScope.launch {
            localSaver(currentData)
            onDataSaved(currentData)

            if (isSubmit) {
                _saveSuccess.send(true)
            }

            try {
                syncRepository.pushLocalChanges()
            } catch (_: Exception) { }
        }
    }
}