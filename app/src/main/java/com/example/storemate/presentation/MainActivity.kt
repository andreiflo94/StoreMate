package com.example.storemate.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.storemate.domain.repositories.AuthRepository
import com.example.storemate.presentation.screens.AppNavGraph
import com.example.storemate.presentation.theme.StoreMateTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val authRepository: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Reading the stored session touches disk, so the nav graph is held back
        // until we know whether to open on the dashboard or the login screen.
        // Rendering first would flash the login screen on every cold start.
        var startState by mutableStateOf<StartState>(StartState.Resolving)

        lifecycleScope.launch {
            val session = authRepository.restoreSession()
            startState = StartState.Ready(loggedIn = session != null)
        }

        setContent {
            StoreMateTheme {
                when (val state = startState) {
                    StartState.Resolving -> Unit
                    is StartState.Ready -> AppNavGraph(startLoggedIn = state.loggedIn)
                }
            }
        }
    }

    private sealed interface StartState {
        data object Resolving : StartState
        data class Ready(val loggedIn: Boolean) : StartState
    }
}
