package org.p2p.wallet.common.ui.widget

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetInputTextViewBinding
import org.p2p.wallet.utils.LetterInputFilter

@Deprecated("delete one day")
class InputTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetInputTextViewBinding.inflate(
        LayoutInflater.from(context), this
    )

    private val colorIdle = context.getColor(R.color.backgroundDisabled)
    private val colorFocused = context.getColor(R.color.backgroundButtonPrimary)
    private val colorError = context.getColor(R.color.systemErrorMain)
    private val colorSuccess = context.getColor(R.color.systemSuccessMain)

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.InputTextView)

        val suffixText = typedArray.getText(R.styleable.InputTextView_suffixText)
        binding.textInputLayout.suffixText = suffixText

        val hintText = typedArray.getText(R.styleable.InputTextView_hint)
        binding.textInputLayout.hint = hintText

        val maxLength = typedArray.getInt(R.styleable.InputTextView_android_maxLength, -1)
        if (maxLength != -1) {
            binding.textInputEditText.filters += InputFilter.LengthFilter(maxLength)
        }

        val digits = typedArray.getString(R.styleable.InputTextView_android_digits)
        if (digits != null) {
            binding.textInputEditText.filters += LetterInputFilter(digits)
        }
        typedArray.recycle()
    }

    fun doAfterTextChanged(block: (Editable?) -> Unit) {
        binding.textInputEditText.doAfterTextChanged { block.invoke(it) }
    }

    fun getText(): String {
        return binding.textInputEditText.text.toString()
    }

    fun setMessageWithState(text: String, state: State) {
        binding.placeholderTextView.text = text
        setState(state)
    }

    fun focusAndShowKeyboard() {
        binding.textInputEditText.focusAndShowKeyboard()
    }

    private fun setState(state: State) {
        binding.progressBar.isVisible = state == State.Loading
        when (state) {
            State.Idle -> setIdleState()
            State.Loading -> setLoadingState()
            State.Error -> setErrorState()
            State.Success -> setSuccessState()
        }
    }

    private fun setIdleState() {
        with(binding) {
            textInputLayout.boxStrokeColor = colorIdle
            textInputLayout.hintTextColor = ColorStateList.valueOf(colorIdle)

            placeholderTextView.setTextColor(colorIdle)
        }
    }

    private fun setLoadingState() {
        with(binding) {
            textInputLayout.boxStrokeColor = colorFocused
            textInputLayout.hintTextColor = ColorStateList.valueOf(colorFocused)

            placeholderTextView.setTextColor(colorFocused)
        }
    }

    private fun setErrorState() {
        with(binding) {
            textInputLayout.boxStrokeColor = colorError
            textInputLayout.hintTextColor = ColorStateList.valueOf(colorError)

            placeholderTextView.setTextColor(colorError)
        }
    }

    private fun setSuccessState() {
        with(binding) {
            textInputLayout.boxStrokeColor = colorFocused
            textInputLayout.hintTextColor = ColorStateList.valueOf(colorFocused)

            placeholderTextView.setTextColor(colorSuccess)
        }
    }

    enum class State {
        Loading,
        Error,
        Idle,
        Success
    }
}
