package org.p2p.uikit.utils.image

import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.core.view.isVisible
import android.widget.ImageView
import org.p2p.core.common.IconContainer
import org.p2p.core.common.setIcon
import org.p2p.uikit.R
import org.p2p.uikit.utils.background.BackgroundUiModel
import org.p2p.uikit.utils.background.applyTo
import org.p2p.uikit.utils.background.shape.shapeCircle
import org.p2p.uikit.utils.background.shapeDrawable
import org.p2p.uikit.utils.getColorStateList

data class ImageViewUiModel(
    val icon: IconContainer,
    @ColorRes val iconTint: Int? = null,
    val scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER,
    // use MaterialShapeDrawable + ShapeAppearanceModel for clipping
    val background: BackgroundUiModel? = null,
)

fun commonCircleImage(
    icon: IconContainer,
    @ColorRes backgroundTint: Int = R.color.icons_rain,
    @Px strokeWidth: Float = 0f,
    @ColorRes strokeColor: Int? = null,
): ImageViewUiModel = ImageViewUiModel(
    icon = icon,
    background = BackgroundUiModel(
        background = shapeDrawable(
            shape = shapeCircle(),
        ),
        backgroundTint = backgroundTint,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
    )
)

fun ImageView.bindOrGone(model: ImageViewUiModel?) {
    this.isVisible = model != null
    if (model != null) bind(model)
}

fun ImageView.bind(model: ImageViewUiModel) {
    setIcon(model.icon)
    imageTintList = model.iconTint?.let { getColorStateList(it) }
    scaleType = model.scaleType
    model.background.applyTo(this)
}
