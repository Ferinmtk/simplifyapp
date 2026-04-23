package com.simplifybiz.mobile.data

internal interface BaseDao<T : Syncable> {
    suspend fun getEntity(): T?
    suspend fun insertEntity(entity: T)
}
