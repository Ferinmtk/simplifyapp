package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.StrategyEntity
import com.simplifybiz.mobile.data.StrategyRepository

internal class UpdateStrategyUseCase(
    private val repository: StrategyRepository
) {
    /**
     * Saves the entity locally.
     * UI state is managed independently by the database observer in the ViewModel.
     */
    suspend fun execute(entity:StrategyEntity): StrategyEntity {
        repository.saveStrategy(entity)
        return entity
    }
}
