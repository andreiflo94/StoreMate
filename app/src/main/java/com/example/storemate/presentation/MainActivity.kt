package com.example.storemate.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.storemate.presentation.screens.AppNavGraph
import com.example.storemate.presentation.theme.StoreMateTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoreMateTheme {
                AppNavGraph()
            }
        }
    }
}