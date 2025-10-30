package io.github.neilyich.glassmorphism

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import io.github.neilyich.glassmorphism.utils.Fields
import io.github.neilyich.glassmorphism.utils.UpdatedFieldsHolder
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Scope used to update parameters of [blurredBackground] without recompositions. 
 * Each parameter affects only draw phase
 */
@Stable
interface BlurredBackgroundScope {
    /** radius of blur */
    var blurRadius: Dp

    /** shape of blurred background */
    var shape: Shape

    /** additional color to blurred background */
    var tintColor: Color

    /** color drawn behind blurred background */
    var backgroundColor: Color

    /** tileMode used in BlurEffect */
    var tileMode: TileMode
}

@Stable
internal data class BlurredBackgroundScopeImpl(
    private val initialBlurRadius: Dp,
    private val initialShape: Shape,
    private val initialTintColor: Color,
    private val initialBackgroundColor: Color,
    private val initialTileMode: TileMode,
) : BlurredBackgroundScope {
    var updatedFields = UpdatedFieldsHolder(
        Fields.BlurRadius or
                Fields.Shape or
                Fields.TintColor or
                Fields.BackgroundColor or
                Fields.TileMode
    )

    override var blurRadius by UpdatedField(initialBlurRadius, Fields.BlurRadius)
    override var shape by UpdatedField(initialShape, Fields.Shape)
    override var tintColor by UpdatedField(initialTintColor, Fields.TintColor)
    override var backgroundColor by UpdatedField(initialBackgroundColor, Fields.BackgroundColor)
    override var tileMode by UpdatedField(initialTileMode, Fields.TileMode)

    private class UpdatedField<T>(
        initialValue: T,
        val field: Int
    ) : ReadWriteProperty<BlurredBackgroundScopeImpl, T> {
        private var valueState by mutableStateOf(initialValue)

        override fun getValue(
            thisRef: BlurredBackgroundScopeImpl,
            property: KProperty<*>
        ): T {
            return valueState
        }

        override fun setValue(
            thisRef: BlurredBackgroundScopeImpl,
            property: KProperty<*>,
            value: T
        ) {
            if (valueState != value) {
                thisRef.updatedFields += field
                valueState = value
            }
        }
    }
}