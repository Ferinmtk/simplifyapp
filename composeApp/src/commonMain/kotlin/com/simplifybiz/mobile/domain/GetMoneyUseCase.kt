package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.MoneyEntity
import com.simplifybiz.mobile.data.MoneyRepository
import kotlinx.coroutines.flow.Flow

internal class GetMoneyUseCase(
    private val repository: MoneyRepository
) {
    suspend fun execute() = repository.getMoney()

    /**
     * Reactive stream for the Money module.
     */
    fun observe(): Flow<MoneyEntity?> = repository.observeMoney()
}
