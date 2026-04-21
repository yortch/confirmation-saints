package com.yortch.confirmationsaints

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yortch.confirmationsaints.ui.navigation.AppRoot
import com.yortch.confirmationsaints.ui.theme.ConfirmationSaintsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. Installs the AndroidX SplashScreen then renders the
 * Compose tree. Hilt injects view models into Compose via `hiltViewModel()`.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConfirmationSaintsTheme {
                AppRoot()
            }
        }
    }
}

