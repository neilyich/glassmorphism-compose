package io.github.neilyich.glassmorphism.samples

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.util.fastRoundToInt
import androidx.navigation.NavHostController
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import io.github.neilyich.glassmorphism.samples.Sample.BlurSettingsIconAnimation.A
import io.github.neilyich.glassmorphism.samples.Sample.BlurSettingsIconAnimation.B
import io.github.neilyich.glassmorphism.samples.Sample.BlurSettingsIconAnimation.C
import io.github.neilyich.glassmorphism.samples.Sample.BlurSettingsIconAnimation.MAX_ROTATION
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

abstract class Sample {
    abstract val name: String

    @Composable
    protected abstract fun Content(
        navController: NavHostController,
        blurSettings: BlurSettings,
        isSettingsIconVisible: Boolean,
    )

    @Composable
    protected abstract fun rememberDefaultBlurSettings(isBlurEnabled: Boolean): BlurSettings

    private var isBlurSettingsOpened by mutableStateOf(false)
    private var blurSettingsIconBounds by mutableStateOf(Rect.Zero)
    private val blurSettingsDialogAnimationProgress = Animatable(0f)

    private var rememberedBlurSettings: BlurSettings? = null

    @Composable
    fun Content(
        navController: NavHostController,
        isBlurEnabled: Boolean,
        isSettingsEnabled: Boolean,
    ) {
        if (!isSettingsEnabled) {
            val blurSettings = rememberedBlurSettings ?: rememberDefaultBlurSettings(isBlurEnabled)
            rememberedBlurSettings = blurSettings
            Content(navController, blurSettings, false)
            return
        }
        Box {
            val blurSettings = rememberedBlurSettings ?: rememberDefaultBlurSettings(isBlurEnabled)
            rememberedBlurSettings = blurSettings
            Content(navController, blurSettings, true)
            BlurSettingsDialog(blurSettings)
        }
    }

    private object BlurSettingsIconAnimation {
        const val MAX_ROTATION = 720f
        const val MIN_SCALE = 0.5f
        const val A = 4 * (1-MIN_SCALE) / MAX_ROTATION / MAX_ROTATION
        const val B = 4 * (MIN_SCALE-1) / MAX_ROTATION
        const val C = 1
    }

