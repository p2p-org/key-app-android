package org.p2p.uikit.atoms

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import org.p2p.uikit.R
import org.p2p.uikit.utils.toPx

private const val VISIBLE_COUNT_MAX_VALUE = 99
private const val MIN_SIZE_DP = 18
private const val VERTICAL_PADDING_DP = 0
private const val HORIZONTAL_PADDING_DP = 4
private const val PLUS_SIGN = '+'

class UiKitCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var count = 0
        set(value) {
            text = validateCount(value)
            field = value
        }

    @ColorInt
    var fillingColor: Int = context.getColor(R.color.mountain)
        set(value) {
            (background as? GradientDrawable)?.setColor(value)
            field = value
        }

    init {
        minWidth = MIN_SIZE_DP.toPx()
        minHeight = MIN_SIZE_DP.toPx()
        gravity = Gravity.CENTER

        val textColor = context.getColor(R.color.text_snow)
        setBackgroundResource(R.drawable.background_counter)
        setTextAppearance(R.style.UiKit_TextAppearance_Regular_Label2)
        setTextColor(textColor)
        setPadding(
            HORIZONTAL_PADDING_DP.toPx(),
            VERTICAL_PADDING_DP.toPx(),
            HORIZONTAL_PADDING_DP.toPx(),
            VERTICAL_PADDING_DP.toPx()
        )
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        when {
            text.isNullOrBlank() || !text.all { it.isDigit() || it == PLUS_SIGN } -> count = 0
            else -> super.setText(text, type)
        }
    }

    fun incCounter() {
        count = count.inc()
    }

    fun decCounter() {
        count = count.dec()
    }

    private fun validateCount(value: Int): String = if (value > VISIBLE_COUNT_MAX_VALUE) {
        "$VISIBLE_COUNT_MAX_VALUE+"
    } else {
        value.toString()
    }
}
