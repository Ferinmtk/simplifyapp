package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class LeadershipRepository(
    private val dao: LeadershipDao
) {
    /**
     * Exposes the reactive database stream.
     */
    fun observeLeadership(): Flow<LeadershipEntity?> = dao.observeEntity()

    suspend fun getLeadership(): LeadershipEntity {
        return dao.getEntity() ?: LeadershipEntity(uuid = "")
    }

    suspend fun saveLeadership(entity: LeadershipEntity) {
        dao.insertEntity(entity)
    }
}
