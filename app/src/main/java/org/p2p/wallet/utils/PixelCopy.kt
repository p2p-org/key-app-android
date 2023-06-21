package org.p2p.wallet.utils

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.Window

@Deprecated("old code")
object PixelCopy {

    fun getBitmapView(view: View, window: Window, listener: PixelCopyListener) {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val locationOfViewWindow = IntArray(2)
        view.getLocationInWindow(locationOfViewWindow)

        val x = locationOfViewWindow[0]
        val y = locationOfViewWindow[1]

        val scope = Rect(x, y, x + view.width, y + view.height)
        val looper = Looper.getMainLooper()
        PixelCopy.request(
            window, scope, bitmap,
            { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    listener.onCopySuccess(bitmap)
                } else {
                    listener.onCopyError()
                }
            },
            Handler(looper)
        )
    }
}

interface PixelCopyListener {
    fun onCopySuccess(bitmap: Bitmap)
    fun onCopyError()
}
