package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class SalesRepository(
    private val dao: SalesDao
) {
    /**
     * Exposes the reactive database stream.
     */
    fun observeSales(): Flow<SalesEntity?> = dao.observeEntity()

    suspend fun getSales(): SalesEntity {
        return dao.getEntity() ?: SalesEntity(uuid = "")
    }

    suspend fun saveSales(entity: SalesEntity) {
        dao.insertEntity(entity)
        // Manual pushLocalChanges removed; now handled by BaseSyncViewModel orchestration.
    }
}
