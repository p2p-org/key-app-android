package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.os.postDelayed
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetPinViewBinding
import org.p2p.wallet.utils.emptyString

private const val PIN_CODE_LENGTH = 6
private const val DELAY_MS = 50L

class PinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onPinCompleted: ((String) -> Unit)? = null
    var onBiometricClicked: (() -> Unit)? = null
    var onResetClicked: (() -> Unit)? = null

    private var pinCode: String = emptyString()

    private val pinHandler = Handler(Looper.getMainLooper())

    private val binding = WidgetPinViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL

        binding.pinCodeView.setPinLength(PIN_CODE_LENGTH)

        binding.keyboardView.setLeftButtonVisible(false)
        binding.keyboardView.setRightButtonDrawable(R.drawable.ic_backspace)

        binding.keyboardView.onNumberClicked = { number -> onNumberEntered(number) }

        binding.keyboardView.onLeftButtonClicked = {
            onBiometricClicked?.invoke()
        }

        binding.keyboardView.onRightButtonClicked = {
            pinCode = pinCode.dropLast(1)
            updateDots()
        }
    }

    fun startErrorAnimation(errorText: String) {
        with(binding) {
            messageTextView.setTextColor(context.getColor(R.color.systemErrorMain))
            messageTextView.text = errorText
            messageTextView.isVisible = true
            pinCodeView.startErrorAnimation {
                messageTextView.isInvisible = true
                clearPin()
            }
        }
    }

    fun startSuccessAnimation(text: String, onAnimationFinished: () -> Unit) {
        with(binding) {
            messageTextView.setTextColor(context.getColor(R.color.systemSuccessMain))
            messageTextView.text = text
            messageTextView.isVisible = true
            pinCodeView.startSuccessAnimation {
                messageTextView.isVisible = true
                clearPin()
                onAnimationFinished()
            }
        }
    }

    fun showLockedState(message: String) {
        with(binding) {
            keyboardView.isEnabled = false
            pinCodeView.isVisible = false
            progressBar.isVisible = false

            messageTextView.isVisible = true
            messageTextView.text = message
        }
    }

    fun showUnlockedState() {
        with(binding) {
            keyboardView.isEnabled = true
            pinCodeView.isVisible = true
            progressBar.isVisible = true

            messageTextView.isVisible = false
            messageTextView.text = emptyString()
        }
    }

    fun setFingerprintVisible(isVisible: Boolean) {
        binding.keyboardView.setLeftButtonVisible(isVisible)
    }

    fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.pinCodeView.isInvisible = isLoading
        binding.keyboardView.isEnabled = !isLoading
    }

    fun clearPin() {
        pinCode = emptyString()
        updateDots()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.keyboardView.isEnabled = enabled
    }

    private fun onNumberEntered(number: Char) {
        if (pinCode.length == PIN_CODE_LENGTH) return

        if (pinCode.length < PIN_CODE_LENGTH) {
            pinCode += number
            updateDots()
        }

        if (pinCode.length == PIN_CODE_LENGTH) {
            pinHandler.postDelayed(DELAY_MS) { onPinCompleted?.invoke(pinCode) }
        }
    }

    private fun updateDots() {
        binding.pinCodeView.refresh(pinCode.length)
        binding.keyboardView.setRightButtonVisible(pinCode.isNotEmpty())
    }
}
