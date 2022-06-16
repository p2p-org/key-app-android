package org.p2p.uikit.atoms

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.button.MaterialButton
import org.p2p.uikit.R
import org.p2p.uikit.utils.toPx

private const val LOADER_RADIUS = 8f
private const val LOADER_STROKE_WIDTH = 2f

class UiKitButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    private var currentIcon: Drawable? = null
    private val circularProgressDrawable = initCircularProgressDrawable()

    var isLoading: Boolean = false
        set(loading) {
            if (loading) {
                currentIcon = icon
                icon = circularProgressDrawable
                icon.callback = drawableCallback
                circularProgressDrawable.start()
            } else {
                icon = currentIcon
                circularProgressDrawable.stop()
            }
            field = loading
        }

    private val drawableCallback = object : Drawable.Callback {
        override fun unscheduleDrawable(who: Drawable, what: Runnable) {}

        override fun invalidateDrawable(who: Drawable) {
            this@UiKitButton.invalidate()
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {}
    }

    private fun initCircularProgressDrawable() = CircularProgressDrawable(context).apply {
        setColorSchemeColors(context.getColor(R.color.lime))
        centerRadius = LOADER_RADIUS.toPx()
        strokeWidth = LOADER_STROKE_WIDTH.toPx()
    }
}
