package org.p2p.wallet.common.ui.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import org.p2p.wallet.R
import org.p2p.wallet.utils.Constants.SYMBOL_ZERO
import org.p2p.wallet.utils.DecimalFormatter
import org.p2p.wallet.utils.NoOp
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.orZero
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * This Watcher is responsible for limitation input amount by the user and formatting
 * We prohibit entering the value after the dot if it's exceeded the max allowed length
 * For example: 10000.1234456789123123 -> 10 000.123456789
 * */

private const val SYMBOL_DOT = "."
private const val MAX_INT_LENGTH = 12
private const val MAX_FRACTION_LENGTH = 9

class AmountFractionTextWatcher(
    editText: EditText,
    private val maxLengthAllowed: Int,
    private val onValueChanged: (String) -> Unit
) : TextWatcher {

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

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = NoOp

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val value = s.toString()
        val spaceWasRemoved = before == 1 && value.dropSpaces() == valueText.dropSpaces()
        var symbolsAfterCursor = value.length - field.get()?.selectionEnd.orZero()

        // Move cursor one char left if user tries to remove space
        if (spaceWasRemoved) symbolsAfterCursor++

        valueText = when {
            value.isEmpty() -> value
            // Remove whole string if dot before zero was removed
            value == SYMBOL_ZERO && before == 1 -> emptyString()
            value == "$SYMBOL_ZERO$SYMBOL_ZERO" && start == 1 -> SYMBOL_ZERO
            value.startsWith(SYMBOL_DOT) -> "$SYMBOL_ZERO$value"
            value.endsWith(SYMBOL_DOT) -> handleEndWithDotCase(value)
            value.contains(SYMBOL_DOT) -> handleValueWithDotCase(value)
            else -> handleGeneralCase(value)
        }

        // Keep cursor in same place from the end
        cursorPosition = if (valueText.length < symbolsAfterCursor) 0 else valueText.length - symbolsAfterCursor
    }

    override fun afterTextChanged(edit: Editable?) {
        field.get()?.apply {
            removeTextChangedListener(this@AmountFractionTextWatcher)
            field.get()?.let {
                setText(valueText)
                setSelection(cursorPosition)
            }
            addTextChangedListener(this@AmountFractionTextWatcher)
        }
    }

    private fun handleEndWithDotCase(value: String): String = value.dropLast(1)
        .dropSpaces()
        .formatDecimal(maxLengthAllowed) + SYMBOL_DOT

    private fun handleGeneralCase(value: String): String = value.dropSpaces()
        .take(MAX_INT_LENGTH)
        .formatDecimal(maxLengthAllowed)

    private fun handleValueWithDotCase(value: String): String {
        val dotPosition = value.indexOf(SYMBOL_DOT)
        val intPart = value.substring(0, dotPosition)
            .dropSpaces()
            .take(MAX_INT_LENGTH)

        // Remove extra dots and shorten fractional part to maxLengthAllowed symbols
        val fractionalPart = value.substring(dotPosition + 1)
            .dropSpaces()
            .replace(SYMBOL_DOT, emptyString())
            .take(maxLengthAllowed)

        return intPart.formatDecimal(maxLengthAllowed) + SYMBOL_DOT + fractionalPart
    }

    private fun String.dropSpaces(): String {
        return replace(" ", emptyString())
    }

    private fun String.formatDecimal(fractionLength: Int): String {
        return DecimalFormatter.format(this.toBigDecimal(), fractionLength)
    }
}
