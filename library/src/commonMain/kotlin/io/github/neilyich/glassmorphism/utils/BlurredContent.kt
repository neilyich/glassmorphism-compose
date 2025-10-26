package io.github.neilyich.glassmorphism.utils

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.layer.GraphicsLayer

@Immutable
internal data class BlurredContent(
    val key: Any,
    val zIndex: Float?,
    val contentLayer: GraphicsLayer? = null,
    val positionOnScreen: Rect = Rect.Zero,
) {
    val isSpecified: Boolean
        get() = positionOnScreen.isEmpty.not() && contentLayer != null
}