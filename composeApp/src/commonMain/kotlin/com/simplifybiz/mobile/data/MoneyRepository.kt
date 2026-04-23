package com.simplifybiz.mobile.data

import kotlinx.coroutines.flow.Flow

internal class MoneyRepository(
    private val dao: MoneyDao
) {
    fun observeMoney(): Flow<MoneyEntity?> = dao.observeEntity()

    suspend fun getMoney(): MoneyEntity = dao.getEntity() ?: MoneyEntity()

    suspend fun saveMoney(entity: MoneyEntity) = dao.insertEntity(entity)
}
