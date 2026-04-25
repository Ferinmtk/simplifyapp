package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class ObjectiveRepository(
    private val dao: ObjectiveDao
) {
    // --- Singleton accessors (dashboard / legacy) ---

    fun observeObjective(): Flow<ObjectiveEntity?> = dao.observeEntity()

    suspend fun getObjective(): ObjectiveEntity {
        return dao.getEntity() ?: ObjectiveEntity()
    }

    // --- Multi-row accessors ---

    suspend fun getAllObjectives(): List<ObjectiveEntity> = dao.getAllEntities()

    fun observeAllObjectives(): Flow<List<ObjectiveEntity>> = dao.observeAllEntities()

    suspend fun getByUuid(uuid: String): ObjectiveEntity? = dao.getByUuid(uuid)

    fun observeByUuid(uuid: String): Flow<ObjectiveEntity?> = dao.observeByUuid(uuid)

    suspend fun getAllDirty(): List<ObjectiveEntity> = dao.getAllDirty()

    suspend fun saveObjective(objective: ObjectiveEntity) {
        dao.insertEntity(objective)
    }

    suspend fun deleteByUuid(uuid: String) {
        dao.deleteByUuid(uuid)
    }

    suspend fun deleteAllByUuid(uuids: List<String>) {
        dao.deleteAllByUuid(uuids)
    }
}