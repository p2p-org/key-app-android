package org.p2p.uikit.utils

import android.graphics.Outline
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.AttrRes
import androidx.appcompat.content.res.AppCompatResources
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
    var shape: ShapeAppearanceModel? = null
        private set

    fun update(newShape: ShapeAppearanceModel) {
        this.shape = newShape
        shadowDrawable?.shapeAppearanceModel = newShape
    }

    override fun getOutline(view: View, outline: Outline) {
        val shapeAppearanceModel = shape ?: return
        if (shadowDrawable == null) {
            shadowDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        }
        destination.set(0f, 0f, view.width.toFloat(), view.height.toFloat())
        destination.round(rect)
        shadowDrawable?.bounds = rect
        shadowDrawable?.getOutline(outline)
    }
}

fun View.shapeOutline(
    shape: ShapeAppearanceModel
) {
    val outlineProvider = this.outlineProvider
    if (outlineProvider is RoundOutlineProvider) {
        outlineProvider.update(shape)
        invalidateOutline()
    } else {
        this.outlineProvider = RoundOutlineProvider().also {
            it.update(shape)
            clipToOutline = true
        }
    }
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
