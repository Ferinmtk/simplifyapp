package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@PublishedApi
interface RiskDao : BaseDao<RiskEntity> {

    @Query("SELECT * FROM risks LIMIT 1")
    override suspend fun getEntity(): RiskEntity?

    /**
     * Reactive stream for the Risk module.
     */
    @Query("SELECT * FROM risks LIMIT 1")
    fun observeEntity(): Flow<RiskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertEntity(entity: RiskEntity)
}