    @Composable
    protected fun BlurSettingsIcon(isVisible: Boolean) {
        if (!isVisible) return
        val iconRotation = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            snapshotFlow { isBlurSettingsOpened }.collectLatest {
                launch {
                    val targetRotation = if (it) MAX_ROTATION else 0
                    iconRotation.animateTo(targetRotation.toFloat(), tween(500))
                }
                launch {
                    val targetProgress = if (it) 1f else 0f
                    blurSettingsDialogAnimationProgress.animateTo(
                        targetValue = targetProgress,
                        animationSpec = tween(250),
                    )
                }
            }
        }
        Icon(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clickable(null, null) {
                    isBlurSettingsOpened = !isBlurSettingsOpened
                }
                .onGloballyPositioned {
                    blurSettingsIconBounds = it.boundsInRoot()
                }
                .graphicsLayer {
                    rotationZ = iconRotation.value
                    val scale = A*iconRotation.value*iconRotation.value + B*iconRotation.value + C
                    scaleX = scale
                    scaleY = scale
                },
            imageVector = Icons.Outlined.Settings,
            contentDescription = null,
        )
    }

    @Composable
    private fun BlurSettingsDialog(blurSettings: BlurSettings) {
        Column(
            modifier = Modifier
                .layout { m, c ->
                    val p = m.measure(c)
                    layout(p.width, p.height) {
                        val progress = blurSettingsDialogAnimationProgress.value
                        if (progress == 0f) return@layout
                        val positionInRoot = coordinates?.positionInRoot() ?: return@layout
                        val xInRoot = blurSettingsIconBounds.left + blurSettingsIconBounds.width - p.width
                        val yInRoot = blurSettingsIconBounds.top + blurSettingsIconBounds.height
                        p.placeWithLayer(
                            position = Offset(
                                x = xInRoot - positionInRoot.x,
                                y = yInRoot - positionInRoot.y,
                            ).round()
                        ) {
                            translationX = p.width/2f * (1f-progress)
                            translationY = -p.height/2f * (1f-progress)
                            scaleX = progress
                            scaleY = progress
                        }
                    }
                }
                .padding(top = 12.dp)
                .fillMaxWidth(0.8f)
                .dropShadow(MaterialTheme.shapes.large, Shadow(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.large)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Final).consume()
                    }
                }
                .padding(top = 12.dp)
                .padding(horizontal = 12.dp)
                .fillMaxWidth(),
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                BlurSettingsTitle()
                BlurRadiusSettings(blurSettings)
                ColorSettings("tintColor", blurSettings.tintColor) {
                    blurSettings.tintColor = it
                }
                ColorSettings("backgroundColor", blurSettings.backgroundColor) {
                    blurSettings.backgroundColor = it
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
                ResetToDefaultsButton(blurSettings)
            }
        }
    }

    @Composable
    private fun BlurSettingsTitle() {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier,
                text = "Blur Settings",
                style = MaterialTheme.typography.titleMedium,
            )
            Icon(
                modifier = Modifier
                    .clickable(null, null) {
                        isBlurSettingsOpened = false
                    },
                imageVector = Icons.Filled.Close,
                contentDescription = null,
            )
        }
    }

    @Composable
    private fun BlurRadiusSettings(blurSettings: BlurSettings) {
        Text("Radius: ${blurSettings.blurRadius.value.fastRoundToInt()}.dp")
        Slider(
            value = blurSettings.blurRadius.value,
            valueRange = 0f..100f,
            onValueChange = {
                blurSettings.blurRadius = it.dp
            },
        )
    }

    @Composable
    private fun ColorSettings(label: String, color: Color, onColorChanged: (Color) -> Unit) {
        var isEditMode by remember { mutableStateOf(false) }
        val controller = rememberColorPickerController()
        val editModeAnimationProgress by animateFloatAsState(
            targetValue = if (isEditMode) 1f else 0f,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = 0.001f,
            ),
        )
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("$label:")
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(28.dp)
                    .background(
                        if (isEditMode) {
                            controller.selectedColor.value
                        } else {
                            color
                        }, CircleShape)
            )
            Spacer(Modifier.weight(1f))
            AnimatedContent(
                targetState = isEditMode,
                contentAlignment = Alignment.CenterEnd,
                transitionSpec = {
                    if (targetState) {
                        slideInVertically { -it }.togetherWith(slideOutVertically { it })
                    } else {
                        slideInVertically { it }.togetherWith(slideOutVertically { -it })
                    }
                },
            ) { state ->
                TextButton(
                    onClick = {
                        controller.selectByColor(color, false)
                        isEditMode = !isEditMode
                    },
                ) {
                    Text(if (isEditMode) "Cancel" else "Edit")
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .layout { m, c ->
                    val p = m.measure(c)
                    val height = (p.height * editModeAnimationProgress).roundToInt().coerceAtLeast(0)
                    layout(p.width, height) {
                        p.placeWithLayer(0, 0) {
                            transformOrigin = TransformOrigin(0.5f, 0f)
                            scaleY = editModeAnimationProgress
                            alpha = editModeAnimationProgress
                        }
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Row(Modifier.fillMaxWidth(0.5f).padding(end = 4.dp)) {
                    HsvColorPicker(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        controller = controller,
                        initialColor = color,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(start =  4.dp)
                        .align(Alignment.TopEnd),
                ) {
                    AlphaSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        controller = controller,
                        initialColor = color,
                    )
                    BrightnessSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(24.dp),
                        controller = controller,
                        initialColor = color,
                    )
                }
                Box(
                    modifier = Modifier.fillMaxWidth(0.5f).align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = {
                            onColorChanged(controller.selectedColor.value)
                            isEditMode = false
                        },
                        enabled = controller.selectedColor.value != color,
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }

    @Composable
    private fun ResetToDefaultsButton(blurSettings: BlurSettings) {
        val defaultBlurSettings = rememberDefaultBlurSettings(blurSettings.isBlurEnabled)
        TextButton(
            onClick = {
                blurSettings.blurRadius = defaultBlurSettings.blurRadius
                blurSettings.tintColor = defaultBlurSettings.tintColor
                blurSettings.backgroundColor = defaultBlurSettings.backgroundColor
                blurSettings.shape = defaultBlurSettings.shape
                blurSettings.tileMode = defaultBlurSettings.tileMode
            },
        ) {
            Text("Reset to defaults")
        }
    }
}