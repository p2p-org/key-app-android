package org.p2p.wallet.common.ui

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.lang.ref.WeakReference

class SimpleMaskFormatter(
    val mask: String,
    val maskChar: Char = '#',
    val stablePlaceholders: Boolean = false,
    val stablePlaceholderChar: Char = '_'
) {
    /**
     * This variable just keeps last value of format() method
     * @return is not empty only after formatting.
     */
    var rawText: String = ""
        private set

    class MaskTextWatcher internal constructor(
        private val formatter: SimpleMaskFormatter,
        input: EditText
    ) : TextWatcher {
        private val weakInput: WeakReference<EditText> = WeakReference(input)
        private var isDeleting = false
        private var offset = 0
        private var cursorAtTheEnd = false

        private fun enableMasking(): Boolean {
            return weakInput.get() != null
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            isDeleting = count > after;
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            offset = start
            cursorAtTheEnd = s?.length == start + count
        }

        override fun afterTextChanged(s: Editable?) {
            if (s == null || !enableMasking()) {
                return
            }
            weakInput.get()?.removeTextChangedListener(this)
            val res = formatter.format(s.toString())
            weakInput.get()?.setText(res)

            if (!isDeleting) offset++
            if (cursorAtTheEnd) {
                weakInput.get()?.setSelection(res.length)
            } else {
                weakInput.get()?.setSelection(offset)
            }

            weakInput.get()?.addTextChangedListener(this)
        }
    }

    fun textWatcher(input: EditText): TextWatcher {
        return MaskTextWatcher(this, input)
    }

    fun format(input: String): String {
        // save source input
        rawText = input

        // if used stablePlaceholder and input is empty, just return mask with stablePlaceholderChars
        if (stablePlaceholders && input.isEmpty()) {
            return mask.replace(maskChar, stablePlaceholderChar)
        }

        val result = StringBuilder()
        // offset for source string to match mask
        var offset = 0

        // any characters in mask that are not a {@link #maskChar} with theirs indices in mask
        val placeholders = HashMap<Int, Char>(mask.length)
        for (m in mask.indices) {
            if (mask[m] != maskChar) placeholders[m] = mask[m]
        }

        // cleanup input string from spaces
        val inputLocal = input.filter { it != ' ' }
        // whether the input has a placeholder characters, we don't want to add them twice to the result
        val fixCollisions = input.none { placeholders.values.contains(it) }

        for (i in mask.indices) {
            // get placeholder for position
            val placeholder = placeholders[i]
            // calc input position according to offset given by the mask
            val inputPos = i - offset

            // if input length is smaller than mask and we don't have to add stablePlaceholders - break and return
            if (!stablePlaceholders && inputPos >= inputLocal.length) break

            // we haven't found a placeholder in the mask, so we add the character from the input to the result
            if (placeholder == null) {
                // whether used stablePlaceholder, add to result a {@link #stablePlaceholderChar}
                if (inputPos >= inputLocal.length) {
                    result.append(stablePlaceholderChar)
                } else {
                    val s = inputLocal[inputPos]
                    // attempt to avoid duplicating input char and the same placeholder char
                    if (!placeholders.values.contains(s) || !fixCollisions) {
                        result.append(s)
                    }
                }
            } else {
                // we have a placeholder in the mask, add it to the result
                result.append(placeholder)
                offset++

                // if at the same position where we have a placeholder we found the same character in the input,
                // adding negative offset to skip adding it twice in the next iteration
                if (inputPos < inputLocal.length && placeholder == inputLocal[inputPos]) {
                    offset--
                }
            }
        }
        return result.toString()
    }


}
