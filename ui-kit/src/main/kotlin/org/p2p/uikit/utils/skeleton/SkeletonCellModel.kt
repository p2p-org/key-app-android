package org.p2p.uikit.utils.skeleton

import androidx.annotation.ColorRes
import androidx.annotation.Px
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.Gravity
import android.view.View
import org.p2p.uikit.R

data class SkeletonCellModel(
    @Px val height: Int,
    @Px val width: Int,
    @Px val radius: Float = 0f,
    val gravity: Int = Gravity.CENTER,
    @ColorRes val tint: Int = R.color.bg_rain,
)

fun View.bindSkeleton(model: SkeletonCellModel) {
    val roundedCorners = floatArrayOf(
        model.radius,
        model.radius,
        model.radius,
        model.radius,
        model.radius,
        model.radius,
        model.radius,
        model.radius
    )

    val roundRectShape = RoundRectShape(roundedCorners, null, null)
    val skeletonDrawable = ShapeDrawable(roundRectShape)
    skeletonDrawable.intrinsicWidth = model.width
    skeletonDrawable.intrinsicHeight = model.height

    // shadow
    background = skeletonDrawable
    backgroundTintList = context.getColorStateList(android.R.color.transparent)

    foreground = skeletonDrawable.mutate().constantState?.newDrawable()
    foregroundTintList = context.getColorStateList(model.tint)
    model.gravity.let { foregroundGravity = it }
}
