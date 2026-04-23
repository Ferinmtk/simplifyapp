package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import simplifybiz.composeapp.generated.resources.Res
import simplifybiz.composeapp.generated.resources.logo_zen_stones
import simplifybiz.composeapp.generated.resources.logo_simplify_text
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<LoginViewModel>()
        val snackbarHostState = remember { SnackbarHostState() }

        var showEmailForm by remember { mutableStateOf(false) }
        val loginEvent by viewModel.loginEvent.collectAsState(initial = null)

        LaunchedEffect(loginEvent) {
            when (loginEvent) {
                is LoginEvent.Success -> {
                    navigator.replaceAll(DashboardScreen())
                }
                is LoginEvent.Error -> {
                    snackbarHostState.showSnackbar((loginEvent as LoginEvent.Error).message)
                }
                else -> {}
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFFF9F9F9)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(Res.drawable.logo_zen_stones),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(Res.drawable.logo_simplify_text),
                    contentDescription = null,
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Welcome",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                )
                Text(
                    text = "Simplify Your Business",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                if (!showEmailForm) {
                    OutlinedButton(
                        onClick = { viewModel.onGoogleLoginClick() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.LightGray),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "Continue with Google",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
                        Text(text = "or", modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { showEmailForm = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097A7)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                } else {
                    EmailLoginForm(viewModel, onBack = { showEmailForm = false })
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Don't have an Account? Sign Up",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF0097A7),
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
    }

    @Composable
    fun EmailLoginForm(viewModel: LoginViewModel, onBack: () -> Unit) {
        val username by viewModel.username.collectAsState()
        val password by viewModel.password.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.onUsernameChange(it) },
                label = { Text("Username or Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onLoginClick() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097A7))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login")
                }
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Back to options", color = Color.Gray)
            }
        }
    }
}