package org.p2p.wallet.common.ui.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import org.p2p.wallet.R
import org.p2p.wallet.utils.DecimalFormatUtil
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.orZero
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * This Watcher is responsible for limitation input amount by the user and formatting
 * We prohibit entering the value after the dot if it's exceeded the max allowed length
 * For example: 10000.1234456789123123 -> 10 000.123456789
 * */

private const val SYMBOL_ZERO = "0"
private const val SYMBOL_DOT = "."
private const val MAX_AMOUNT_ALLOWED_FRACTION_LENGTH = 9

class AmountFractionTextWatcher(
    editText: EditText,
    private val maxLengthAllowed: Int,
    private val onValueChanged: (String) -> Unit
) : TextWatcher {

    companion object {
        fun installOn(
            editText: EditText,
            maxSymbolsAllowed: Int = MAX_AMOUNT_ALLOWED_FRACTION_LENGTH,
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
        val spaceWasRemoved = value.dropSpaces() == valueText.dropSpaces()
        var symbolsAfterCursor = value.length - field.get()?.selectionEnd.orZero()

        // Move cursor one char left if user tries to remove space
        if (spaceWasRemoved) symbolsAfterCursor++

        valueText = when {
            value.isBlank() -> value
            value == SYMBOL_ZERO && before == 1 -> emptyString()
            value == "$SYMBOL_ZERO$SYMBOL_ZERO" && start == 1 -> SYMBOL_ZERO
            value.startsWith(SYMBOL_DOT) -> "$SYMBOL_ZERO$value"
            value.endsWith(SYMBOL_DOT) -> value.dropLast(1).dropSpaces().formatDecimal() + SYMBOL_DOT
            value.contains(SYMBOL_DOT) -> handleValueWithDot(value)
            else -> value.dropSpaces().formatDecimal()
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

    private fun handleValueWithDot(value: String): String {
        val dotPosition = value.indexOf(SYMBOL_DOT)
        val intPart = value.substring(0, dotPosition).dropSpaces()

        // Remove extra dots and Shorten fractional part to maxLengthAllowed symbols
        val fractionalPart = value.substring(dotPosition + 1)
            .replace(SYMBOL_DOT, emptyString())
            .dropSpaces()
            .take(maxLengthAllowed)

        return intPart.formatDecimal() + SYMBOL_DOT + fractionalPart
    }

    private fun String.dropSpaces(): String {
        return replace(" ", emptyString())
    }

    private fun String.formatDecimal(): String {
        return DecimalFormatUtil.format(this.toBigDecimal(), MAX_AMOUNT_ALLOWED_FRACTION_LENGTH)
    }
}
