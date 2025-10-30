package io.github.neilyich.glassmorphism

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.neilyich.glassmorphism.samples.BottomSheetSample
import io.github.neilyich.glassmorphism.samples.DialogSample
import io.github.neilyich.glassmorphism.samples.ListItemsSample
import io.github.neilyich.glassmorphism.samples.SamplesList
import io.github.neilyich.glassmorphism.samples.SamplesListDestination
import io.github.neilyich.glassmorphism.samples.TopBarSample
import io.github.neilyich.glassmorphism.samples.ui.SamplesTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(isBlurEnabled: Boolean = true, isSettingsEnabled: Boolean = true) {
    SamplesTheme {
        val navController = rememberNavController()
        val samples = remember {
            listOf(
                DialogSample,
                TopBarSample,
                BottomSheetSample,
                ListItemsSample,
            ).sortedBy { it.name }
        }
        NavHost(
            modifier = Modifier.testTagsAsResourceId(),
            navController = navController,
            startDestination = SamplesListDestination,
        ) {
            composable<SamplesListDestination> {
                SamplesList(navController, samples)
            }
            samples.forEach { sample ->
                composable(route = sample::class) {
                    sample.Content(navController, isBlurEnabled, isSettingsEnabled)
                }
            }
        }
    }
}