package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Stable
expect fun Modifier.testTagsAsResourceId(): Modifier