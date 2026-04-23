package com.simplifybiz.mobile.di

import kotlinx.serialization.ExperimentalSerializationApi
import com.simplifybiz.mobile.data.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.date.getTimeMillis
import kotlinx.serialization.json.Json
import org.koin.dsl.module

@OptIn(ExperimentalSerializationApi::class)
val networkModule = module {
    single {
        val sessionManager: SessionManager = get()

        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                    namingStrategy = kotlinx.serialization.json.JsonNamingStrategy.SnakeCase
                })
            }

            install(Logging) {
                level = LogLevel.ALL
                logger = Logger.SIMPLE
            }

            defaultRequest {
                url("https://app.simplifybiz.com/wp-json/simplify/v1/")
                contentType(ContentType.Application.Json)

                // Dynamically fetch token for every request
                sessionManager.getAuthToken()?.let { token ->
                    val cleanToken = token.removePrefix("Bearer ").trim()
                    if (cleanToken.isNotEmpty()) {
                        header("Authorization", "Bearer $cleanToken")
                    }
                }
            }
        }.apply {
            sendPipeline.intercept(HttpSendPipeline.State) {
                context.url.parameters.append("cb", getTimeMillis().toString())
            }
        }
    }
}
