package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.node.requireLayoutDirection
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.roundToIntSize
import androidx.compose.ui.unit.toSize
import io.github.neilyich.glassmorphism.utils.Fields
import io.github.neilyich.glassmorphism.utils.UpdatedFieldsHolder

/**
 * Modifier used to add blurred background to a [Composable].
 *
 * @param blurHolder instance of [BlurHolder] returned from [rememberBlurHolder]
 * @param blurRadius radius of blur
 * @param shape shape of blurred background
 * @param tintColor additional color to blurred background
 * @param backgroundColor color drawn behind blurred background
 * @param tileMode tileMode used in BlurEffect
 *
 * @return [Modifier]
 *
 * @see rememberBlurHolder
 * @see blurredContent
 *
 * @sample BasicDialogSample
 */
fun Modifier.blurredBackground(
    blurHolder: BlurHolder,
    blurRadius: Dp,
    shape: Shape = RectangleShape,
    tintColor: Color = Color.Unspecified,
    backgroundColor: Color = Color.Unspecified,
    tileMode: TileMode = TileMode.Clamp,
) = if (blurHolder.isBlurEnabled) {
    this then BlurredBackgroundElement(blurHolder, blurRadius, shape, tintColor, backgroundColor, tileMode)
} else {
    this
}

private class BlurredBackgroundModifierNode(
    var blurHolder: BlurHolder,
    var blurRadius: Dp,
    var shape: Shape,
    var tintColor: Color,
    var backgroundColor: Color,
    var tileMode: TileMode,
) : Modifier.Node(),
    DrawModifierNode,
    ObserverModifierNode,
    LayoutAwareModifierNode,
    CompositionLocalConsumerModifierNode,
    GlobalPositionAwareModifierNode {

    override val shouldAutoInvalidate = false

    private var graphicsLayer: GraphicsLayer? = null

    private var updatedFields = UpdatedFieldsHolder()

    private var positionOnScreen = Offset.Unspecified
    set(value) {
        if (field != value) {
            updatedFields += Fields.PositionOnScreen
            field = value
        }
    }

    private var size = IntSize.Zero
    set(value) {
        if (field != value) {
            updatedFields += Fields.Size
            field = value
        }
    }

    private var outline: Outline? = null

    private var boundsOutline = Path()

    private var displayedContent = DisplayedContent(emptyList(), Rect.Zero)
    private var isDrawn = false

    override fun onAttach() {
        observeReads { blurHolder.specifiedBlurredContents }
        graphicsLayer = currentValueOf(LocalGraphicsContext).createGraphicsLayer().apply {
            val blurRadiusPx = with(currentValueOf(LocalDensity)) { blurRadius.toPx() }
            if (blurRadiusPx > 0f) {
                renderEffect = BlurEffect(blurRadiusPx, blurRadiusPx, tileMode)
            }
            clip = true
        }
    }

    override fun onObservedReadsChanged() {
        updatedFields += Fields.BlurredContents
        handleUpdatedFields()
    }

    override fun onDetach() {
        graphicsLayer?.let {
            currentValueOf(LocalGraphicsContext).releaseGraphicsLayer(it)
        }
        displayedContent = DisplayedContent(emptyList(), Rect.Zero)
        isDrawn = false
    }

    override fun ContentDrawScope.draw() {
        drawBackground()
        graphicsLayer?.let { layer ->
            if (recordDisplayedContent(layer)) {
                drawLayer(layer)
            }
        }
        drawTint()
        drawContent()
        isDrawn = true
    }


    private fun ContentDrawScope.recordDisplayedContent(layer: GraphicsLayer): Boolean {
        updatedFields -= Fields.BlurredContentAffectingFields
        if (displayedContent.isEmpty) {
            if (isDrawn) return false
            updatedFields = UpdatedFieldsHolder(Fields.BlurredContents)
            handleUpdatedFields()
            if (displayedContent.isEmpty) return false
        }
        layer.topLeft = displayedContent.bounds.topLeft.round()
        layer.setPathOutline(boundsOutline)
        layer.record(
            size = displayedContent.bounds.size.roundToIntSize(),
        ) {
            for (content in displayedContent.blurredContents) {
                val offset = -displayedContent.bounds.topLeft + content.relativeRect.topLeft
                inset(
                    left = offset.x,
                    top = offset.y,
                    right = 0f,
                    bottom = 0f,
                ) {
                    translate(
                        left = -content.contentOffset.x,
                        top = -content.contentOffset.y,
                    ) {
                        drawLayer(content.contentLayer)
                    }
                }
            }
        }
        return true
    }

    private fun ContentDrawScope.drawBackground() = outline?.let {
        if (backgroundColor.isSpecified) {
            drawOutline(it, backgroundColor)
        }
        updatedFields -= Fields.BackgroundColor
    }

    private fun ContentDrawScope.drawTint() = outline?.let {
        if (tintColor.isSpecified) {
            drawOutline(it, tintColor)
        }
        updatedFields -= Fields.TintColor
    }

    override fun onDensityChange() {
        updatedFields += Fields.Density
        handleUpdatedFields()
    }

    override fun onLayoutDirectionChange() {
        updatedFields += Fields.LayoutDirection
        handleUpdatedFields()
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        positionOnScreen = coordinates.positionOnScreen()
        size = coordinates.size
        handleUpdatedFields()
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        positionOnScreen = coordinates.positionOnScreen()
        size = coordinates.size
        handleUpdatedFields()
    }

    private fun handleUpdatedFields() {
        if (updatedFields.any(Fields.OutlineAffectingFields)) {
            updateOutline()
        }
        if (updatedFields.any(Fields.BlurredContentAffectingFields)) {
            if (recalculateDisplayedContent(updatedFields)) return
        }
        if (updatedFields.any(Fields.OnlyDrawAffectingFields)) {
            if (isDrawn) invalidateDraw()
        }
    }

    private fun updateOutline(
        size: Size = this.size.toSize(),
        layoutDirection: LayoutDirection = requireLayoutDirection(),
        density: Density = requireDensity(),
    ) {
        outline = shape.createOutline(
            size = size,
            layoutDirection = layoutDirection,
            density = density,
        )
    }

    private fun recalculateDisplayedContent(updatedFields: UpdatedFieldsHolder): Boolean {
        if (positionOnScreen.isUnspecified || size == IntSize.Zero) return false
        val blurRadiusPx = with(currentValueOf(LocalDensity)) { blurRadius.toPx() }
        val rectOnScreen = Rect(positionOnScreen, size.toSize())
        val inflatedRectOnScreen = rectOnScreen.inflate(blurRadiusPx)
        var boundsLeft = Float.MAX_VALUE
        var boundsTop = Float.MAX_VALUE
        var boundsRight = Float.MIN_VALUE
        var boundsBottom = Float.MIN_VALUE
        val blurredContents = blurHolder.specifiedBlurredContents.mapNotNull { blurredContent ->
            val intersection = blurredContent.positionOnScreen.intersect(inflatedRectOnScreen)
            if (intersection.width < 0f || intersection.height < 0f) {
                return@mapNotNull null
            }
            if (boundsLeft > intersection.left) {
                boundsLeft = intersection.left
            }
            if (boundsTop > intersection.top) {
                boundsTop = intersection.top
            }
            if (boundsRight < intersection.right) {
                boundsRight = intersection.right
            }
            if (boundsBottom < intersection.bottom) {
                boundsBottom = intersection.bottom
            }
            blurredContent.contentLayer?.let { contentLayer ->
                BlurredBackgroundContent(
                    relativeRect = Rect(
                        offset = intersection.topLeft - rectOnScreen.topLeft,
                        size = intersection.size,
                    ),
                    contentLayer = contentLayer,
                    contentOffset = intersection.topLeft - blurredContent.positionOnScreen.topLeft,
                )
            }
        }
        val bounds = Rect(
            topLeft = Offset(boundsLeft, boundsTop),
            bottomRight = Offset(boundsRight, boundsBottom),
        ).translate(-rectOnScreen.topLeft)
        if (updatedFields.any(Fields.OutlineAffectingFields) || bounds != displayedContent.bounds) {
            outline?.let {
                boundsOutline.rewind()
                boundsOutline.addOutline(it)
                boundsOutline.translate(-bounds.topLeft)
            }
        }
        return invalidateDrawIfNeeded(DisplayedContent(blurredContents, bounds))
    }

    private fun invalidateDrawIfNeeded(newDisplayedContent: DisplayedContent): Boolean = if (displayedContent != newDisplayedContent) {
        displayedContent = newDisplayedContent
        if (isDrawn) invalidateDraw()
        true
    } else {
        false
    }

    @Immutable
    private data class BlurredBackgroundContent(
        val relativeRect: Rect,
        val contentLayer: GraphicsLayer,
        val contentOffset: Offset,
    )

    @Immutable
    private data class DisplayedContent(
        val blurredContents: List<BlurredBackgroundContent>,
        val bounds: Rect,
    ) {
        val isEmpty get() = blurredContents.isEmpty() || bounds.isEmpty
    }
}

private data class BlurredBackgroundElement(
    val blurHolder: BlurHolder,
    val blurRadius: Dp,
    val shape: Shape,
    val tintColor: Color,
    val backgroundColor: Color,
    val tileMode: TileMode,
) : ModifierNodeElement<BlurredBackgroundModifierNode>() {
    override fun create() = BlurredBackgroundModifierNode(blurHolder, blurRadius, shape, tintColor, backgroundColor, tileMode)

    override fun update(node: BlurredBackgroundModifierNode) {
        node.blurHolder = blurHolder
        node.shape = shape
        node.blurRadius = blurRadius
        node.tintColor = tintColor
        node.backgroundColor = backgroundColor
        node.tileMode = tileMode
    }

    override fun InspectorInfo.inspectableProperties() {}
}