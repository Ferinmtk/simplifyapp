package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@PublishedApi
interface OperationsDao : BaseDao<OperationsEntity> {

    @Query("SELECT * FROM operations LIMIT 1")
    override suspend fun getEntity(): OperationsEntity?

    /**
     * Reactive stream for the Operations module.
     */
    @Query("SELECT * FROM operations LIMIT 1")
    fun observeEntity(): Flow<OperationsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertEntity(entity: OperationsEntity)
}
