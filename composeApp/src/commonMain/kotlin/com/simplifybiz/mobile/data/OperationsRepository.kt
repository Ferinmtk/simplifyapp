package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class OperationsRepository(
    private val dao: OperationsDao
) {
    /**
     * Exposes the database as a reactive stream.
     */
    fun observeOperations(): Flow<OperationsEntity?> = dao.observeEntity()

    suspend fun getOperations(): OperationsEntity {
        return dao.getEntity() ?: OperationsEntity()
    }

    suspend fun saveOperations(entity: OperationsEntity) {
        dao.insertEntity(entity)
    }
}
