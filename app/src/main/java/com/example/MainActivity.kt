package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.ui.GameViewModel
import com.example.ui.GameViewModelFactory
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room local storage instance
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GameRepository(database.walletDao())

        // Load game view-model utilizing provider factory
        val factory = GameViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[GameViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}
