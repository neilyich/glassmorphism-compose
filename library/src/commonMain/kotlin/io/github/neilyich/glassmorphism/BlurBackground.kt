package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp

@Immutable
internal data class BlurBackground(
    val id: Int,
    val rectOnScreen: Rect,
    val path: Path,
    val blurRadius: Dp,
)