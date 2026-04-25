package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.ObjectiveRepository

internal class DeleteObjectiveUseCase(private val repository: ObjectiveRepository) {
    /**
     * Deletes the objective from the local DB. The next push will not include
     * it; the next pull won't find it either, so server reconciliation is
     * implicit. If you need to delete an objective that already exists on the
     * server, do it from the webapp — mobile-side delete only removes the
     * local copy and any pending dirty state.
     */
    suspend fun execute(uuid: String) {
        repository.deleteByUuid(uuid)
    }
}