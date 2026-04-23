package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.ObjectiveEntity
import com.simplifybiz.mobile.data.ObjectiveRepository
import kotlinx.coroutines.flow.Flow

internal class GetObjectiveUseCase(private val repository: ObjectiveRepository) {
    suspend fun execute(): ObjectiveEntity = repository.getObjective()

    suspend fun executeAll(): List<ObjectiveEntity> = repository.getAllObjectives()

    fun observe(): Flow<ObjectiveEntity?> = repository.observeObjective()
}
