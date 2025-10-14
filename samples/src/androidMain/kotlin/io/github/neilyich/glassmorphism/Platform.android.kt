package io.github.neilyich.glassmorphism

import android.os.Build
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Stable
actual fun Modifier.testTagsAsResourceId(): Modifier = semantics {
    testTagsAsResourceId = true
}