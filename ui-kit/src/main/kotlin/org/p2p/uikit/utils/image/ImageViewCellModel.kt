package org.p2p.uikit.utils.image

import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.core.view.isVisible
import android.widget.ImageView
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.core.common.DrawableContainer
import org.p2p.uikit.R
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.applyForeground
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeOutline
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.getColorStateList

data class ImageViewCellModel(
    val icon: DrawableContainer,
    @ColorRes val iconTint: Int? = null,
    val scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER,
    val background: DrawableCellModel? = null,
    val foreground: DrawableCellModel? = null,
    val clippingShape: ShapeAppearanceModel? = null,
)

fun commonCircleImage(
    icon: DrawableContainer,
    @ColorRes iconTint: Int? = null,
    @ColorRes backgroundTint: Int = R.color.icons_rain,
    @Px strokeWidth: Float = 0f,
    @ColorRes strokeColor: Int = android.R.color.transparent,
): ImageViewCellModel = ImageViewCellModel(
    icon = icon,
    iconTint = iconTint,
    background = DrawableCellModel(
        drawable = shapeDrawable(shape = shapeCircle()),
        tint = backgroundTint,
    ),
    foreground = DrawableCellModel(
        drawable = shapeDrawable(shape = shapeCircle()),
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
    ),
    clippingShape = shapeCircle()
)

fun ImageView.bindOrGone(model: ImageViewCellModel?) {
    this.isVisible = model != null
    if (model != null) bind(model)
}

fun ImageView.bind(model: ImageViewCellModel) {
    model.icon.applyTo(this)
    imageTintList = model.iconTint?.let { getColorStateList(it) }
    scaleType = model.scaleType
    model.background.applyBackground(this)
    model.foreground.applyForeground(this)
    shapeOutline(model.clippingShape)
}
