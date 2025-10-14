package io.github.neilyich.glassmorphism.samples

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

interface Sample {
    val name: String

    @Composable
    fun Content(navController: NavHostController, isBlurEnabled: Boolean)
}