package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
internal class BlurBackgroundHolder(val id: Int) {
    internal var blurBackground by mutableStateOf<BlurBackground?>(null)
}