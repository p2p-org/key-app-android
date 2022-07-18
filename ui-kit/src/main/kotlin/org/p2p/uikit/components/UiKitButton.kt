package org.p2p.uikit.components

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.button.MaterialButton
import org.p2p.uikit.R
import org.p2p.uikit.utils.toPx

private const val LOADER_RADIUS_LARGE = 8f
private const val LOADER_RADIUS_SMALL = 6F
private const val LOADER_STROKE_WIDTH = 2f

class UiKitButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    private var currentIcon: Drawable? = null
    private val circularProgressDrawable = CircularProgressDrawable(context)

    var isLoading: Boolean = false
        set(loading) {
            renderLoading(loading)
            field = loading
        }

    private val drawableCallback = object : Drawable.Callback {
        override fun unscheduleDrawable(who: Drawable, what: Runnable) {}

        override fun invalidateDrawable(who: Drawable) {
            this@UiKitButton.invalidate()
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {}
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitButton).use { typedArray ->
            val defaultColor = context.getColor(R.color.icons_night)
            val iconTint = typedArray.getColor(R.styleable.UiKitButton_iconTint, defaultColor)
            val height = typedArray.getLayoutDimension(
                R.styleable.UiKitButton_android_layout_height,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val smallButtonHeight = resources.getDimension(R.dimen.ui_kit_button_small_height)
            val loaderRadius = if (height > smallButtonHeight) {
                LOADER_RADIUS_LARGE.toPx()
            } else {
                LOADER_RADIUS_SMALL.toPx()
            }

            circularProgressDrawable.apply {
                setColorSchemeColors(iconTint)
                centerRadius = loaderRadius
                strokeWidth = LOADER_STROKE_WIDTH.toPx()
            }
        }
    }

    private fun renderLoading(isLoading: Boolean): Unit = if (isLoading) {
        currentIcon = icon
        icon = circularProgressDrawable
        icon.callback = drawableCallback
        circularProgressDrawable.start()
    } else {
        icon = currentIcon
        circularProgressDrawable.stop()
    }
}
