package com.simplifybiz.mobile.data

interface BaseDao<T : Syncable> {
    suspend fun getEntity(): T?
    suspend fun insertEntity(entity: T)
}
