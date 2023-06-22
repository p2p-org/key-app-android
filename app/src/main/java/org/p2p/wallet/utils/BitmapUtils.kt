package org.p2p.wallet.utils

import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas

object BitmapUtils {
    fun fromVectorDrawable(
        context: Context,
        @DrawableRes drawableId: Int
    ): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, drawableId) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}
