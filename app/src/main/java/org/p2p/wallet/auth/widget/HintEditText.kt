package org.p2p.wallet.auth.widget

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

class HintEditText : AppCompatEditText {

    private var hintText: String? = null

    var onEmptyDelete: (() -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttrs: Int) : super(context, attrs, defStyleAttrs)

    fun getHintText(): String? {
        return hintText
    }

    fun setHintText(value: String?) {
        hintText = value
        text = text
    }

    override fun isSaveEnabled(): Boolean {
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL && text.isNullOrEmpty()) {
            onEmptyDelete?.invoke()
        }
        return super.onKeyDown(keyCode, event)
    }
}
