package com.p2p.wallet.restore.ui.secretkeys.utils

import android.graphics.Color
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.restore.ui.secretkeys.adapter.SecretPhraseAdapter

class KeywordEditOnKeyListener(
    private val adapter: SecretPhraseAdapter,
    var recyclerView: RecyclerView? = null,
    var _position: Int? = null
) : View.OnKeyListener {
    override fun onKey(currentView: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (currentView !is AppCompatEditText) return false
        if (_position == null) return false
        val position = _position!!

        if (position == 0) return false
        if (keyCode != KeyEvent.KEYCODE_DEL ||
            event?.action != KeyEvent.ACTION_DOWN ||
            currentView.text.toString().isNotEmpty()
        ) {
            return false
        }

        val previousItem: ViewGroup = recyclerView?.getChildAt(position - 1) as ViewGroup
        val previousTextNum = previousItem.getChildAt(0) as AppCompatTextView
        val previousEdtKeyword = previousItem.getChildAt(1) as AppCompatEditText
        adapter.removeItemAt(position, previousItem, previousTextNum, previousEdtKeyword, currentView)
        previousItem.setBackgroundColor(Color.TRANSPARENT)
        return false
    }
}