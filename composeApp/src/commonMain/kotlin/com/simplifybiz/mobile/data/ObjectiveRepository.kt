package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class ObjectiveRepository(
    private val dao: ObjectiveDao
) {
    fun observeObjective(): Flow<ObjectiveEntity?> = dao.observeEntity()

    suspend fun getObjective(): ObjectiveEntity {
        return dao.getEntity() ?: ObjectiveEntity()
    }

    suspend fun getAllObjectives(): List<ObjectiveEntity> {
        return dao.getAllEntities()
    }

    suspend fun saveObjective(objective: ObjectiveEntity) {
        dao.insertEntity(objective)
    }
}
