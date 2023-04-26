package org.p2p.wallet.auth.ui.reserveusername.widget

import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.widget.FrameLayout
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetReserveUsernameInputViewBinding
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import java.util.regex.Matcher
import java.util.regex.Pattern

fun interface ReserveUsernameInputViewListener {
    fun onInputChanged(newValue: String)
}

private val UPPER_CASE_REGEX: Pattern = Pattern.compile("[A-Z]")

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

    var usernamePostfixText: String
        get() = binding.textViewKeyPostfix.text.toString()
        set(value) {
            binding.textViewKeyPostfix.text = value
        }

    init {
        binding.editTextUsername.doAfterTextChanged { editable ->
            if (editable == null) return@doAfterTextChanged

            replaceUppercaseWithLowercase(editable)
            listener?.onInputChanged(editable.toString().lowercase())
        }
        binding.imageViewClear.setOnClickListener {
            binding.editTextUsername.setText(emptyString())
        }
    }

    private fun replaceUppercaseWithLowercase(editable: Editable) {
        val matcher: Matcher = UPPER_CASE_REGEX.matcher(editable)
        while (matcher.find()) {
            val upperCaseRegion = editable.subSequence(matcher.start(), matcher.end()).toString()
            editable.replace(
                matcher.start(),
                matcher.end(),
                upperCaseRegion.lowercase()
            )
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
