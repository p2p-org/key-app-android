package com.p2p.wowlet.supportclass.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.p2p.wowlet.R
import com.p2p.wowlet.entities.enums.PinCodeFragmentType

class PinView : LinearLayoutCompat {
    private var pin = ""
    private var pinCount = 6 // default size 6
    private var inputCountFinish = false // default size 6
    private var _wrongPinCodeCount = 0
    val wrongPinCodeCount
        get() = _wrongPinCodeCount

    lateinit var pinCodeFragmentType: PinCodeFragmentType

    var isFirstPinInput = false
    var createPinCode: ((pin: String) -> Unit)? = null
    var verifyPinCode: ((pin: String) -> Unit)? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        orientation = HORIZONTAL
        initPinView()
    }

    private fun initPinView() {
        val dotSize = resources.getDimensionPixelSize(R.dimen.dp_20)
        val dotMargin = resources.getDimensionPixelSize(R.dimen.dp_10)
        removeAllViews()
        (1..pinCount).forEach {
            val imageView = ImageView(context)
            val layoutParams = LayoutParams(dotSize, dotSize, 0.0f)
            layoutParams.setMargins(dotMargin, dotMargin, dotMargin, dotMargin)
            imageView.layoutParams = layoutParams
            if (it > pin.length) {
                imageView.setImageResource(R.drawable.bg_pin_code_dot_empty)
            } else {
                imageView.setImageResource(R.drawable.bg_pin_code_dot_fill)
            }
            this.addView(imageView)

        }
    }

    fun errorPinViewsDesign() {
        val childCount: Int = childCount
        for (i in 0 until childCount) {
            val v: ImageView = getChildAt(i) as ImageView
            v.setImageResource(R.drawable.bg_pin_code_error)
        }
    }

    fun onPinButtonClicked(text: String) {
        if(pinCodeFragmentType== PinCodeFragmentType.CREATE){
            inputPinCode(text)
        }else if (wrongPinCodeCount != 3) {
            inputPinCode(text)
        }
    }
    private fun inputPinCode(code:String){
        if (pin.length < pinCount) {
            this.pin += code
            initPinView()
        }
        if (pin.length == pinCount && !inputCountFinish) {
            inputCountFinish = true
            if (pinCodeFragmentType== PinCodeFragmentType.CREATE) {
                if (isFirstPinInput) {
                    verifyPinCode?.invoke(pin)
                    _wrongPinCodeCount++
                } else
                    createPinCode?.invoke(pin)
            } else {
                verifyPinCode?.invoke(pin)
                _wrongPinCodeCount++
            }
        }
    }
    fun clearPin() {
        inputCountFinish = false
        pin = ""
        initPinView()
    }

    fun onDeleteButtonClicked() {
        if (wrongPinCodeCount != 3) {
            inputCountFinish = false
            if (pin.isNotEmpty()) {
                pin = pin.substring(0, pin.length - 1)
                initPinView()
            }
        }
    }

    fun setMaxPinSize(pinCount: Int) {
        this.pinCount = pinCount
    }

}