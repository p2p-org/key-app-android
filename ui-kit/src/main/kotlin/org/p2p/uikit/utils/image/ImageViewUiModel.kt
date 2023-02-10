package org.p2p.uikit.utils.image

import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.core.view.isVisible
import android.widget.ImageView
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.core.common.IconContainer
import org.p2p.core.common.setIcon
import org.p2p.uikit.R
import org.p2p.uikit.utils.drawable.DrawableUiModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.applyForeground
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeOutline
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.getColorStateList

data class ImageViewUiModel(
    val icon: IconContainer,
    @ColorRes val iconTint: Int? = null,
    val scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER,
    val background: DrawableUiModel? = null,
    val foreground: DrawableUiModel? = null,
    val clippingShape: ShapeAppearanceModel? = null,
)

fun commonCircleImage(
    icon: IconContainer,
    @ColorRes backgroundTint: Int = R.color.icons_rain,
    @Px strokeWidth: Float = 0f,
    @ColorRes strokeColor: Int = android.R.color.transparent,
): ImageViewUiModel = ImageViewUiModel(
    icon = icon,
    background = DrawableUiModel(
        drawable = shapeDrawable(shape = shapeCircle()),
        tint = backgroundTint,
    ),
    foreground = DrawableUiModel(
        drawable = shapeDrawable(shape = shapeCircle()),
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
    ),
    clippingShape = shapeCircle()
)

fun ImageView.bindOrGone(model: ImageViewUiModel?) {
    this.isVisible = model != null
    if (model != null) bind(model)
}

fun ImageView.bind(model: ImageViewUiModel) {
    setIcon(model.icon)
    imageTintList = model.iconTint?.let { getColorStateList(it) }
    scaleType = model.scaleType
    model.background.applyBackground(this)
    model.foreground.applyForeground(this)
    shapeOutline(model.clippingShape)
}
