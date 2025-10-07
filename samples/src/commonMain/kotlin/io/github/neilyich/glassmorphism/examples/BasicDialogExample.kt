package io.github.neilyich.glassmorphism.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.neilyich.glassmorphism.blurredBackground
import io.github.neilyich.glassmorphism.blurredContent
import io.github.neilyich.glassmorphism.rememberBlurHolder

@Composable
fun BasicDialogExample(greeting: String) {
    Box(Modifier.fillMaxSize()) {

        val blurHolder = rememberBlurHolder()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .blurredContent(blurHolder)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(36.dp),
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
                        .background(colors[i % colors.size].copy(alpha = 0.5f))
                        .fillMaxWidth()
                        .height(48.dp),
                )
            }
        }

        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .size(200.dp)
                .blurredBackground(
                    blurHolder = blurHolder,
                    blurRadius = 36.dp,
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp),
                )
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(12.dp),
            text = greeting,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = Color.White,
        )
    }
}