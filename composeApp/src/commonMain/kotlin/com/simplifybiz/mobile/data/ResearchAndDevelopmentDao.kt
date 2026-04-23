package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@PublishedApi
internal interface ResearchAndDevelopmentDao : BaseDao<ResearchAndDevelopmentEntity> {

    @Query("SELECT * FROM research_and_development LIMIT 1")
    override suspend fun getEntity(): ResearchAndDevelopmentEntity?

    /**
     * Reactive stream for the R&D module.
     */
    @Query("SELECT * FROM research_and_development LIMIT 1")
    fun observeEntity(): Flow<ResearchAndDevelopmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertEntity(entity: ResearchAndDevelopmentEntity)
}