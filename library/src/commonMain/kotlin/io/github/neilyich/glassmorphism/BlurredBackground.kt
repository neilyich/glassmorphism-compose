package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.roundToIntSize
import androidx.compose.ui.unit.toSize
import io.github.neilyich.glassmorphism.utils.BlurredContent
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
 * @param block block to update parameters if they are updated frequently (e.g. in animations) for better performance.
 * Also it is valid to pass all parameters in this block not specifying them in braces
 *
 * @return [Modifier]
 *
 * @see rememberBlurHolder
 * @see blurredContent
 *
 * @sample BasicDialogSample
 */
@Stable
fun Modifier.blurredBackground(
    blurHolder: BlurHolder,
    blurRadius: Dp = 0.dp,
    shape: Shape = RectangleShape,
    tintColor: Color = Color.Unspecified,
    backgroundColor: Color = Color.Unspecified,
    tileMode: TileMode = TileMode.Clamp,
    block: (BlurredBackgroundScope.() -> Unit)? = null,
): Modifier {
    val scope = BlurredBackgroundScopeImpl(
        initialBlurRadius = if (blurHolder.isBlurEnabled) blurRadius else Dp.Unspecified,
        initialShape = shape,
        initialTintColor = tintColor,
        initialBackgroundColor = backgroundColor,
        initialTileMode = tileMode,
    )
    return this then BlurredBackgroundElement(blurHolder, scope, block)
}

private class BlurredBackgroundModifierNode(
    var blurHolder: BlurHolder,
    var scope: BlurredBackgroundScopeImpl,
    var block: (BlurredBackgroundScope.() -> Unit)?,
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
    private var blurredContents = listOf<BlurredContent>()
    private var isDrawn = false

    override fun onAttach() {
        graphicsLayer = currentValueOf(LocalGraphicsContext).createGraphicsLayer()
        updatedFields = UpdatedFieldsHolder(Fields.All)
        observeReads { handleUpdatedFields() }
    }

    fun processUpdate() {
        displayedContent = DisplayedContent(emptyList(), Rect.Zero)
        updatedFields = UpdatedFieldsHolder(Fields.All)
        handleUpdatedFields()
    }

    override fun onObservedReadsChanged() {
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
        scope.updatedFields = UpdatedFieldsHolder()
        updatedFields = UpdatedFieldsHolder()
    }


    private fun ContentDrawScope.recordDisplayedContent(layer: GraphicsLayer): Boolean {
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
                    left = offset.x.coerceAtMost(size.width),
                    top = offset.y.coerceAtMost(size.height),
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
        if (scope.backgroundColor.isSpecified) {
            drawOutline(it, scope.backgroundColor)
        }
    }

    private fun ContentDrawScope.drawTint() = outline?.let {
        if (scope.tintColor.isSpecified) {
            drawOutline(it, scope.tintColor)
        }
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
        block?.invoke(scope)
        updatedFields += scope.updatedFields.value
        scope.updatedFields = UpdatedFieldsHolder()
        if (blurHolder.specifiedBlurredContents != blurredContents) {
            updatedFields += Fields.BlurredContents
            blurredContents = blurHolder.specifiedBlurredContents
        }
        if (updatedFields.any(Fields.GraphicsLayerAffectingFields)) {
            updateGraphicsLayer()
            updatedFields -= Fields.GraphicsLayerAffectingFields
        }
        if (updatedFields.any(Fields.OutlineAffectingFields)) {
            updateOutline()
            updatedFields -= Fields.OutlineAffectingFields
        }
        if (updatedFields.any(Fields.BlurredContentAffectingFields)) {
            updatedFields -= Fields.BlurredContentAffectingFields
            if (recalculateDisplayedContent(updatedFields)) return
        }
        if (updatedFields.any(Fields.OnlyDrawAffectingFields)) {
            updatedFields -= Fields.OnlyDrawAffectingFields
            if (isDrawn) invalidateDraw()
        }
    }

    private fun updateGraphicsLayer() = graphicsLayer?.apply {
        renderEffect = if (scope.blurRadius.isSpecified && scope.blurRadius.value > 0f) {
            val blurRadiusPx = with(currentValueOf(LocalDensity)) { scope.blurRadius.toPx() }
            BlurEffect(blurRadiusPx, blurRadiusPx, scope.tileMode)
        } else {
            null
        }
        clip = true
    }

    private fun updateOutline(
        size: Size = this.size.toSize(),
        layoutDirection: LayoutDirection = requireLayoutDirection(),
        density: Density = requireDensity(),
    ) {
        outline = scope.shape.createOutline(
            size = size,
            layoutDirection = layoutDirection,
            density = density,
        )
    }

    private fun recalculateDisplayedContent(updatedFields: UpdatedFieldsHolder): Boolean {
        if (positionOnScreen.isUnspecified || size == IntSize.Zero) return false
        val blurRadiusPx = with(currentValueOf(LocalDensity)) { scope.blurRadius.toPx() }
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
    val scope: BlurredBackgroundScopeImpl,
    val block: (BlurredBackgroundScope.() -> Unit)?,
) : ModifierNodeElement<BlurredBackgroundModifierNode>() {
    override fun create() = BlurredBackgroundModifierNode(blurHolder, scope, block)

    override fun update(node: BlurredBackgroundModifierNode) {
        node.blurHolder = blurHolder
        node.scope = scope
        node.block = block
        node.processUpdate()
    }

    override fun InspectorInfo.inspectableProperties() {}
}