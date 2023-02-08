package org.p2p.uikit.model

import androidx.annotation.ColorRes
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.core.common.IconContainer
import org.p2p.uikit.R
import org.p2p.uikit.utils.shapeCircle

data class ImageViewUiModel(
    val icon: IconContainer,
    @ColorRes val iconTint: Int? = null,
    val scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER,

    // use ShapeAppearanceModel for clipping
    val background: Drawable? = null,
    @ColorRes val backgroundTint: Int? = null,

    val shape: ShapeAppearanceModel? = null,
    val strokeWidthDp: Int? = null,
    @ColorRes val strokeColor: Int? = null,
)

fun IconContainer.commonCircleImage(
    icon: IconContainer
): ImageViewUiModel = ImageViewUiModel(
    icon = icon,
    shape = shapeCircle(),
    backgroundTint = R.color.icons_rain,
)
