package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.RiskRepository
import com.simplifybiz.mobile.data.RiskEntity
import kotlinx.coroutines.flow.Flow

internal class GetRiskUseCase(private val repository: RiskRepository) {
    suspend fun execute(): RiskEntity = repository.getRisk()

    fun observe(): Flow<RiskEntity?> = repository.observeRisk()
}
