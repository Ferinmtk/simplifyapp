package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class LinkRepository(
    private val dao: LinkDao
) {
    fun observeLinks(): Flow<LinkEntity?> = dao.observeEntity()

    suspend fun getLinks(): LinkEntity {
        return dao.getEntity() ?: LinkEntity()
    }

    suspend fun saveLinks(entity: LinkEntity) {
        dao.insertEntity(entity)
    }
}