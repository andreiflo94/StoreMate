package com.example.storemate.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.storemate.presentation.common.LoginIntent
import com.example.storemate.presentation.common.LoginScreenState
import com.example.storemate.presentation.theme.StoreMateTheme
import com.example.storemate.presentation.viewmodels.LoginViewModel

@Composable
fun LoginRoute(viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreen(state = uiState, onIntent = viewModel::onIntent)
}

@Composable
fun LoginScreen(
    state: LoginScreenState,
    onIntent: (LoginIntent) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "StoreMate", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(4.dp))

        Text(
            text = if (state.isRegisterMode) {
                "Create a store on your shop's server"
            } else {
                "Sign in to your shop's server"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.serverUrl,
            onValueChange = { onIntent(LoginIntent.ServerUrlChanged(it)) },
            label = { Text("Server address") },
            placeholder = { Text("192.168.1.10:8080") },
            supportingText = { Text("The computer running StoreMate in your shop") },
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_server_url"),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            )
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.username,
            onValueChange = { onIntent(LoginIntent.UsernameChanged(it)) },
            label = { Text("Username") },
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_username"),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
            label = { Text("Password") },
            singleLine = true,
            enabled = !state.isSubmitting,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password"),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_error")
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                onIntent(LoginIntent.Submit)
            },
            enabled = state.isValid() && !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_submit")
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (state.isRegisterMode) "Create store" else "Sign in")
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { onIntent(LoginIntent.ToggleRegisterMode) },
            enabled = !state.isSubmitting,
            modifier = Modifier.testTag("login_toggle_mode")
        ) {
            Text(
                if (state.isRegisterMode) {
                    "Already have an account? Sign in"
                } else {
                    "First time here? Create a store"
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    StoreMateTheme {
        LoginScreen(
            state = LoginScreenState(serverUrl = "192.168.1.10:8080", username = "demo"),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    StoreMateTheme {
        LoginScreen(
            state = LoginScreenState(
                serverUrl = "192.168.1.10:8080",
                username = "demo",
                password = "wrong",
                errorMessage = "Invalid username or password"
            ),
            onIntent = {}
        )
    }
}
