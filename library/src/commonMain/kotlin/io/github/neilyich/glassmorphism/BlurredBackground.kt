package io.github.neilyich.glassmorphism

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

/**
 * Modifier used to add blurred background to a [Composable].
 *
 * @param blurHolder instance of [BlurHolder] returned from [rememberBlurHolder]
 * @param color additional color to blurred background
 * @param shape shape of blurred background
 *
 * @return [Modifier]
 *
 * @see rememberBlurHolder
 * @see blurredContent
 *
 * @sample BasicDialogSample
 */
@Composable
fun Modifier.blurredBackground(
    blurHolder: BlurHolder,
    blurRadius: Dp,
    color: Color = Color.Transparent,
    shape: Shape = RectangleShape,
): Modifier {
    val blurBackgroundHolder = blurHolder.rememberBlurBackgroundHolder()
    return this.background(color, shape) then BlurredBackgroundElement(blurBackgroundHolder, shape, blurRadius)
}

private class BlurredBackgroundModifierNode(
    var blurBackgroundHolder: BlurBackgroundHolder,
    var shape: Shape,
    var blurRadius: Dp,
) : Modifier.Node(), LayoutModifierNode {
    private val path = Path()

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val p = measurable.measure(constraints)
        return layout(p.width, p.height) {
            coordinates?.let {
                val size = Size(p.width.toFloat(), p.height.toFloat())
                path.rewind()
                path.addOutline(shape.createOutline(size, layoutDirection, this@measure))
                blurBackgroundHolder.blurBackground = BlurBackground(
                    id = blurBackgroundHolder.id,
                    rectOnScreen = Rect(it.positionOnScreen(), size),
                    path = path,
                    blurRadius = blurRadius,
                )
            }
            p.place(0, 0)
        }
    }
}

private data class BlurredBackgroundElement(
    val blurBackgroundHolder: BlurBackgroundHolder,
    val shape: Shape,
    val blurRadius: Dp,
) : ModifierNodeElement<BlurredBackgroundModifierNode>() {
    override fun create() = BlurredBackgroundModifierNode(blurBackgroundHolder, shape, blurRadius)

    override fun update(node: BlurredBackgroundModifierNode) {
        node.blurBackgroundHolder = blurBackgroundHolder
        node.shape = shape
        node.blurRadius = blurRadius
    }

    override fun InspectorInfo.inspectableProperties() {}
}