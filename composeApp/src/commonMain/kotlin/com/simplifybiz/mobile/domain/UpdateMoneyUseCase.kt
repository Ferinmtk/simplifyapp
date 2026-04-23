package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.MoneyEntity
import com.simplifybiz.mobile.data.MoneyRepository

internal class UpdateMoneyUseCase(
    private val repository: MoneyRepository
) {
    /**
     * Saves the entity locally.
     * UI state is managed independently by the database observer in the ViewModel.
     */
    suspend fun execute(entity: MoneyEntity): MoneyEntity {
        repository.saveMoney(entity)
        return entity
    }
}
