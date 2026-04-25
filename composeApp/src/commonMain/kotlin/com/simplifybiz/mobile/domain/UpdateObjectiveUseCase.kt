package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.ObjectiveEntity
import com.simplifybiz.mobile.data.ObjectiveRepository

internal class UpdateObjectiveUseCase(private val repository: ObjectiveRepository) {
    /**
     * Saves the objective to the local database. The reactive flow triggers
     * any subscribed UI updates automatically. This is the canonical "persist
     * now" call — every action that mutates an objective should reach here.
     */
    suspend fun execute(objective: ObjectiveEntity): ObjectiveEntity {
        repository.saveObjective(objective)
        return objective
    }
}