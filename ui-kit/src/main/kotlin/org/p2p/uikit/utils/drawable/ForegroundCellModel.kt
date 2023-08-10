package org.p2p.uikit.utils.drawable

import android.view.View
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeRectangle

sealed interface ForegroundCellModel {

    /**
     * Before apply ripple, make sure colorControlHighlight is set in theme and it has right color
     */
    data class Ripple(
        val shape: ShapeAppearanceModel = shapeRectangle(),
    ) : ForegroundCellModel
}

fun ForegroundCellModel?.applyForeground(view: View) {
    when (this) {
        is ForegroundCellModel.Ripple -> applyForeground(view)
        else -> Unit
    }
}

fun ForegroundCellModel.Ripple.applyForeground(view: View) {
    view.rippleForeground(shape = shape)
}
