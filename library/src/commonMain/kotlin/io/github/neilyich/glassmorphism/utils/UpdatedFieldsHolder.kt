package io.github.neilyich.glassmorphism.utils

import kotlin.jvm.JvmInline

@Suppress("ConstPropertyName")
internal data object Fields {
    const val PositionOnScreen = 1
    const val Size = 1 shl 1
    const val Shape = 1 shl 2
    const val BlurRadius = 1 shl 3
    const val TintColor = 1 shl 4
    const val Density = 1 shl 5
    const val LayoutDirection = 1 shl 6
    const val BackgroundColor = 1 shl 7
    const val BlurredContents = 1 shl 8

    const val OutlineAffectingFields = Size or Shape or Density or LayoutDirection
    const val BlurredContentAffectingFields = OutlineAffectingFields or
            PositionOnScreen or
            BlurRadius or
            BlurredContents
    const val OnlyDrawAffectingFields = TintColor or BackgroundColor
}

@JvmInline
internal value class UpdatedFieldsHolder(val value: Int = 0) {
    operator fun plus(flag: Int): UpdatedFieldsHolder = UpdatedFieldsHolder(value or flag)
    operator fun minus(flag: Int): UpdatedFieldsHolder = UpdatedFieldsHolder(value and flag.inv())
    operator fun contains(flag: Int): Boolean = (flag and value) == flag
    fun any(flag: Int): Boolean = (flag and value) != 0

    fun fields() = buildString {
        if (contains(Fields.PositionOnScreen)) append("PositionOnScreen,")
        if (contains(Fields.Size)) append("Size,")
        if (contains(Fields.Shape)) append("Shape,")
        if (contains(Fields.BlurRadius)) append("BlurRadius,")
        if (contains(Fields.TintColor)) append("TintColor,")
        if (contains(Fields.Density)) append("Density,")
        if (contains(Fields.LayoutDirection)) append("LayoutDirection,")
        if (contains(Fields.BackgroundColor)) append("BackgroundColor,")
        if (contains(Fields.BlurredContents)) append("BlurredContents,")
    }
}