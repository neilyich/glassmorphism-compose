package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

/**
 * Modifier used to mark a [Composable] which content must be blurred where it is overlapped.
 *
 * @param blurHolder instance of [BlurHolder] returned from [rememberBlurHolder]
 *
 * @return [Modifier]
 *
 * @see rememberBlurHolder
 * @see blurredBackground
 *
 * @sample BasicDialogSample
 */
@Composable
fun Modifier.blurredContent(blurHolder: BlurHolder): Modifier = if (blurHolder.isBlurEnabled) {
    this then BlurredContentElement(blurHolder)
} else {
    this
}

private class BlurredContentModifierNode(
    var blurHolder: BlurHolder,
): Modifier.Node(), DrawModifierNode, LayoutAwareModifierNode, ObserverModifierNode, CompositionLocalConsumerModifierNode {
    override val shouldAutoInvalidate: Boolean
        get() = false

    private var positionOnScreen = Offset.Zero
    private var size = Size.Zero
    private var displayedBlurs = listOf<DisplayedBlur>()
    private var pathById = mapOf<Int, Path>()
    private val unionPath = Path()
    private var radiusById = mapOf<Int, Dp>()

    // cache graphics layers per id
    private val graphicsLayersById = mutableMapOf<Int, GraphicsLayer>()

    private fun update(
        blurBackgrounds: List<BlurBackground> = blurHolder.blurBackgrounds,
        positionOnScreen: Offset = this.positionOnScreen,
        size: Size = this.size,
    ) {
        val rectOnScreen = Rect(positionOnScreen, size)
        val updatedBlurs = blurBackgrounds.mapNotNull { blurBackground ->
            val intersection = rectOnScreen.intersect(blurBackground.rectOnScreen)
            if (intersection.width < 0f || intersection.height < 0f) {
                null
            } else {
                val dx = rectOnScreen.left - blurBackground.rectOnScreen.left
                val dy = rectOnScreen.top - blurBackground.rectOnScreen.top
                DisplayedBlur(
                    id = blurBackground.id,
                    path = blurBackground.path,
                    offset = Offset(dx, dy),
                    blurRadius = blurBackground.blurRadius,
                )
            }
        }
        if (updatedBlurs != displayedBlurs) {
            displayedBlurs = updatedBlurs
            val newPathById = mutableMapOf<Int, Path>()
            unionPath.rewind()
            val newRadiusById = mutableMapOf<Int, Dp>()
            for (blur in displayedBlurs) {
                val p = newPathById.getOrPut(blur.id) {
                    pathById[blur.id]?.apply { rewind() } ?: Path()
                }
                p.addPath(blur.path, -blur.offset)
                unionPath.addPath(blur.path, -blur.offset)
                newRadiusById[blur.id] = blur.blurRadius
            }
            pathById = newPathById
            radiusById = newRadiusById

            val activeIds = newPathById.keys
            val graphicsContext = currentValueOf(LocalGraphicsContext)
            val iterator = graphicsLayersById.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.key !in activeIds) {
                    graphicsContext.releaseGraphicsLayer(entry.value)
                    iterator.remove()
                }
            }

            invalidateDraw()
        }
    }

    override fun onAttach() {
        update()
        observeReads { blurHolder.blurBackgrounds }
    }

    override fun onDetach() {
        val graphicsContext = currentValueOf(LocalGraphicsContext)
        for (layer in graphicsLayersById.values) {
            graphicsContext.releaseGraphicsLayer(layer)
        }
    }

    override fun onObservedReadsChanged() {
        update()
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        positionOnScreen = coordinates.positionOnScreen()
        update()
    }

    override fun onRemeasured(size: IntSize) {
        this.size = size.toSize()
        update()
    }

    override fun ContentDrawScope.draw() {
        // draw blurred content per id
        val density = currentValueOf(LocalDensity)
        val graphicsContext = currentValueOf(LocalGraphicsContext)
        for ((id, path) in pathById) {
            val radius = radiusById[id] ?: 0.dp
            val blurRadiusPx = with(density) { radius.toPx() }
            val layer = graphicsLayersById.getOrPut(id) {
                graphicsContext.createGraphicsLayer().apply {
                    renderEffect = if (blurRadiusPx > 0f) BlurEffect(blurRadiusPx, blurRadiusPx, TileMode.Decal) else null
                }
            }
            // update effect if radius changed since creation
            val shouldHaveEffect = blurRadiusPx > 0f
            val hasEffect = layer.renderEffect != null
            if (shouldHaveEffect != hasEffect) {
                layer.renderEffect = if (shouldHaveEffect) BlurEffect(blurRadiusPx, blurRadiusPx, TileMode.Decal) else null
            }
            layer.record {
                this@draw.drawContent()
            }
            clipPath(path) {
                drawLayer(layer)
            }
        }
        // draw non-blurred content outside union path
        clipPath(unionPath, ClipOp.Difference) {
            this@draw.drawContent()
        }
    }

    private data class DisplayedBlur(
        val id: Int,
        val path: Path,
        val offset: Offset,
        val blurRadius: Dp,
    )
}

private data class BlurredContentElement(
    val blurHolder: BlurHolder,
) : ModifierNodeElement<BlurredContentModifierNode>() {
    override fun create() = BlurredContentModifierNode(blurHolder)

    override fun update(node: BlurredContentModifierNode) {
        node.blurHolder = blurHolder
    }

    override fun InspectorInfo.inspectableProperties() {}
}