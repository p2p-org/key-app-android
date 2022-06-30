package org.p2p.uikit.atoms.cells

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
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

    var isRead: Boolean = false
        set(value) {
            setBackground(value)
            field = value
        }

    init {
        minWidth = MIN_SIZE_DP.toPx()
        minHeight = MIN_SIZE_DP.toPx()
        gravity = Gravity.CENTER

        val textColor = ContextCompat.getColor(context, R.color.text_snow)
        setBackground(isRead)
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
            text.isNullOrBlank() -> count = 0
            !text.all { it.isDigit() || it == PLUS_SIGN } -> {
                throw IllegalArgumentException("Text should be digits and '+' only")
            }
            else -> super.setText(text, type)
        }
    }

    fun inc() {
        count = count.inc()
    }

    fun dec() {
        count = count.dec()
    }

    private fun validateCount(value: Int): String = if (value > VISIBLE_COUNT_MAX_VALUE) {
        "$VISIBLE_COUNT_MAX_VALUE+"
    } else {
        value.toString()
    }

    private fun setBackground(isRead: Boolean) {
        val drawableRes = if (isRead) R.drawable.background_counter_read else R.drawable.background_counter_new
        setBackgroundResource(drawableRes)
    }
}
