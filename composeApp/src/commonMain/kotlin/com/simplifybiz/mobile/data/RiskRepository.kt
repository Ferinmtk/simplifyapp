package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class RiskRepository(
    private val dao: RiskDao
) {
    /**
     * Exposes the reactive database stream.
     */
    fun observeRisk(): Flow<RiskEntity?> = dao.observeEntity()

    suspend fun getRisk(): RiskEntity {
        return dao.getEntity() ?: RiskEntity()
    }

    suspend fun saveRisk(entity: RiskEntity) {
        dao.insertEntity(entity)
    }
}
