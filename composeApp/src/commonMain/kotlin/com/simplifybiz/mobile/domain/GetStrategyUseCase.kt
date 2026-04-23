package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.StrategyEntity
import com.simplifybiz.mobile.data.StrategyRepository
import kotlinx.coroutines.flow.Flow

internal class GetStrategyUseCase(private val repository: StrategyRepository) {
    suspend fun execute(): StrategyEntity = repository.getStrategy()

    /**
     * Observe Strategy changes reactively.
     */
    fun observe(): Flow<StrategyEntity?> = repository.observeStrategy()
}
