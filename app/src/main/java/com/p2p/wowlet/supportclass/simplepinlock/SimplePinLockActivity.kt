package com.p2p.wowlet.supportclass.simplepinlock

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.p2p.wowlet.R
import com.p2p.wowlet.utils.dp2px

abstract class SimplePinLockActivity : AppCompatActivity(), Util {

    private var pin = ""

    companion object {
        val CANCELLED = 1001
        val PIN_REGISTERED = 1002
        val PIN_CONFIRMED = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_lock)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val gridView = findViewById<GridView>(R.id.gridView)
        gridView.adapter =
            PinButtonAdapter(this, pinFingerPrint = {}, pinButtonClick = {}, pinReset = {})

        gridView.numColumns = 3

        val cancelButton = findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            onCancelButtonPressed()
        }

        reloadPinView()
    }

    fun reloadPinView() {
        val pinView = findViewById<LinearLayout>(R.id.pinView)
        val dotSize = dp2px(this, 20.0f).toInt()
        val dotMargin = dp2px(this, 10.0f).toInt()
        pinView.removeAllViews()
        (1..getMaxPinSize()).forEach {
            val imageView = ImageView(this)
            val layoutParams = LinearLayout.LayoutParams(dotSize, dotSize, 0.0f)
            layoutParams.setMargins(dotMargin, dotMargin, dotMargin, dotMargin)
            imageView.layoutParams = layoutParams
            if (it > pin.length) {
                imageView.setImageResource(R.drawable.bg_pin_code_dot_empty)
            } else {
                imageView.setImageResource(R.drawable.bg_pin_code_dot_fill)
            }
            pinView.addView(imageView)
        }
    }

    open fun getMaxPinSize(): Int {
        return 4
    }

    open fun onPinButtonClicked(text: String) {
        if (pin.length < getMaxPinSize()) {
            this.pin += text
            reloadPinView()
        }

        if (pin.length == getMaxPinSize()) {
            onPinInputFinished()
        }
    }

    /*
     * Delete one character.
     */
    open fun onDeleteButtonClicked() {
        if (pin.length > 0) {
            pin = pin.substring(0, pin.length - 1)
            reloadPinView()
        }
    }

    fun getPin(): String {
        return pin
    }

    fun clearPin() {
        pin = ""
        reloadPinView()
    }


    abstract fun onPinInputFinished()

    /*
     * Called when "CANCEL" button is pressed.
     */
    open fun onCancelButtonPressed(): Boolean {
        setResult(CANCELLED)
        finish()
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (pin.length != 0) {
                onDeleteButtonClicked()
            } else {
                setResult(CANCELLED)
                finish()
            }
            return true
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }
}
