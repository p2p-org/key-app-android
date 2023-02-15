package org.p2p.uikit.utils.skeleton

import androidx.annotation.ColorRes
import androidx.annotation.Px
import org.p2p.core.common.TextContainer
import org.p2p.uikit.R
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx

fun skeletonText(
    length: Int = 6,
    @Px radius: Float = 6f.toPx(),
    @ColorRes tint: Int = R.color.bg_rain,
): TextViewCellModel {
    return TextViewCellModel(
        text = TextContainer("a".repeat(length)),
        textColor = android.R.color.transparent,
        background = DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(radius)),
            tint = tint,
        ),
    )
}
