package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class StrategyRepository(
    private val dao: StrategyDao
) {
    /**
     * Exposes the database as a reactive stream.
     * The UI will automatically update when background sync modifies the DB.
     */
    fun observeStrategy(): Flow<StrategyEntity?> = dao.observeEntity()

    suspend fun getStrategy(): StrategyEntity {
        return dao.getEntity() ?: StrategyEntity(uuid = "")
    }

    suspend fun saveStrategy(strategy: StrategyEntity) {
        dao.insertEntity(strategy)
        // Manual push removed. Orchestration is now handled by BaseSyncViewModel.
    }
}
