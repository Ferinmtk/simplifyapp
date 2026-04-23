package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@PublishedApi
internal interface MarketingDao : BaseDao<MarketingEntity> {

    @Query("SELECT * FROM marketing LIMIT 1")
    override suspend fun getEntity(): MarketingEntity?

    /**
     * Reactive stream for the Marketing module.
     */
    @Query("SELECT * FROM marketing LIMIT 1")
    fun observeEntity(): Flow<MarketingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertEntity(entity: MarketingEntity)

    @Query("DELETE FROM marketing")
    suspend fun clearMarketing()
}
