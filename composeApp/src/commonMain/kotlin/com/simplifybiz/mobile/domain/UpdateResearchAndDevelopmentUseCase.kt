package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.ResearchAndDevelopmentEntity
import com.simplifybiz.mobile.data.ResearchAndDevelopmentRepository

internal class UpdateResearchAndDevelopmentUseCase(private val repository: ResearchAndDevelopmentRepository) {
    suspend fun execute(entity: ResearchAndDevelopmentEntity): ResearchAndDevelopmentEntity {
        repository.saveRD(entity)
        return entity
    }
}
