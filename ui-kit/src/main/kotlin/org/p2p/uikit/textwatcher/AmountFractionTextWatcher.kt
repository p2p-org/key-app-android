package org.p2p.uikit.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import org.p2p.uikit.R
import org.p2p.uikit.utils.emptyString
import org.p2p.uikit.utils.orZero
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * This Watcher is responsible for limitation input amount by the user and formatting
 * We prohibit entering the value after the dot if it's exceeded the max allowed length
 * For example: 10000.1234456789123123 -> 10 000.123456789
 * */
private const val MAX_FRACTION_LENGTH = 9

class AmountFractionTextWatcher(
    editText: EditText,
    maxLengthAllowed: Int,
    private val onValueChanged: (String) -> Unit
) : TextWatcher {

    private val amountFractionFormatter = AmountFractionFormatter(maxLengthAllowed)

    companion object {
        fun installOn(
            editText: EditText,
            maxSymbolsAllowed: Int = MAX_FRACTION_LENGTH,
            onValueChanged: (String) -> Unit
        ): AmountFractionTextWatcher {
            val textWatcher = AmountFractionTextWatcher(editText, maxSymbolsAllowed, onValueChanged)
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

    private var valueText: String by Delegates.observable(emptyString()) { _, oldValue, newValue ->
        if (oldValue != newValue) onValueChanged.invoke(newValue.dropSpaces())
    }

    private var cursorPosition: Int = 0

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
            setSelection(cursorPosition)

            addTextChangedListener(this@AmountFractionTextWatcher)
        }
    }

    private fun String.dropSpaces(): String {
        return replace(" ", emptyString())
    }
}
