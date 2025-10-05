package io.github.neilyich.glassmorphism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun BasicDialogSample() {
    Box(Modifier.fillMaxSize()) {

        // create instance of BlurHolder
        val blurHolder = rememberBlurHolder()

        ExampleBackgroundContent(
            modifier = Modifier
                .fillMaxSize()
                // Use blurredContent to indicate that content of this Composable must be blurred where it is overlapped
                .blurredContent(
                    blurHolder = blurHolder,
                    blurRadius = 24.dp,
                ),
        )

        ExampleDialog(
            modifier = Modifier
                .align(Alignment.Center)
                .size(200.dp)
                // Use blurredBackground for an overlapping Composable to make its background blurred
                .blurredBackground(
                    blurHolder = blurHolder,
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp),
                ),
        )
    }
}

@Composable
private fun ExampleBackgroundContent(modifier: Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
        repeat(50) { i ->
            Box(
                modifier = Modifier
                    .background(colors[i % colors.size])
                    .fillMaxWidth()
                    .height(48.dp),
            )
        }
    }
}

@Composable
private fun ExampleDialog(modifier: Modifier) {
    Box(modifier)
}