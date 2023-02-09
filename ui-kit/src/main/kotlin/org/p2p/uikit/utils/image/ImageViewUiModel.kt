package org.p2p.uikit.utils.image

import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.core.view.isVisible
import android.widget.ImageView
import com.google.android.material.shape.MaterialShapeDrawable
import org.p2p.core.common.IconContainer
import org.p2p.core.common.setIcon
import org.p2p.uikit.R
import org.p2p.uikit.utils.background.BackgroundUiModel
import org.p2p.uikit.utils.background.shape.shapeCircle
import org.p2p.uikit.utils.background.shapeDrawable
import org.p2p.uikit.utils.getColorStateList
import org.p2p.uikit.utils.text.bind

data class ImageViewUiModel(
    val icon: IconContainer,
    @ColorRes val iconTint: Int? = null,
    val scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER,
    // use ShapeAppearanceModel for clipping
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
            strokeWidth = strokeWidth,
        ),
        backgroundTint = backgroundTint,
        strokeColor = strokeColor,
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
    model.background.let { bg ->
        val drawable = bg?.background?.mutate()?.constantState?.newDrawable()
        if (drawable is MaterialShapeDrawable) {
            drawable.strokeColor = bg.strokeColor?.let { getColorStateList(it) }
        }
        this.background = drawable
        backgroundTintList = bg?.backgroundTint?.let { getColorStateList(it) }
    }
}
