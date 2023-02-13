package org.p2p.uikit.utils.drawable

import androidx.annotation.ColorRes
import androidx.annotation.Px
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.uikit.utils.drawable.shape.shapeRectangle

data class DrawableCellModel(
    val drawable: Drawable = shapeDrawable(),
    @ColorRes val tint: Int = android.R.color.transparent,
    // only MaterialShapeDrawable support
    @Px val strokeWidth: Float = 0f,
    // only MaterialShapeDrawable support
    @ColorRes val strokeColor: Int = android.R.color.transparent,
)

fun shapeDrawable(
    shape: ShapeAppearanceModel = shapeRectangle(),
) = MaterialShapeDrawable(shape)

fun DrawableCellModel?.applyBackground(view: View) {
    val model = this
    if (model == null) {
        view.background = null
        view.backgroundTintList = null
        return
    }
    val drawable = model.mutateDrawable(view.context)
    view.background = drawable
    view.backgroundTintList = view.context.getColorStateList(model.tint)
}

fun DrawableCellModel?.applyForeground(view: View) {
    val model = this
    if (model == null) {
        view.foreground = null
        view.foregroundTintList = null
        return
    }
    val drawable = model.mutateDrawable(view.context)
    view.foreground = drawable
    view.foregroundTintList = view.context.getColorStateList(model.tint)
}

private fun DrawableCellModel.mutateDrawable(context: Context): Drawable? {
    val drawable = this.drawable.mutate().constantState?.newDrawable()
    if (drawable is MaterialShapeDrawable) {
        drawable.strokeColor = context.getColorStateList(this.strokeColor)
        drawable.strokeWidth = this.strokeWidth
    }
    return drawable
}
