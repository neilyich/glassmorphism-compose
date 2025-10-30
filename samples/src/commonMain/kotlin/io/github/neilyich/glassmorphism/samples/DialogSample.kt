package io.github.neilyich.glassmorphism.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.neilyich.glassmorphism.blurredBackground
import io.github.neilyich.glassmorphism.blurredContent
import io.github.neilyich.glassmorphism.rememberBlurHolder
import io.github.neilyich.glassmorphism.samples.ui.BackIcon
import kotlinx.serialization.Serializable

@Serializable
object DialogSample : Sample() {
    override val name: String = "Dialog"

    @Composable
    override fun rememberDefaultBlurSettings(isBlurEnabled: Boolean): BlurSettings {
        val colorScheme = MaterialTheme.colorScheme
        return remember(isBlurEnabled) {
            BlurSettings(
                isBlurEnabled = isBlurEnabled,
                blurRadius = 36.dp,
                backgroundColor = colorScheme.background,
                shape = RoundedCornerShape(16.dp),
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        navController: NavHostController,
        blurSettings: BlurSettings,
        isSettingsIconVisible: Boolean
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("$name Sample") },
                    navigationIcon = { BackIcon(navController) },
                    actions = { BlurSettingsIcon(isSettingsIconVisible) },
                )
            },
        ) {
            val colors = remember {
                listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Green,
                    Color.Cyan,
                    Color.Blue,
                    Color.Magenta,
                )
            }
            Box(Modifier.fillMaxSize()) {
                val blurHolder = rememberBlurHolder(blurSettings.isBlurEnabled)
                LazyColumn(
                    modifier = Modifier
                        .testTag("lazy_column")
                        .fillMaxSize()
                        .blurredContent(blurHolder),
                    verticalArrangement = Arrangement.spacedBy(36.dp),
                ) {
                    items(50) { index ->
                        Box(
                            modifier = Modifier
                                .background(colors[index % colors.size].copy(alpha = 0.5f))
                                .fillMaxWidth()
                                .height(48.dp),
                        ) {
                            Text("item $index", Modifier.align(Alignment.Center))
                        }
                    }
                }
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(200.dp)
                        .blurredBackground(
                            blurHolder = blurHolder,
                            blurRadius = blurSettings.blurRadius,
                            tintColor = blurSettings.tintColor,
                            backgroundColor = blurSettings.backgroundColor,
                            shape = blurSettings.shape,
                        )
                        .border(
                            width = Dp.Hairline,
                            color = Color.Gray,
                            shape = blurSettings.shape,
                        )
                        .padding(12.dp),
                    text = name,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }
    }
}