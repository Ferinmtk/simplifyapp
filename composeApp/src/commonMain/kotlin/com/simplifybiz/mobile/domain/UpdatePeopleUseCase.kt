package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.PeopleEntity
import com.simplifybiz.mobile.data.PeopleRepository

internal class UpdatePeopleUseCase(
    private val repository: PeopleRepository
) {
    /**
     * Persists the entity to the local database.
     * The Reactive Flow will automatically trigger a UI update upon completion.
     */
    suspend fun execute(entity: PeopleEntity): PeopleEntity {
        repository.savePeople(entity)
        return entity
    }
}
