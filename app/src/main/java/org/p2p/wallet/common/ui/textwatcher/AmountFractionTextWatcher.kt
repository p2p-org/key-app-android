package org.p2p.wallet.common.ui.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import org.p2p.wallet.R
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * This Watcher is responsible for limitation input amount by the user.
 * We prohibit entering the value after the dot if it's exceeded the max allowed length
 * For example: 0.1234456789123123 -> 0.123456789
 * */

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

    private var valueText: String by Delegates.observable("") { _, oldValue, newValue ->
        if (oldValue != newValue) onValueChanged.invoke(newValue)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val value = s.toString()
        valueText = if (value.contains(".")) {
            val dividedValues = value.split(".")
            val valueAfterDelimiter = dividedValues.last()
            val fractionValue = valueAfterDelimiter.take(maxLengthAllowed)
            "${dividedValues.first()}.$fractionValue"
        } else {
            value
        }
    }

    override fun afterTextChanged(edit: Editable?) {
        field.get()?.apply {
            removeTextChangedListener(this@AmountFractionTextWatcher)
            edit?.clear()
            edit?.append(valueText)
            addTextChangedListener(this@AmountFractionTextWatcher)
        }
    }
}