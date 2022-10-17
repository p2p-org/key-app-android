package org.p2p.wallet.auth.ui.reserveusername.widget

import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetReserveUsernameInputViewBinding
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

fun interface ReserveUsernameInputViewListener {
    fun onInputChanged(newValue: String)
}

class ReserveUsernameInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    enum class InputState {
        USERNAME_INVALID,
        USERNAME_CHECK,
        USERNAME_AVAILABLE,
        USERNAME_NOT_AVAILABLE
    }

    private val binding = inflateViewBinding<WidgetReserveUsernameInputViewBinding>()

    var listener: ReserveUsernameInputViewListener? = null

    init {
        binding.editTextUsername.doAfterTextChanged {
            it?.toString()?.also { listener?.onInputChanged(it) }
        }
        binding.imageViewClear.setOnClickListener {
            binding.editTextUsername.setText(emptyString())
        }
    }

    fun renderState(state: InputState) {
        when (state) {
            InputState.USERNAME_INVALID -> {
                binding.progressBar.isVisible = false
                binding.textViewBottomMessage.isInvisible = false
                binding.textViewBottomMessage.setText(R.string.reserve_username_input_username_hint_message)
                binding.textViewBottomMessage.setTextColorRes(R.color.text_night)
            }
            InputState.USERNAME_CHECK -> {
                binding.progressBar.isVisible = true
                binding.textViewBottomMessage.isInvisible = true
            }
            InputState.USERNAME_AVAILABLE -> {
                binding.progressBar.isVisible = false
                binding.textViewBottomMessage.isInvisible = false
                binding.textViewBottomMessage.setText(R.string.reserve_username_input_username_available)
                binding.textViewBottomMessage.setTextColorRes(R.color.text_night)
            }
            InputState.USERNAME_NOT_AVAILABLE -> {
                binding.progressBar.isVisible = false
                binding.textViewBottomMessage.isInvisible = false
                binding.textViewBottomMessage.setText(R.string.reserve_username_input_username_not_available)
                binding.textViewBottomMessage.setTextColorRes(R.color.text_rose)
            }
        }
    }
}
