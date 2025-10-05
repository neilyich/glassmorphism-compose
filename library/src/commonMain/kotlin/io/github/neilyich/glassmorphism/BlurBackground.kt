package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path

@Immutable
internal data class BlurBackground(
    val rectOnScreen: Rect,
    val path: Path,
)