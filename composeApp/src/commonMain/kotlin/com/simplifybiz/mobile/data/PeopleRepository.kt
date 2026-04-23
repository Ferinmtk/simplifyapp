package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class PeopleRepository(
    private val dao: PeopleDao
) {
    fun observePeople(): Flow<PeopleEntity?> = dao.observeEntity()

    suspend fun getPeople(): PeopleEntity = dao.getEntity() ?: PeopleEntity()

    suspend fun savePeople(entity: PeopleEntity) = dao.insertEntity(entity)
}
