package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.local.ResumeDatabase
import com.example.data.repository.ResumeRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ResumeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create Room database instance
        val database = ResumeDatabase.getDatabase(applicationContext)
        val repository = ResumeRepository(database.resumeDao)
        val resumeViewModel = ResumeViewModel(repository)

        setContent {
            val isDarkTheme by resumeViewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainScreen(viewModel = resumeViewModel)
            }
        }
    }
}
