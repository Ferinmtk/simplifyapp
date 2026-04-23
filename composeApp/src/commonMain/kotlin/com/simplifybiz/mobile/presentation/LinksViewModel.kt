package com.simplifybiz.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.LinkEntity
import com.simplifybiz.mobile.data.LinkItem
import com.simplifybiz.mobile.data.UniversalSyncRepository
import com.simplifybiz.mobile.domain.GetLinksUseCase
import com.simplifybiz.mobile.domain.UpdateLinksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LinksUiState(
    val items: List<LinkItem> = emptyList(),
    val searchQuery: String = ""
) {
    val filtered: List<LinkItem>
        get() = if (searchQuery.isBlank()) items
        else items.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.url.contains(searchQuery, ignoreCase = true)
        }
}

internal class LinksViewModel(
    private val getLinksUseCase: GetLinksUseCase,
    syncRepository: UniversalSyncRepository
) : BaseSyncViewModel<LinkEntity>(syncRepository) {

    private val _uiState = MutableStateFlow(LinksUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Observe Room DB — updates automatically when sync pulls new data
        viewModelScope.launch {
            getLinksUseCase.observe().collect { entity ->
                entity?.let { _uiState.value = _uiState.value.copy(items = it.items) }
            }
        }

        // Load local data immediately on start
        loadData(
            localLoader = { getLinksUseCase.execute() },
            onDataLoaded = { _uiState.value = _uiState.value.copy(items = it.items) }
        )
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun refresh() {
        viewModelScope.launch {
            runForcedSync {
                val refreshed = getLinksUseCase.execute()
                _uiState.value = _uiState.value.copy(items = refreshed.items)
                _validationMessage.send("Links updated.")
            }
        }
    }
}