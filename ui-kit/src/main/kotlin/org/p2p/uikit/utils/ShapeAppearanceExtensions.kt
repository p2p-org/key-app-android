package org.p2p.uikit.utils

import androidx.annotation.AttrRes
import androidx.appcompat.content.res.AppCompatResources
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewOutlineProvider
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

fun View.rippleBackground(
    shape: ShapeAppearanceModel? = shapeRectangle(),
    @AttrRes color: Int = android.R.attr.selectableItemBackground,
) {
    foreground = AppCompatResources.getDrawable(context, resFromTheme(color))
    if (shape != null) shapeOutline(shape)
}

class RoundOutlineProvider : ViewOutlineProvider() {

    private val rect = Rect()
    private var destination = RectF()

    private var shadowDrawable: MaterialShapeDrawable? = null
    private var shape: ShapeAppearanceModel? = null

    fun update(newShape: ShapeAppearanceModel, view: View, destination: RectF) {
        this.shape = newShape
        if (shadowDrawable == null) {
            shadowDrawable = MaterialShapeDrawable(newShape)
        }
        this.destination = destination
        view.invalidate()
        view.invalidateOutline()
    }

    override fun getOutline(view: View, outline: Outline) {
        val shapeAppearanceModel = shape ?: return
        if (shadowDrawable == null) {
            shadowDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        }
        destination.round(rect)
        shadowDrawable?.bounds = rect
        shadowDrawable?.getOutline(outline)
    }
}

fun View.shapeOutline(
    shape: ShapeAppearanceModel
) {
    val destination = RectF()

    fun updateOutline() {
        val outlineProvider = this.outlineProvider
        destination.set(0f, 0f, width.toFloat(), height.toFloat())
        if (outlineProvider is RoundOutlineProvider) {
            outlineProvider.update(shape, this, destination)
        } else {
            this.outlineProvider = RoundOutlineProvider().also {
                clipToOutline = true
                it.update(shape, this, destination)
            }
        }
    }

    addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
        updateOutline()
    }
    updateOutline()
}

fun shapeRectangle(): ShapeAppearanceModel = ShapeAppearanceModel.builder().build()

fun shapeRounded24dp(): ShapeAppearanceModel = ShapeAppearanceModel.builder()
    .setAllCorners(CornerFamily.ROUNDED, 24f.toPx())
    .build()

fun shapeTopRounded24dp(): ShapeAppearanceModel = shapeTopRounded(topDp = 24f)

fun shapeBottomRounded24dp(): ShapeAppearanceModel = shapeBottomRounded(bottomDp = 24f)

fun shapeTopRounded(topDp: Float = 0f): ShapeAppearanceModel = shapeRounded(
    topLeftDp = topDp,
    topRightDp = topDp,
)

fun shapeBottomRounded(bottomDp: Float = 0f): ShapeAppearanceModel = shapeRounded(
    bottomRightDp = bottomDp,
    bottomLeftDp = bottomDp,
)

fun shapeRoundedAll(
    cornerSizeDp: Float = 0f,
): ShapeAppearanceModel = ShapeAppearanceModel.builder()
    .setAllCorners(CornerFamily.ROUNDED, cornerSizeDp)
    .build()

fun shapeRounded(
    topLeftDp: Float = 0f,
    topRightDp: Float = 0f,
    bottomRightDp: Float = 0f,
    bottomLeftDp: Float = 0f,
): ShapeAppearanceModel = ShapeAppearanceModel.builder()
    .setTopLeftCorner(CornerFamily.ROUNDED, topLeftDp.toPx())
    .setTopRightCorner(CornerFamily.ROUNDED, topRightDp.toPx())
    .setBottomRightCorner(CornerFamily.ROUNDED, bottomRightDp.toPx())
    .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeftDp.toPx())
    .build()
