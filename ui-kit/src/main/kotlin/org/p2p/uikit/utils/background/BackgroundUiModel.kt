package org.p2p.uikit.utils.background

import androidx.annotation.ColorRes
import androidx.annotation.Px
import android.graphics.drawable.Drawable
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.uikit.utils.background.shape.shapeRectangle

data class BackgroundUiModel(
    // preferred to use MaterialShapeDrawable + ShapeAppearanceModel
    val background: Drawable = shapeDrawable(),
    @ColorRes val backgroundTint: Int? = null,
    @ColorRes val strokeColor: Int? = null,
)

fun shapeDrawable(
    shape: ShapeAppearanceModel = shapeRectangle(),
    @Px strokeWidth: Float? = null,
) = MaterialShapeDrawable(shape)
    .apply {
        if (strokeWidth != null) this.strokeWidth = strokeWidth
//        if (strokeColor != null) this.strokeColor = Resources.getSystem().getColorStateList(strokeColor, null)
    }
