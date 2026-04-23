package com.simplifybiz.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@PublishedApi
interface PeopleDao : BaseDao<PeopleEntity> {

    @Query("SELECT * FROM people LIMIT 1")
    override suspend fun getEntity(): PeopleEntity?

    /**
     * Reactive observer for the People module.
     */
    @Query("SELECT * FROM people LIMIT 1")
    fun observeEntity(): Flow<PeopleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertEntity(entity: PeopleEntity)

    @Query("DELETE FROM people")
    suspend fun clear()
}
