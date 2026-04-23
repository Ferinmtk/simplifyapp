package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.ResearchAndDevelopmentRepository
import com.simplifybiz.mobile.data.ResearchAndDevelopmentEntity
import kotlinx.coroutines.flow.Flow

internal class GetResearchAndDevelopmentUseCase(private val repository: ResearchAndDevelopmentRepository) {
    suspend fun execute(): ResearchAndDevelopmentEntity = repository.getRD()

    fun observe(): Flow<ResearchAndDevelopmentEntity?> = repository.observeRD()
}
