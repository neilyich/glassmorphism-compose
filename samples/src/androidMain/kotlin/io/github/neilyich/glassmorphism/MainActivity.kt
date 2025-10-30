package io.github.neilyich.glassmorphism

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val isBlurEnabled = intent.getBooleanExtra("isBlurEnabled", true)
        val isSettingsEnabled = intent.getBooleanExtra("isSettingsEnabled", true)
        setContent {
            App(isBlurEnabled, isSettingsEnabled)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}