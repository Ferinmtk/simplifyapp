package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Multi-row DAO for the Objectives module.
 *
 * NOTE: BaseDao.getEntity() / observeEntity() return the FIRST row for
 * compatibility with the dashboard status aggregator and the legacy generic
 * sync path. Real list operations go through getAll / observeAll / getByUuid.
 */
@Dao
interface ObjectiveDao : BaseDao<ObjectiveEntity> {

    // --- Singleton-style accessors used by dashboard + legacy callers ---

    @Query("SELECT * FROM objectives LIMIT 1")
    override suspend fun getEntity(): ObjectiveEntity?

    @Query("SELECT * FROM objectives LIMIT 1")
    fun observeEntity(): Flow<ObjectiveEntity?>

    @Upsert
    override suspend fun insertEntity(entity: ObjectiveEntity)

    // --- Multi-row accessors (the real Objectives module) ---

    @Query("SELECT * FROM objectives ORDER BY dueDate ASC, uuid ASC")
    suspend fun getAllEntities(): List<ObjectiveEntity>

    @Query("SELECT * FROM objectives ORDER BY dueDate ASC, uuid ASC")
    fun observeAllEntities(): Flow<List<ObjectiveEntity>>

    @Query("SELECT * FROM objectives WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): ObjectiveEntity?

    @Query("SELECT * FROM objectives WHERE uuid = :uuid LIMIT 1")
    fun observeByUuid(uuid: String): Flow<ObjectiveEntity?>

    @Query("SELECT * FROM objectives WHERE isDirty = 1")
    suspend fun getAllDirty(): List<ObjectiveEntity>

    @Query("DELETE FROM objectives WHERE uuid = :uuid")
    suspend fun deleteByUuid(uuid: String)

    @Query("DELETE FROM objectives WHERE uuid IN (:uuids)")
    suspend fun deleteAllByUuid(uuids: List<String>)
}