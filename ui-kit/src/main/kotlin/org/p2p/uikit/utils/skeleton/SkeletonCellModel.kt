package org.p2p.uikit.utils.skeleton

import androidx.annotation.Px
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.view.Gravity
import android.view.View
import org.p2p.uikit.R
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.getColorStateList

data class SkeletonCellModel(
    @Px val height: Int,
    @Px val width: Int,
    @Px val radius: Float = 0f,
    val gravity: Int = Gravity.CENTER,
)

fun View.bindSkeleton(model: SkeletonCellModel) {

    fun setupSkeleton(drawable: PaintDrawable): Drawable = drawable.apply {
        setCornerRadius(model.radius)
        intrinsicWidth = model.width
        intrinsicHeight = model.height
    }

    val shimmer = Shimmer.ColorHighlightBuilder()
        .setHighlightAlpha(0f)
        .setHighlightColor(getColor(R.color.bg_snow))
        .setBaseAlpha(1f)
        .setBaseColor(getColor(R.color.bg_rain))
        .setAutoStart(true)
        .build()
    val skeletonDrawable = SkeletonDrawable()
    skeletonDrawable.setShimmer(shimmer)

    val shadowBackground = setupSkeleton(PaintDrawable())
    val skeleton = setupSkeleton(skeletonDrawable)

    background = shadowBackground
    backgroundTintList = getColorStateList(android.R.color.transparent)
    foreground = skeleton
    foregroundGravity = model.gravity
}
