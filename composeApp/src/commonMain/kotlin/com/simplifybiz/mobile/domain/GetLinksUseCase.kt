package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.LinkEntity
import com.simplifybiz.mobile.data.LinkRepository
import kotlinx.coroutines.flow.Flow

internal class GetLinksUseCase(
    private val repository: LinkRepository
) {
    suspend fun execute(): LinkEntity = repository.getLinks()

    fun observe(): Flow<LinkEntity?> = repository.observeLinks()
}