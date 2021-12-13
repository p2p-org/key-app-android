package org.p2p.wallet.common.ui.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import org.p2p.wallet.R
import org.p2p.wallet.utils.Constants
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class PrefixTextWatcher(
    editText: EditText,
    private val prefixSymbol: String,
    private val onValueChanged: ((String) -> Unit)? = null
) : TextWatcher {

    companion object {
        fun installOn(
            editText: EditText,
            prefixSymbol: String = Constants.USD_SYMBOL,
            onValueChanged: ((String) -> Unit)? = null
        ): PrefixTextWatcher {
            val prefixWatcher = PrefixTextWatcher(editText, prefixSymbol, onValueChanged)
            editText.addTextChangedListener(prefixWatcher)
            editText.setTag(R.id.prefix_watcher_tag_id, prefixWatcher)
            return prefixWatcher
        }

        fun uninstallFrom(editText: EditText) {
            val prefixWatcher = editText.getTag(R.id.prefix_watcher_tag_id) as? PrefixTextWatcher
            editText.removeTextChangedListener(prefixWatcher)
        }
    }

    private val field = WeakReference(editText)

    private var valueText: String by Delegates.observable("") { _, oldValue, newValue ->
        if (oldValue != newValue) onValueChanged?.invoke(newValue)
    }

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        val clearedValue = text.toString().filter { it.isDigit() }

        if (clearedValue.isEmpty()) {
            valueText = ""
            return
        }

        val formattedValue = String.format("%,d", clearedValue.toBigInteger())
        valueText = "$prefixSymbol $formattedValue"
    }

    override fun afterTextChanged(edit: Editable?) {
        field.get()?.apply {
            removeTextChangedListener(this@PrefixTextWatcher)
            edit?.clear()
            edit?.append(valueText)
            addTextChangedListener(this@PrefixTextWatcher)
        }
    }
}