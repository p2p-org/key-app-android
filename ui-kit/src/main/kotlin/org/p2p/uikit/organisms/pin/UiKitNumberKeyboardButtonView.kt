package org.p2p.uikit.organisms.pin

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetUiKitNumberButtonBinding

class UiKitNumberKeyboardButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetUiKitNumberButtonBinding.inflate(LayoutInflater.from(context), this)

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.UiKitNumberKeyboardButtonView)

            val text = typedArray.getString(R.styleable.UiKitNumberKeyboardButtonView_keyboard_button_text)
            if (text != null) {
                binding.keyboardButtonTextView.text = text
            }

            val image = typedArray.getDrawable(R.styleable.UiKitNumberKeyboardButtonView_keyboard_button_image)
            if (image != null) {
                binding.keyboardButtonImageView.setImageDrawable(image)
                binding.keyboardButtonImageView.visibility = View.VISIBLE
            }

            typedArray.recycle()
        }

        setBackgroundResource(R.drawable.bg_new_keyboard_selector)

        clipToOutline = true
    }

    fun setIcon(@DrawableRes drawableResId: Int) {
        binding.keyboardButtonImageView.setImageResource(drawableResId)
        binding.keyboardButtonImageView.visibility = View.VISIBLE
    }

    fun setPressedColor(color: Int) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.bg_new_keyboard_pressed) as RippleDrawable
        drawable.setColor(ColorStateList.valueOf(color))
        background = drawable
    }
}
