package com.p2p.wowlet.supportclass.simplepinlock

import android.os.Bundle
import android.widget.TextView
import com.p2p.wowlet.R

abstract class SimpleConfirmPinLockActivity : SimplePinLockActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val messageTextView = findViewById<TextView>(R.id.messageTextView)
        messageTextView.text = getString(R.string.simplepinlock_request_pin)
    }

    override fun onPinInputFinished() {
        if (isPinCorrect(getPin())) {
            setResult(PIN_CONFIRMED)
            finish()
        } else {
            clearPin()
            val messageTextView = findViewById<TextView>(R.id.messageTextView)
            messageTextView.text = getString(R.string.simplepinlock_invalid_pin)
        }
    }

    /*
     * return true if pin is correct
     */
    abstract fun isPinCorrect(pin: String) : Boolean
}