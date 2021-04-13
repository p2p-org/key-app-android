package com.p2p.wallet.utils

import android.app.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class DialogLifecycleObserver(
    private val lifecycleOwner: LifecycleOwner,
    private val dialog: Dialog,
    private val dismissCallback: (() -> Unit)?
) : LifecycleEventObserver {
    init {
        lifecycleOwner.lifecycle.also { lifecycle ->
            lifecycle.addObserver(this)
            dialog.setOnDismissListener {
                dismissCallback?.invoke()
                lifecycle.removeObserver(this)
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event > Lifecycle.Event.ON_RESUME) {
            dialog.dismiss()
            lifecycleOwner.lifecycle.removeObserver(this)
        }
    }
}