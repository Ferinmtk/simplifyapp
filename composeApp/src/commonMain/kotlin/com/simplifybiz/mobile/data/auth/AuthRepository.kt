package com.simplifybiz.mobile.data.auth

import com.simplifybiz.mobile.data.SessionManager
import com.simplifybiz.mobile.data.UserDao
import com.simplifybiz.mobile.data.UserEntity
import com.simplifybiz.mobile.data.WPUser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.util.date.getTimeMillis
import kotlinx.serialization.Serializable

class AuthRepository(
    private val client: HttpClient,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    @Serializable
    data class JwtResponse(val token: String)

    suspend fun login(user: String, pass: String): Result<Boolean> {
        return try {
            val timestamp = getTimeMillis()
            val response: JwtResponse = client.post("https://app.simplifybiz.com/wp-json/jwt-auth/v1/token?cb=$timestamp") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("username" to user, "password" to pass))
            }.body()

            handleUserSession(response.token, timestamp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<Boolean> {
        return try {
            val timestamp = getTimeMillis()
            val response: JwtResponse = client.post("https://app.simplifybiz.com/wp-json/simplify-auth/v1/google?cb=$timestamp") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("id_token" to idToken))
            }.body()

            handleUserSession(response.token, timestamp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleUserSession(token: String, timestamp: Long): Result<Boolean> {
        val bearerToken = "Bearer $token"

        return try {
            val wpUser: WPUser = client.get("https://app.simplifybiz.com/wp-json/wp/v2/users/me?cb=$timestamp") {
                header(HttpHeaders.Authorization, bearerToken)
            }.body()

            // Safe role extraction: defaults to empty list if field is missing in JSON
            val userRoles = wpUser.roles ?: emptyList()
            val allowedRoles = listOf("coach", "business_owner", "administrator", "subscriber")

            if (userRoles.none { it in allowedRoles }) {
                return Result.failure(Exception("no_subscription"))
            }

            sessionManager.saveAuthToken(bearerToken)

            val newUser = UserEntity(
                username = wpUser.slug,
                email = wpUser.email ?: "",
                displayName = wpUser.name,
                roles = userRoles.joinToString(","),
                avatarUrl = wpUser.avatar_urls?.get("96")
            ).apply {
                remoteId = wpUser.id
                uuid = "$timestamp-${(0..9999).random()}"
                status = "active"
            }

            userDao.insertUser(newUser)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
        userDao.clearUser()
    }

    suspend fun getCurrentUserSync(): UserEntity? {
        return userDao.getCurrentUserSync()
    }
}