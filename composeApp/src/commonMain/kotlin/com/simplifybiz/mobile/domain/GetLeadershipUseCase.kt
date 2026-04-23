package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.LeadershipEntity
import com.simplifybiz.mobile.data.LeadershipRepository
import kotlinx.coroutines.flow.Flow

internal class GetLeadershipUseCase(
    private val repository: LeadershipRepository
) {
    suspend fun execute(): LeadershipEntity {
        return repository.getLeadership()
    }

    fun observe(): Flow<LeadershipEntity?> = repository.observeLeadership()
}
