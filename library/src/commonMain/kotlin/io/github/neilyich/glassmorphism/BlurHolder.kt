package io.github.neilyich.glassmorphism

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Creates instance of BlurHolder.
 * This instance must be passed to both [blurredContent] and [blurredBackground].
 *
 * @param isBlurEnabled if false - [blurredContent] will have no effect and [blurredBackground] will have the same effect as [Modifier.background]
 *
 * @return [BlurHolder]
 *
 * @see blurredContent
 * @see blurredBackground
 *
 * @sample BasicDialogSample
 */
@Composable
fun rememberBlurHolder(isBlurEnabled: Boolean = true) = remember { BlurHolder(isBlurEnabled) }

/**
 * Instance of this class is used to connect [blurredContent] and [blurredBackground] with each other.
 *
 * @see rememberBlurHolder
 * @see blurredContent
 * @see blurredBackground
 *
 * @sample BasicDialogSample
 */
@Stable
class BlurHolder(initialBlurEnabled: Boolean) {

    /**
     * If false - [blurredContent] will have no effect and [blurredBackground] will have the same effect as [Modifier.background]
     */
    val isBlurEnabled by mutableStateOf(initialBlurEnabled)

    private var nextBackgroundHolderId = 0
    private val backgroundHolders = mutableStateMapOf<Int, BlurBackgroundHolder>()

    internal val blurBackgrounds by derivedStateOf { backgroundHolders.values.mapNotNull { it.blurBackground } }

    @Composable
    internal fun rememberBlurBackgroundHolder(): BlurBackgroundHolder {
        val blurBackgroundHolder = remember {
            val id = nextBackgroundHolderId++
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
