package com.duetduetku.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.duetduetku.app.navigation.AppNavigation
import com.duetduetku.app.ui.theme.DuetDuetkuTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import javax.inject.Inject
import com.duetduetku.app.data.datastore.UserPreferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appTheme by userPreferences.appTheme.collectAsState(initial = 0)
            
            val isDarkTheme = when (appTheme) {
                1 -> false // Light
                2 -> true  // Dark
                else -> isSystemInDarkTheme() // System (0)
            }

            DuetDuetkuTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}