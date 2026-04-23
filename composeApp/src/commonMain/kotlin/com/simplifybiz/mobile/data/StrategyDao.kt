package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@PublishedApi
interface StrategyDao : BaseDao<StrategyEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertEntity(entity: StrategyEntity)

    @Query("SELECT * FROM strategies LIMIT 1")
    override suspend fun getEntity(): StrategyEntity?

    /**
     * Reactive stream for the Strategy module.
     */
    @Query("SELECT * FROM strategies LIMIT 1")
    fun observeEntity(): Flow<StrategyEntity?>
}
