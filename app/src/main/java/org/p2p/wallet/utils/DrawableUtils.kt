package org.p2p.wallet.utils

import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable

object DrawableUtils {
    fun getDrawableWithSizes(
        context: Context,
        @DrawableRes drawableId: Int,
        @Dimension(unit = Dimension.DP)
        width: Int,
        @Dimension(unit = Dimension.DP)
        height: Int,
    ): Drawable? {
        val drawable = AppCompatResources.getDrawable(context, drawableId) ?: return null
        return getResizedDrawableUsingSpecificSize(drawable, width, height)
    }

    private fun getResizedDrawableUsingSpecificSize(drawable: Drawable, newWidth: Int, newHeight: Int): LayerDrawable {
        return LayerDrawable(arrayOf(drawable)).also { it.setLayerSize(0, newWidth, newHeight) }
    }
}
