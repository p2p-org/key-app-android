package org.p2p.wallet.auth.widget

import androidx.appcompat.widget.AppCompatTextView
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import org.p2p.wallet.utils.emptyString

class HintTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var hintText: String = emptyString()
    private var savedNumberText: String = emptyString()

    fun setHintText(hintText: String) {
        text = hintText
        this.hintText = hintText
    }

    /*
    * The number 1 has less width than other numbers
    * If the hint has 1 but entered number has something else then we'll see some spacing errors
    *
    * That's why we are moving the hint position for the space of the number which user entered
    * */
    fun updateNumber(numberText: String) {
        val isDeletion = numberText.isEmpty() || numberText.length < savedNumberText.length
        text = hintText.takeLast(hintText.length - numberText.length)

        if (isDeletion) {
            val lastDeletedNumber = savedNumberText.lastOrEmpty()
            savedNumberText = numberText

            val numberSize = paint.measureText(lastDeletedNumber)
            val newLayoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                leftMargin -= numberSize.toInt()
            }

            layoutParams = newLayoutParams
        } else {
            savedNumberText = numberText

            val lastNumber = numberText.lastOrEmpty()
            val numberSize = paint.measureText(lastNumber)
            val newLayoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                leftMargin += numberSize.toInt()
            }

            layoutParams = newLayoutParams
        }
    }
}

private fun String.lastOrEmpty(): String = lastOrNull()?.toString().orEmpty()
