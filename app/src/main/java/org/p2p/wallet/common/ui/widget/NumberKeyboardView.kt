package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.view.isInvisible
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetKeyboardBinding

class NumberKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    var onNumberClicked: ((Char) -> Unit)? = null
    var onLeftButtonClicked: (() -> Unit)? = null
    var onRightButtonClicked: (() -> Unit)? = null

    private var buttons = mutableListOf<NumberKeyboardButtonView>()

    private val binding = WidgetKeyboardBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        initKeyboardButtons()
    }

    private fun initKeyboardButtons() {
        with(binding) {
            buttons.apply {
                add(zeroPinCodeButton)
                add(onePinCodeButton)
                add(twoPinCodeButton)
                add(threePinCodeButton)
                add(fourPinCodeButton)
                add(fivePinCodeButton)
                add(sixPinCodeButton)
                add(sevenPinCodeButton)
                add(eightPinCodeButton)
                add(ninePinCodeButton)
                add(additionalRightPinCodeButton)
                add(additionalLeftPinCodeButton)
            }

            buttons.forEach {
                it.setOnClickListener { view -> onClick(view) }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.additionalLeftPinCodeButton -> onLeftButtonClicked?.invoke()
            R.id.additionalRightPinCodeButton -> onRightButtonClicked?.invoke()
            else -> {
                val char = when (view.id) {
                    R.id.zeroPinCodeButton -> '0'
                    R.id.onePinCodeButton -> '1'
                    R.id.twoPinCodeButton -> '2'
                    R.id.threePinCodeButton -> '3'
                    R.id.fourPinCodeButton -> '4'
                    R.id.fivePinCodeButton -> '5'
                    R.id.sixPinCodeButton -> '6'
                    R.id.sevenPinCodeButton -> '7'
                    R.id.eightPinCodeButton -> '8'
                    R.id.ninePinCodeButton -> '9'
                    else -> throw IllegalArgumentException("ID does not reference a View inside this View")
                }

                onNumberClicked?.invoke(char)
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        buttons.forEach { it.isEnabled = enabled }
        super.setEnabled(enabled)
    }

    fun setLeftButtonVisible(isVisible: Boolean) {
        binding.additionalLeftPinCodeButton.isInvisible = !isVisible
        binding.additionalLeftPinCodeButton.setBackgroundColor(
            context.getColor(android.R.color.transparent)
        )
    }

    fun setLeftButtonBackground(@DrawableRes drawableResId: Int) {
        binding.additionalLeftPinCodeButton.setBackgroundResource(drawableResId)
    }

    fun setLeftButtonDrawable(@DrawableRes drawableResId: Int) {
        binding.additionalLeftPinCodeButton.setIcon(drawableResId)
    }

    fun setRightButtonVisible(isVisible: Boolean) {
        binding.additionalRightPinCodeButton.isInvisible = !isVisible
    }

    fun setRightButtonDrawable(@DrawableRes drawableResId: Int) {
        binding.additionalRightPinCodeButton.setIcon(drawableResId)
    }
}
