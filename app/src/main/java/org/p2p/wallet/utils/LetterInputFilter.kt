package org.p2p.wallet.utils

import android.text.InputFilter
import android.text.Spanned

class LetterInputFilter(private val availableChars: String) : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val subSequence = source?.subSequence(start, end)
        return if (subSequence?.all { availableChars.contains(it) } == true) subSequence.toString() else emptyString()
    }
}
