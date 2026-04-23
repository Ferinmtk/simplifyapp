package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.OperationsEntity
import com.simplifybiz.mobile.data.OperationsRepository

internal class UpdateOperationsUseCase(
    private val repository: OperationsRepository
) {
    /**
     * Saves the entity to the local database.
     * The Reactive Flow will automatically trigger a UI update once this completes.
     */
    suspend fun execute(entity: OperationsEntity): OperationsEntity {
        repository.saveOperations(entity)
        return entity
    }
}
