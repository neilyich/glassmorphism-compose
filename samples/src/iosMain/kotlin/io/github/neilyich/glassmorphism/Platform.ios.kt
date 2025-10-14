package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@Stable
actual fun Modifier.testTagsAsResourceId(): Modifier = this