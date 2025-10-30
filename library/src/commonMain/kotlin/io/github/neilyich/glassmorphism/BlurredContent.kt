package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.toSize

/**
 * Modifier used to mark a [Composable] which content must be blurred where it is overlapped.
 *
 * @param blurHolder instance of [BlurHolder] returned from [rememberBlurHolder]
 * @param zIndex zIndex (can be used when multiple [blurredContent] overlap each other
 * @param key the key used to identify content of its [Composable]
 *
 * @return [Modifier]
 *
 * @see rememberBlurHolder
 * @see blurredBackground
 *
 * @sample BasicDialogSample
 */
@Composable
fun Modifier.blurredContent(blurHolder: BlurHolder, zIndex: Float? = null, key: Any? = null): Modifier = if (blurHolder.isBlurEnabled) {
    val contentKey = blurHolder.rememberBlurredContent(key, zIndex)
    this then BlurredContentElement(contentKey, blurHolder)
} else {
    this
}

private class BlurredContentModifierNode(
    var key: Any,
    var blurHolder: BlurHolder,
) : Modifier.Node(),
    DrawModifierNode,
    LayoutAwareModifierNode,
    GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {

    private var size: Size = Size.Unspecified
    private var positionOnScreen: Offset = Offset.Unspecified

    override fun onAttach() {}

    override fun onDetach() {
        blurHolder.removeBlurredContent(key)?.contentLayer?.let {
            currentValueOf(LocalGraphicsContext).releaseGraphicsLayer(it)
        }
    }

    override fun ContentDrawScope.draw() {
        blurHolder.withBlurredContentLayer(
            key = key,
            contentLayerFactory = { currentValueOf(LocalGraphicsContext).createGraphicsLayer() },
        ) { layer ->
            layer.record {
                this@draw.drawContent()
            }
            drawLayer(layer)
        }
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        handleUpdatedCoordinates(coordinates)
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        handleUpdatedCoordinates(coordinates)
    }

    private fun handleUpdatedCoordinates(coordinates: LayoutCoordinates) {
        val positionOnScreen = coordinates.positionOnScreen()
        val size = coordinates.size.toSize()
        if (this.positionOnScreen != positionOnScreen || this.size != size) {
            this.positionOnScreen = positionOnScreen
            this.size = size
            blurHolder.withBlurredContent(key) {
                copy(
                    positionOnScreen = Rect(positionOnScreen, size)
                )
            }
        }
    }
}

private data class BlurredContentElement(
    val key: Any,
    val blurHolder: BlurHolder,
) : ModifierNodeElement<BlurredContentModifierNode>() {
    override fun create() = BlurredContentModifierNode(key, blurHolder)

    override fun update(node: BlurredContentModifierNode) {
        node.key = key
        node.blurHolder = blurHolder
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "blurredContent"
        properties["key"] = key
        properties["blurHolder"] = blurHolder
    }
}