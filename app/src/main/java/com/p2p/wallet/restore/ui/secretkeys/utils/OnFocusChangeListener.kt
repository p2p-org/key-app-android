package com.p2p.wallet.restore.ui.secretkeys.utils

import android.view.View
import android.view.ViewGroup
import com.p2p.wallet.R

class OnFocusChangeListener : View.OnFocusChangeListener {
    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        val parent = v?.parent as ViewGroup
        if (!hasFocus) {
            parent.setBackgroundResource(R.drawable.bg_secret_keyword)
        }
    }
}