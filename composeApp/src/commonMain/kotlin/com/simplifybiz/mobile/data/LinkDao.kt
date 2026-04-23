package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
@PublishedApi
internal interface LinkDao : BaseDao<LinkEntity> {

    @Query("SELECT * FROM links LIMIT 1")
    override suspend fun getEntity(): LinkEntity?

    @Query("SELECT * FROM links LIMIT 1")
    fun observeEntity(): Flow<LinkEntity?>

    @Upsert
    override suspend fun insertEntity(entity: LinkEntity)
}