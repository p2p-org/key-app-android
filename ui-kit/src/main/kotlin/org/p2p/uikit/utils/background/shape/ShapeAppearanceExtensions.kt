package org.p2p.uikit.utils.background.shape

import androidx.annotation.AttrRes
import androidx.annotation.Px
import androidx.appcompat.content.res.AppCompatResources
import android.view.View
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearanceModel.PILL
import org.p2p.uikit.utils.resFromTheme
import org.p2p.uikit.utils.toPx

fun View.rippleForeground(
    shape: ShapeAppearanceModel? = shapeRectangle(),
    @AttrRes color: Int = android.R.attr.selectableItemBackground,
) {
    foreground = AppCompatResources.getDrawable(context, resFromTheme(color))
    if (shape != null) shapeOutline(shape)
}

fun View.shapeOutline(
    shape: ShapeAppearanceModel?
) {
    if (shape == null) {
        this.outlineProvider = null
        invalidateOutline()
        return
    }
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

fun shapeCircle(): ShapeAppearanceModel = ShapeAppearanceModel.builder()
    .setAllCornerSizes(PILL)
    .build()

fun shapeRounded24dp(): ShapeAppearanceModel = ShapeAppearanceModel.builder()
    .setAllCorners(CornerFamily.ROUNDED, 24f.toPx())
    .build()

fun shapeTopRounded24dp(): ShapeAppearanceModel = shapeTopRounded(top = 24f.toPx())

fun shapeBottomRounded24dp(): ShapeAppearanceModel = shapeBottomRounded(bottom = 24f.toPx())

fun shapeTopRounded(@Px top: Float = 0f): ShapeAppearanceModel = shapeRounded(
    topLeft = top,
    topRight = top,
)

fun shapeBottomRounded(@Px bottom: Float = 0f): ShapeAppearanceModel = shapeRounded(
    bottomRight = bottom,
    bottomLeft = bottom,
)

fun shapeRoundedAll(
    @Px cornerSize: Float = 0f,
): ShapeAppearanceModel = ShapeAppearanceModel.builder()
    .setAllCorners(CornerFamily.ROUNDED, cornerSize)
    .build()

fun shapeRounded(
    @Px topLeft: Float = 0f,
    @Px topRight: Float = 0f,
    @Px bottomRight: Float = 0f,
    @Px bottomLeft: Float = 0f,
): ShapeAppearanceModel = ShapeAppearanceModel.builder()
    .setTopLeftCorner(CornerFamily.ROUNDED, topLeft)
    .setTopRightCorner(CornerFamily.ROUNDED, topRight)
    .setBottomRightCorner(CornerFamily.ROUNDED, bottomRight)
    .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeft)
    .build()
