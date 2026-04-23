package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.MarketingEntity
import com.simplifybiz.mobile.data.MarketingRepository
import kotlinx.coroutines.flow.Flow

internal class GetMarketingUseCase(
    private val repository: MarketingRepository
) {
    suspend fun execute(): MarketingEntity = repository.getMarketing()

    /**
     * Observe Marketing changes reactively.
     */
    fun observe(): Flow<MarketingEntity?> = repository.observeMarketing()
}
