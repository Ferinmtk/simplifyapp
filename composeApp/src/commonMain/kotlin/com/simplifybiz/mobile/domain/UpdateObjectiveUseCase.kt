package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.ObjectiveEntity
import com.simplifybiz.mobile.data.ObjectiveRepository

internal class UpdateObjectiveUseCase(private val repository: ObjectiveRepository) {
    /**
     * Saves the objective to the local database.
     * The reactive flow triggers the UI update automatically.
     */
    suspend fun execute(objective: ObjectiveEntity): ObjectiveEntity {
        repository.saveObjective(objective)
        return objective
    }
}
