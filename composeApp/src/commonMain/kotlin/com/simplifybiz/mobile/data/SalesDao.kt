package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesDao : BaseDao<SalesEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertEntity(entity: SalesEntity)

    @Query("SELECT * FROM sales LIMIT 1")
    override suspend fun getEntity(): SalesEntity?

    /**
     * Reactive stream for the Sales module.
     */
    @Query("SELECT * FROM sales LIMIT 1")
    fun observeEntity(): Flow<SalesEntity?>
}
