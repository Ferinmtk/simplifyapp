package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.RiskEntity
import com.simplifybiz.mobile.data.RiskRepository

internal class UpdateRiskUseCase(private val repository: RiskRepository) {
    /**
     * Saves the risk entity locally.
     * The reactive stream handles the UI update.
     */
    suspend fun execute(entity: RiskEntity): RiskEntity {
        repository.saveRisk(entity)
        return entity
    }
}
