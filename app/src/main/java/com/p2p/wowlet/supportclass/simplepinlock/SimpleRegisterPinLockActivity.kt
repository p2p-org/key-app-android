package com.p2p.wowlet.supportclass.simplepinlock

import android.os.Bundle
import android.widget.TextView
import com.p2p.wowlet.R

abstract class SimpleRegisterPinLockActivity : SimplePinLockActivity() {
    var pin1 = ""
    var pin2 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val messageTextView = findViewById<TextView>(R.id.messageTextView)
        messageTextView.text = getString(R.string.simplepinlock_enter_pin1)
    }

    override fun onPinInputFinished() {
        if (pin1 == "") {
            pin1 = getPin()

            clearPin()
            val messageTextView = findViewById<TextView>(R.id.messageTextView)
            messageTextView.text = getString(R.string.simplepinlock_enter_pin2)

        } else if (pin2 == "") {
            pin2 = getPin()

            if (pin1 == pin2) {
                onPinRegistered(getPin())

                setResult(PIN_REGISTERED)
                finish()
            } else {
                clearPin()
                pin1 = ""
                pin2 = ""
                val messageTextView = findViewById<TextView>(R.id.messageTextView)
                messageTextView.text = getString(R.string.simplepinlock_pin_mismatch)
            }
        }
    }

    abstract fun onPinRegistered(pin: String)
}