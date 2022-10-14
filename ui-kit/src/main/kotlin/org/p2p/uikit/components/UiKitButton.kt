package org.p2p.uikit.components

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.content.res.use
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

    private var buttonIcon: Drawable? = null
    private val circularProgressDrawable = CircularProgressDrawable(context)

    var isLoadingState: Boolean = false
        set(loading) {
            renderLoadingState(loading)
            field = loading
        }

    private val progressDrawableCallback = object : Drawable.Callback {
        override fun unscheduleDrawable(who: Drawable, what: Runnable) = Unit

        override fun invalidateDrawable(who: Drawable) {
            this@UiKitButton.invalidate()
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) = Unit
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

        this.buttonIcon = icon
    }

    private fun renderLoadingState(isLoading: Boolean) {
        if (this.isLoadingState == isLoading) return
        if (isLoading) {
            buttonIcon = icon
            icon = circularProgressDrawable
            icon.callback = progressDrawableCallback
            circularProgressDrawable.start()
        } else {
            icon = buttonIcon
            circularProgressDrawable.stop()
        }
    }
}
