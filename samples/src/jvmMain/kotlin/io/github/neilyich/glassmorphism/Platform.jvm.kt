package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

@Stable
actual fun Modifier.testTagsAsResourceId(): Modifier = this