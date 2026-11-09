package com.example.carnetsanitario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.carnetsanitario.data.local.AppDatabase
import com.example.carnetsanitario.data.repository.CarnetRepository
import com.example.carnetsanitario.ui.navigation.AppNavigation
import com.example.carnetsanitario.ui.theme.CarnetSanitarioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CarnetRepository(db = database)

        setContent {
            CarnetSanitarioTheme {
                AppNavigation(repository = repository)
            }
        }
    }
}