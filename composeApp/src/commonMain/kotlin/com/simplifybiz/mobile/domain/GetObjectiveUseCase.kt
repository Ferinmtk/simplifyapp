package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.ObjectiveEntity
import com.simplifybiz.mobile.data.ObjectiveRepository
import kotlinx.coroutines.flow.Flow

internal class GetObjectiveUseCase(private val repository: ObjectiveRepository) {

    // --- Singleton-style accessors (used by dashboard status) ---

    suspend fun execute(): ObjectiveEntity = repository.getObjective()

    fun observe(): Flow<ObjectiveEntity?> = repository.observeObjective()

    // --- Multi-row accessors (the real Objectives module) ---

    suspend fun executeAll(): List<ObjectiveEntity> = repository.getAllObjectives()

    fun observeAll(): Flow<List<ObjectiveEntity>> = repository.observeAllObjectives()

    suspend fun getByUuid(uuid: String): ObjectiveEntity? = repository.getByUuid(uuid)

    fun observeByUuid(uuid: String): Flow<ObjectiveEntity?> = repository.observeByUuid(uuid)
}