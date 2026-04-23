package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.MarketingEntity
import com.simplifybiz.mobile.data.MarketingRepository

internal class UpdateMarketingUseCase(
    private val repository: MarketingRepository
) {
    /**
     * Persists the entity to the local database.
     * The Reactive Flow will automatically trigger a UI update.
     */
    suspend fun execute(entity: MarketingEntity): MarketingEntity {
        repository.saveMarketing(entity)
        return entity
    }
}
