package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjectiveDao : BaseDao<ObjectiveEntity> {
    @Query("SELECT * FROM objectives")
    suspend fun getAllEntities(): List<ObjectiveEntity>

    @Query("SELECT * FROM objectives LIMIT 1")
    override suspend fun getEntity(): ObjectiveEntity?

    /**
     * Reactive stream for the Objectives module.
     */
    @Query("SELECT * FROM objectives LIMIT 1")
    fun observeEntity(): Flow<ObjectiveEntity?>

    @Upsert
    override suspend fun insertEntity(entity: ObjectiveEntity)
}
