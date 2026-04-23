package com.simplifybiz.mobile.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifybiz.mobile.data.auth.AuthRepository
import com.simplifybiz.mobile.data.auth.GoogleAuthManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class LoginEvent {
    data object Success : LoginEvent()
    data class Error(val message: String) : LoginEvent()
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loginEvent = Channel<LoginEvent>()
    val loginEvent = _loginEvent.receiveAsFlow()

    fun onUsernameChange(value: String) { _username.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun onGoogleLoginClick(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val idToken = googleAuthManager.getGoogleIdToken(context)

            if (idToken != null) {
                val result = authRepository.loginWithGoogle(idToken)

                result.onSuccess {
                    _loginEvent.send(LoginEvent.Success)
                }.onFailure { e ->
                    if (e.message == "no_subscription") {
                        sendError("No active subscription found")
                    } else {
                        sendError("Google login failed")
                    }
                }
            } else {
                sendError("Google sign-in cancelled")
            }
            _isLoading.value = false
        }
    }

    fun onLoginClick() {
        val user = _username.value
        val pass = _password.value

        if (user.isBlank() || pass.isBlank()) {
            sendError("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.login(user, pass)

            result.onSuccess {
                _loginEvent.send(LoginEvent.Success)
            }.onFailure { e ->
                if (e.message == "no_subscription") {
                    sendError("No active subscription found")
                } else {
                    sendError("Login failed. Check credentials")
                }
            }
            _isLoading.value = false
        }
    }

    private fun sendError(msg: String) {
        viewModelScope.launch { _loginEvent.send(LoginEvent.Error(msg)) }
    }
}