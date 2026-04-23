package com.simplifybiz.mobile.data

import kotlinx.serialization.Serializable

@Serializable
data class WPUser(
    val id: Int,
    val name: String,
    val slug: String,
    val email: String? = null,
    val roles: List<String>? = emptyList(),
    val avatar_urls: Map<String, String>? = null
)
