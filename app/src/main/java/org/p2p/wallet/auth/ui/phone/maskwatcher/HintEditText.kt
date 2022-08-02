/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */
package org.p2p.wallet.auth.ui.phone.maskwatcher

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

class HintEditText : AppCompatEditText {
    private var hintText: String? = null
    private var textOffset = 0f
    private var spaceSize = 0f
    private var numberSize = 0f
    private val paint = Paint()
    private val rect = Rect()

    var onEmptyDelete: (() -> Unit)? = null

    constructor(context: Context?) : super(context!!) {
        paint.color = Color.LTGRAY
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        paint.color = Color.LTGRAY
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttrs: Int) : super(
        context!!, attrs, defStyleAttrs
    ) {
        paint.color = Color.LTGRAY
    }

    fun getHintText(): String? {
        return hintText
    }

    fun setHintText(value: String?) {
        hintText = value
        onTextChange()
        text = text
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        onTextChange()
    }

    fun onTextChange() {
        textOffset = if (length() > 0) getPaint().measureText(text, 0, length()) else 0F
        spaceSize = getPaint().measureText(" ")
        numberSize = getPaint().measureText("1")
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (hintText != null && length() < hintText!!.length) {
            val top = measuredHeight / 2
            var offsetX = textOffset + paddingLeft
            for (a in length() until hintText!!.length) {
                if (hintText!![a] == ' ') {
                    offsetX += spaceSize
                } else {
                    rect[offsetX.toInt() + dip(1), top, (offsetX + numberSize).toInt() - dip(1)] = top + dip(2)
                    canvas.drawRect(rect, paint)
                    offsetX += numberSize
                }
            }
        }
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

    private fun dip(value: Int): Int {
        return (value * Resources.getSystem().displayMetrics.density).toInt()
    }
}
