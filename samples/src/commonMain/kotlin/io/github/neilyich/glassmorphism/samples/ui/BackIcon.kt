package io.github.neilyich.glassmorphism.samples.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController

@Composable
fun BackIcon(navController: NavHostController) {
    IconButton(
        onClick = navController::navigateUp,
        modifier = Modifier.testTag("back"),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
    }
}