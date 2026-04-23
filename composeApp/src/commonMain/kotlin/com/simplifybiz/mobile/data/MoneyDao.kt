package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@PublishedApi
// Change from 'interface' to 'internal interface' to match @PublishedApi and BaseDao visibility
interface MoneyDao : BaseDao<MoneyEntity> {

    @Query("SELECT * FROM money LIMIT 1")
    override suspend fun getEntity(): MoneyEntity?

    /**
     * Reactive observer for the Money module.
     * Note: If this shows "never used", it is because the ViewModel hasn't collected it yet.
     */
    @Query("SELECT * FROM money LIMIT 1")
    fun observeEntity(): Flow<MoneyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertEntity(entity: MoneyEntity)

    @Query("DELETE FROM money")
    suspend fun clear()
}
