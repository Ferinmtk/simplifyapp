package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.OperationsEntity
import com.simplifybiz.mobile.data.OperationsRepository
import kotlinx.coroutines.flow.Flow

internal class GetOperationsUseCase(
    private val repository: OperationsRepository
) {
    suspend fun execute() = repository.getOperations()

    /**
     * Observe the operations data reactively.
     */
    fun observe(): Flow<OperationsEntity?> = repository.observeOperations()
}
