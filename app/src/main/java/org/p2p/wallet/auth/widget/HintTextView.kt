package org.p2p.wallet.auth.widget

import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isInvisible
import android.content.Context
import android.util.AttributeSet
import org.p2p.wallet.utils.emptyString

class HintTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var hintText: String = emptyString()

    fun setHintText(hintText: String) {
        // we don't need that symbol in hint because input has no such symbols
        text = hintText.filterNot { it == '-' }
        this.hintText = hintText
    }

    /**
     * We are updating the hint according to the entered number
     * Thus, the hint is always visible and not visible for integers which are entered
     * */
    fun updateNumber(numberText: String) {
        val updatedHint = buildString {
            append(numberText)

            val hintTextLeft = (hintText.length - numberText.length).coerceAtLeast(0)
            append(hintText.takeLast(hintTextLeft))
        }

        text = updatedHint

        hideIfNeeded(numberText)
    }

    /**
     * If the input is too long, we are resizing the text
     * Thus, the user can see the hint on the background
     * Therefore, we are hiding hint if there is too long input
     * */
    private fun hideIfNeeded(numberText: String) {
        val isVisible = numberText.length <= hintText.length
        isInvisible = !isVisible
    }
}
