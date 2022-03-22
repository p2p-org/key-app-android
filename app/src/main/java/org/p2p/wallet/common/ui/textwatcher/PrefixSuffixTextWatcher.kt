package org.p2p.wallet.common.ui.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.widget.EditText
import org.p2p.wallet.R
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.getString
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.properties.Delegates

private const val COMMA_SYMBOL = ","
private const val DOT_SYMBOL = "."

class PrefixSuffixTextWatcher(
    editText: EditText,
    private val prefixSuffixSymbol: String,
    private val onValueChanged: ((PrefixData) -> Unit)? = null,
    private val validInput: String,
    private val isSuffix: Boolean = true
) : TextWatcher {

    companion object {
        fun installOn(
            editText: EditText,
            prefixSuffixSymbol: String = Constants.USD_SYMBOL,
            isSuffix: Boolean = true,
            onValueChanged: ((PrefixData) -> Unit)? = null,
        ): PrefixSuffixTextWatcher {
            val validInput = editText.getString(R.string.buy_value_allowed_symbols)
            val validInputWithSuffix = arrayOf(
                prefixSuffixSymbol,
                validInput
            )
            val prefixWatcher = PrefixSuffixTextWatcher(
                editText,
                prefixSuffixSymbol,
                onValueChanged,
                validInput,
                isSuffix
            )
            editText.keyListener = DigitsKeyListener.getInstance(validInputWithSuffix.joinToString())
            editText.addTextChangedListener(prefixWatcher)
            editText.setTag(R.id.prefix_watcher_tag_id, prefixWatcher)
            return prefixWatcher
        }

        fun uninstallFrom(editText: EditText) {
            val prefixWatcher = editText.getTag(R.id.prefix_watcher_tag_id) as? PrefixSuffixTextWatcher
            editText.removeTextChangedListener(prefixWatcher)
        }
    }

    private val field = WeakReference(editText)

    private var valueText: PrefixData by Delegates.observable(PrefixData()) { _, oldValue, newValue ->
        if (oldValue != newValue) onValueChanged?.invoke(newValue)
    }

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        val clearedValue = text.toString().replace(COMMA_SYMBOL, DOT_SYMBOL).filter {
            it in validInput
        }

        if (clearedValue.isEmpty()) {
            valueText = PrefixData()
            return
        }

        val finalText = if (isSuffix) {
            "$clearedValue$prefixSuffixSymbol"
        } else {
            "$prefixSuffixSymbol$clearedValue"
        }
        valueText = PrefixData(
            prefixText = finalText,
            valueWithoutPrefix = clearedValue
        )
    }

    override fun afterTextChanged(edit: Editable?) {
        field.get()?.apply {
            removeTextChangedListener(this@PrefixSuffixTextWatcher)
            edit?.clear()
            edit?.append(valueText.prefixText)
            if (isSuffix) {
                val textLastCharIndex = valueText.prefixText.length - 1
                val prefixSuffixSize = prefixSuffixSymbol.length - 1
                setSelection(max(0, textLastCharIndex - prefixSuffixSize))
            }
            addTextChangedListener(this@PrefixSuffixTextWatcher)
        }
    }
}
