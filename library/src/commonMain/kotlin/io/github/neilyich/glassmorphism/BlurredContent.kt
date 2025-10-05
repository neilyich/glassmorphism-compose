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
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize

@Composable
fun Modifier.blurredContent(blurHolder: BlurHolder, blurRadius: Dp): Modifier {
    val graphicsLayer = rememberBlurGraphicsLayer(blurRadius)
    return this then BlurredContentElement(blurHolder, graphicsLayer)
}

@Composable
private fun rememberBlurGraphicsLayer(blurRadius: Dp): GraphicsLayer {
    val blurRadiusPx = with(LocalDensity.current) { blurRadius.toPx() }
    return rememberGraphicsLayer().apply {
        if (blurRadiusPx > 0f) {
            renderEffect = BlurEffect(blurRadiusPx, blurRadiusPx, TileMode.Decal)
        }
    }
}

private class BlurredContentModifierNode(
    var blurHolder: BlurHolder,
    var graphicsLayer: GraphicsLayer,
): Modifier.Node(), DrawModifierNode, LayoutAwareModifierNode, ObserverModifierNode {
    override val shouldAutoInvalidate: Boolean
        get() = false

    private var positionOnScreen = Offset.Zero
    private var size = Size.Zero
    private var displayedBlurs = listOf<DisplayedBlur>()
    private var blurredPath = Path()

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
                DisplayedBlur(blurBackground.path, Offset(dx, dy))
            }
        }
        if (updatedBlurs != displayedBlurs) {
            displayedBlurs = updatedBlurs
            blurredPath.rewind()
            for (blur in displayedBlurs) {
                blurredPath.addPath(blur.path, -blur.offset)
            }
            invalidateDraw()
        }
    }

    override fun onAttach() {
        update()
        observeReads { blurHolder.blurBackgrounds }
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
        graphicsLayer.record {
            this@draw.drawContent()
        }
        clipPath(blurredPath) {
            drawLayer(graphicsLayer)
        }
        clipPath(blurredPath, ClipOp.Difference) {
            this@draw.drawContent()
        }
    }

    private data class DisplayedBlur(
        val path: Path,
        val offset: Offset,
    )
}

private data class BlurredContentElement(
    val blurHolder: BlurHolder,
    val graphicsLayer: GraphicsLayer,
) : ModifierNodeElement<BlurredContentModifierNode>() {
    override fun create() = BlurredContentModifierNode(blurHolder, graphicsLayer)

    override fun update(node: BlurredContentModifierNode) {
        node.blurHolder = blurHolder
        node.graphicsLayer = graphicsLayer
    }

    override fun InspectorInfo.inspectableProperties() {}
}