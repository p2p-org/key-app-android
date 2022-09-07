package org.p2p.uikit.organisms.pin

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.os.postDelayed
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetUiKitPinViewBinding
import org.p2p.uikit.utils.emptyString
import org.p2p.uikit.utils.inflateViewBinding

private const val PIN_CODE_LENGTH = 6
private const val DELAY_MS = 50L

class UiKitPinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onPinCompleted: ((String) -> Unit)? = null
    var onBiometricClicked: (() -> Unit)? = null
    var onKeyboardClicked: (() -> Unit)? = null

    private var pinCode: String = emptyString()

    private val pinHandler = Handler(Looper.getMainLooper())

    private val binding = inflateViewBinding<WidgetUiKitPinViewBinding>()

    init {
        orientation = VERTICAL

        binding.pinCodeView.setPinLength(PIN_CODE_LENGTH)

        binding.keyboardView.setLeftButtonVisible(false)
        binding.keyboardView.setRightButtonDrawable(R.drawable.ic_new_backspace)

        binding.keyboardView.onNumberClicked = { number ->
            onNumberEntered(number)
            onKeyboardClicked?.invoke()
        }

        binding.keyboardView.onLeftButtonClicked = {
            onBiometricClicked?.invoke()
        }

        binding.keyboardView.onRightButtonClicked = {
            onKeyboardClicked?.invoke()
            pinCode = pinCode.dropLast(1)
            updateDots()
        }
    }

    fun startErrorAnimation() {
        binding.pinCodeView.startErrorAnimation {
            clearPin()
        }
    }

    fun onSuccessPin() {
        binding.pinCodeView.setSuccessDotsColor()
    }

    fun resetDotsColor() {
        binding.pinCodeView.resetDotsColor()
    }

    fun showLockedState() {
        with(binding) {
            keyboardView.isEnabled = false
            pinCodeView.isVisible = false
            progressBar.isVisible = false
        }
    }

    fun showUnlockedState() {
        with(binding) {
            keyboardView.isEnabled = true
            pinCodeView.isVisible = true
            progressBar.isVisible = true
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
    }
}
