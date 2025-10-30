package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.layer.GraphicsLayer
import io.github.neilyich.glassmorphism.utils.BlurredContent
import io.github.neilyich.glassmorphism.utils.BlurredContentKey

/**
 * Creates instance of BlurHolder.
 * This instance must be passed to both [blurredContent] and [blurredBackground].
 *
 * @param isBlurEnabled if false - [blurredContent] will have no effect and [blurredBackground] will give no blur ignoring value of `blurRadius`.
 * By default it is `true` if blur is available for the device
 *
 * @return [BlurHolder]
 *
 * @see blurredContent
 * @see blurredBackground
 *
 * @sample BasicDialogSample
 */
@Composable
fun rememberBlurHolder(isBlurEnabled: Boolean = checkIfBlurAvailable()) = remember(isBlurEnabled) { BlurHolder(isBlurEnabled) }

/**
 * Checks if blur is available for the device
 * (returns `false` only for Android 11 and lower because blur is not supported there)
 */
expect fun checkIfBlurAvailable(): Boolean

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
     * If false - [blurredContent] will have no effect and [blurredBackground] will give no blur ignoring value of `blurRadius`
     */
    val isBlurEnabled by mutableStateOf(initialBlurEnabled)

    private var blurredContentId = 0

    private val blurredContents = mutableStateMapOf<Any, BlurredContent>()

    internal val specifiedBlurredContents by derivedStateOf {
        blurredContents.values.filter { it.isSpecified }.sortedBy { it.zIndex }
    }

    @Composable
    internal fun rememberBlurredContent(key: Any?, zIndex: Float?): Any {
        val blurredContent = remember(key) {
            val contentKey = key ?: BlurredContentKey(blurredContentId++)
            val blurredContent = BlurredContent(contentKey, zIndex)
            blurredContents[contentKey] = blurredContent
            blurredContent
        }
        if (zIndex != blurredContent.zIndex) {
            withBlurredContent(blurredContent.key) {
                copy(zIndex = zIndex)
            }
        }
        return blurredContent.key
    }

    internal fun removeBlurredContent(key: Any) = blurredContents.remove(key)

    internal inline fun withBlurredContentLayer(
        key: Any,
        contentLayerFactory: () -> GraphicsLayer,
        block: (GraphicsLayer) -> Unit,
    ) {
        val blurredContent = blurredContents[key] ?: return
        val layer = blurredContent.contentLayer ?: contentLayerFactory().also {
            blurredContents[key] = blurredContent.copy(contentLayer = it)
        }
        block(layer)
    }

    internal inline fun withBlurredContent(key: Any, block: BlurredContent.() -> BlurredContent) {
        val blurredContent = blurredContents[key] ?: return
        blurredContents[key] = blurredContent.block()
    }
}