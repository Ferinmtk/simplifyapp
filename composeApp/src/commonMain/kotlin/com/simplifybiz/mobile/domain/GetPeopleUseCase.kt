package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.PeopleEntity
import com.simplifybiz.mobile.data.PeopleRepository
import kotlinx.coroutines.flow.Flow

internal class GetPeopleUseCase(
    private val repository: PeopleRepository
) {
    suspend fun execute() = repository.getPeople()

    /**
     * Reactive stream for the People module.
     */
    fun observe(): Flow<PeopleEntity?> = repository.observePeople()
}
