package org.p2p.core.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.lang.ref.WeakReference
import kotlin.properties.Delegates.observable
import org.p2p.core.R
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.orZero

/**
 * This Watcher is responsible for limitation input amount by the user and formatting
 * We prohibit entering the value after the dot if it's exceeded the max allowed length
 * For example: 10000.1234456789123123 -> 10 000.123456789
 * */
private const val MAX_FRACTION_LENGTH = 9

class AmountFractionTextWatcher(
    editText: EditText,
    maxLengthAllowed: Int,
    maxIntLength: Int,
    private val onValueChanged: (String) -> Unit
) : TextWatcher {

    private val amountFractionFormatter = AmountFractionFormatter(maxLengthAllowed, maxIntLength)

    companion object {
        private const val MAX_INT_LENGTH = 12

        fun installOn(
            editText: EditText,
            maxDecimalsAllowed: Int = MAX_FRACTION_LENGTH,
            maxIntLength: Int = MAX_INT_LENGTH,
            onValueChanged: (String) -> Unit
        ): AmountFractionTextWatcher {
            val textWatcher = AmountFractionTextWatcher(editText, maxDecimalsAllowed, maxIntLength, onValueChanged)
            editText.addTextChangedListener(textWatcher)
            editText.setTag(R.id.length_watcher_tag_id, textWatcher)
            return textWatcher
        }

        fun uninstallFrom(editText: EditText) {
            val textWatcher = editText.getTag(R.id.length_watcher_tag_id) as? AmountFractionTextWatcher
            editText.removeTextChangedListener(textWatcher)
        }
    }

    private val field = WeakReference(editText)

    private var valueText: String by observable(editText.text?.toString().orEmpty()) { _, oldValue, newValue ->
        if (oldValue != newValue) onValueChanged.invoke(newValue.dropSpaces())
    }

    private var cursorPosition: Int = 0

    init {
        field.get()?.apply {
            val valueText = amountFractionFormatter.formatAmountFraction(
                value = text.toString(),
                before = 0,
                start = 0
            )
            setText(valueText)
            setSelection(valueText.length)
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val value = s.toString()
        val spaceWasRemoved = before == 1 && value.dropSpaces() == valueText.dropSpaces()
        var symbolsAfterCursor = value.length - field.get()?.selectionEnd.orZero()

        // Move cursor one char left if user tries to remove space
        if (spaceWasRemoved) symbolsAfterCursor++

        valueText = amountFractionFormatter.formatAmountFraction(value, before, start)
        // Keep cursor in same place from the end
        cursorPosition = if (valueText.length < symbolsAfterCursor) 0 else valueText.length - symbolsAfterCursor
    }

    override fun afterTextChanged(edit: Editable) {
        field.get()?.apply {
            removeTextChangedListener(this@AmountFractionTextWatcher)
            setText(valueText)
            try {
                setSelection(cursorPosition)
            } catch (e: IndexOutOfBoundsException) {
                val selectionIndex = if (valueText.isNotEmpty()) valueText.length - 1 else 0
                setSelection(selectionIndex)
            }

            addTextChangedListener(this@AmountFractionTextWatcher)
        }
    }

    private fun String.dropSpaces(): String {
        return replace(" ", emptyString())
    }
}
