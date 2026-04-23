package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class MarketingRepository(
    private val dao: MarketingDao
) {
    /**
     * Exposes the reactive database stream.
     */
    fun observeMarketing(): Flow<MarketingEntity?> = dao.observeEntity()

    suspend fun getMarketing(): MarketingEntity {
        return dao.getEntity() ?: MarketingEntity()
    }

    suspend fun saveMarketing(entity: MarketingEntity) {
        dao.insertEntity(entity)
    }
}
