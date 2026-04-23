package com.simplifybiz.mobile.data

import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class UserEntity(
    val username: String,
    val email: String,
    @SerialName("display_name") val displayName: String,
    val roles: String,
    @SerialName("avatar_url") val avatarUrl: String? = null


) : BaseEntity() {
    init {
        this.type = "user"
        this.status = "active" // Set the default value here instead
    }
}
