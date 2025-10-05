package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path

@Composable
fun rememberBlurHolder1() = remember { BlurHolder() }

@Stable
class BlurHolder {
    private var currentId = 0
    private val backgroundHolders = mutableStateMapOf<Int, BlurBackgroundHolder>()

    internal val blurBackgrounds by derivedStateOf { backgroundHolders.values.mapNotNull { it.blurBackground } }

    @Composable
    internal fun rememberBlurBackgroundHolder(): BlurBackgroundHolder {
        val blurBackgroundHolder = remember {
            val id = currentId++
            BlurBackgroundHolder(id).also {
                backgroundHolders[id] = it
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                backgroundHolders.remove(blurBackgroundHolder.id)
            }
        }
        return blurBackgroundHolder
    }
}
