package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.SalesEntity
import com.simplifybiz.mobile.data.SalesRepository

internal class UpdateSalesUseCase(
    private val repository: SalesRepository
) {
    suspend fun execute(entity: SalesEntity): SalesEntity {
        repository.saveSales(entity)
        return entity
    }
}
