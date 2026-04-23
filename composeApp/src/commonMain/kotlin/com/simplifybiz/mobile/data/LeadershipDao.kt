package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@PublishedApi
interface LeadershipDao : BaseDao<LeadershipEntity> {

    @Query("SELECT * FROM leadership LIMIT 1")
    override suspend fun getEntity(): LeadershipEntity?

    /**
     * Reactive stream for the Leadership module.
     */
    @Query("SELECT * FROM leadership LIMIT 1")
    fun observeEntity(): Flow<LeadershipEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertEntity(entity: LeadershipEntity)
}
