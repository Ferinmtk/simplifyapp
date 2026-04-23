package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.LeadershipEntity
import com.simplifybiz.mobile.data.LeadershipRepository

internal class UpdateLeadershipUseCase(
    private val repository: LeadershipRepository
) {
    suspend fun execute(entity: LeadershipEntity): LeadershipEntity {
        repository.saveLeadership(entity)
        return entity
    }
}
