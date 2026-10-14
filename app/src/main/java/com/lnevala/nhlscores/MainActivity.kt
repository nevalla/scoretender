package com.lnevala.nhlscores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lnevala.nhlscores.ui.AppNavigation
import com.lnevala.nhlscores.ui.theme.NhlScoresTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            NhlScoresTheme {
                AppNavigation()
            }
        }
    }
}
