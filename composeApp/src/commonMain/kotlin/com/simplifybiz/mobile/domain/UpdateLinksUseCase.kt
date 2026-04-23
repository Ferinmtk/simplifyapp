package com.simplifybiz.mobile.domain

import com.simplifybiz.mobile.data.LinkEntity
import com.simplifybiz.mobile.data.LinkRepository

internal class UpdateLinksUseCase(
    private val repository: LinkRepository
) {
    suspend fun execute(entity: LinkEntity): LinkEntity {
        repository.saveLinks(entity)
        return entity
    }
}