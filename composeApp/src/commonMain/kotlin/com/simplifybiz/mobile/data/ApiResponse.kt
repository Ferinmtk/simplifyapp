package com.simplifybiz.mobile.data

import kotlinx.serialization.Serializable

@Serializable
internal data class ApiResponse<T>(
    val success: Boolean,
    val data: T
)
