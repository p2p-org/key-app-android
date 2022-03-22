package org.p2p.wallet.restore.ui.keys.adapter

import android.view.KeyEvent
import android.view.View
import android.widget.EditText

class OnKeyEditTextListener(
    private val onKeyRemoved: () -> Unit
) : View.OnKeyListener {

    override fun onKey(currentView: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (currentView !is EditText) return false

        if (keyCode != KeyEvent.KEYCODE_DEL ||
            event?.action != KeyEvent.ACTION_DOWN ||
            currentView.text.toString().isNotEmpty()
        ) {
            return false
        }

        onKeyRemoved()
        return true
    }
}
