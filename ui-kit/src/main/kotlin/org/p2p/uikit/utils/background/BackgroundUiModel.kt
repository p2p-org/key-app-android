package org.p2p.uikit.utils.background

import androidx.annotation.ColorRes
import androidx.annotation.Px
import android.graphics.drawable.Drawable
import android.view.View
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.uikit.utils.background.shape.shapeRectangle

data class BackgroundUiModel(
    // preferred to use MaterialShapeDrawable + ShapeAppearanceModel
    val background: Drawable = shapeDrawable(),
    @ColorRes val backgroundTint: Int? = null,
    @Px val strokeWidth: Float = 0f,
    @ColorRes val strokeColor: Int? = null,
)

fun shapeDrawable(
    shape: ShapeAppearanceModel = shapeRectangle(),
) = MaterialShapeDrawable(shape)

fun BackgroundUiModel?.applyTo(view: View) {
    val model = this
    if (model == null) {
        view.background = null
        view.backgroundTintList = null
        return
    }
    val drawable = model.background.mutate().constantState?.newDrawable()
    if (drawable is MaterialShapeDrawable) {
        drawable.strokeColor = model.strokeColor?.let { view.context.getColorStateList(it) }
        drawable.strokeWidth = model.strokeWidth
    }
    view.background = drawable
    view.backgroundTintList = model.backgroundTint?.let { view.context.getColorStateList(it) }
}
