package org.p2p.uikit.utils.drawable

import androidx.annotation.ColorRes
import org.p2p.uikit.utils.drawable.shape.shapeCircle

object UiKitDrawableCellModels {
    fun shapeCircleWithTint(@ColorRes tint: Int): DrawableCellModel =
        DrawableCellModel(
            drawable = shapeDrawable(shapeCircle()),
            tint = tint
        )
}
