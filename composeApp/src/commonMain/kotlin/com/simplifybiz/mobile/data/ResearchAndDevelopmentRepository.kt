package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class ResearchAndDevelopmentRepository(
    private val dao: ResearchAndDevelopmentDao
) {
    /**
     * Exposes the database as a reactive stream.
     */
    fun observeRD(): Flow<ResearchAndDevelopmentEntity?> = dao.observeEntity()

    suspend fun getRD(): ResearchAndDevelopmentEntity {
        return dao.getEntity() ?: ResearchAndDevelopmentEntity()
    }

    suspend fun saveRD(entity: ResearchAndDevelopmentEntity) {
        dao.insertEntity(entity)
    }
}
