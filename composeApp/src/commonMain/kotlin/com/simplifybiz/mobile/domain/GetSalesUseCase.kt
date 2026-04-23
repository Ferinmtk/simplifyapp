package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.SalesEntity
import com.simplifybiz.mobile.data.SalesRepository
import kotlinx.coroutines.flow.Flow

internal class GetSalesUseCase(
    private val repository: SalesRepository
) {
    suspend fun execute(): SalesEntity = repository.getSales()

    fun observe(): Flow<SalesEntity?> = repository.observeSales()
}
