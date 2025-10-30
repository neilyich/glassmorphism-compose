package io.github.neilyich.glassmorphism.samples

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class BlurSettings(
    val isBlurEnabled: Boolean,
    blurRadius: Dp = 0.dp,
    tintColor: Color = Color.Unspecified,
    backgroundColor: Color = Color.Unspecified,
    shape: Shape = RectangleShape,
    tileMode: TileMode = TileMode.Clamp,
) {
    var blurRadius by mutableStateOf(blurRadius)
    var tintColor by mutableStateOf(tintColor)
    var backgroundColor by mutableStateOf(backgroundColor)
    var shape by mutableStateOf(shape)
    var tileMode by mutableStateOf(tileMode)
}