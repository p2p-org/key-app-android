package org.p2p.uikit.components

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetAmountsViewBinding
import org.p2p.uikit.utils.inflateViewBinding

enum class FocusMode {
    TOKEN,
    CURRENCY
}

class UiKitAmountsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetAmountsViewBinding>()

    private lateinit var tokenTextWatcher: TextWatcher
    private lateinit var currencyTextWatcher: TextWatcher

    var token: String? = null
        set(value) {
            field = value
            binding.textViewToken.text = value
        }

    var currency: String? = null
        set(value) {
            field = value
            binding.textViewCurrency.text = value
        }

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_amounts_view)

        with(binding) {
            val originalTextSize = editTextTokenAmount.textSize
            editTextTokenAmount.doOnTextChanged { text, _, _, _ ->
                handleAmountTextChanged(editTextTokenAmount, textViewTokenAutoSizeHelper, text, originalTextSize)
            }
            editTextCurrencyAmount.doOnTextChanged { text, _, _, _ ->
                handleAmountTextChanged(editTextCurrencyAmount, textViewCurrencyAutoSizeHelper, text, originalTextSize)
            }
        }
    }

    fun getFocusMode(): FocusMode = if (binding.editTextTokenAmount.hasFocus()) {
        FocusMode.TOKEN
    } else {
        FocusMode.CURRENCY
    }

    fun setTokenAmount(tokenAmount: String) = with(binding.editTextTokenAmount) {
        removeTextChangedListener(tokenTextWatcher)
        setText(tokenAmount)
        addTextChangedListener(tokenTextWatcher)
    }

    fun setCurrencyAmount(currencyAmount: String) = with(binding.editTextCurrencyAmount) {
        removeTextChangedListener(currencyTextWatcher)
        setText(currencyAmount)
        addTextChangedListener(currencyTextWatcher)
    }

    fun setOnTokenAmountChangeListener(onTokenAmountChange: (String) -> Unit) {
        tokenTextWatcher = binding.editTextTokenAmount.doAfterTextChanged { onTokenAmountChange(it.toString()) }
    }

    fun setOnCurrencyAmountChangeListener(onCurrencyAmountChange: (String) -> Unit) {
        currencyTextWatcher =
            binding.editTextCurrencyAmount.doAfterTextChanged { onCurrencyAmountChange(it.toString()) }
    }

    fun setOnSelectTokenClickListener(onSelectTokenClick: () -> Unit) {
        binding.textViewToken.setOnClickListener { onSelectTokenClick() }
        binding.imageViewSelectToken.setOnClickListener { onSelectTokenClick() }
    }

    fun setOnSelectCurrencyClickListener(onSelectCurrencyClick: () -> Unit) {
        binding.textViewCurrency.setOnClickListener { onSelectCurrencyClick() }
        binding.imageViewSelectCurrency.setOnClickListener { onSelectCurrencyClick() }
    }

    fun setOnFocusChangeListener(onFocusChanged: (focusMode: FocusMode) -> Unit) {
        binding.editTextTokenAmount.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) onFocusChanged(FocusMode.TOKEN)
        }

        binding.editTextCurrencyAmount.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) onFocusChanged(FocusMode.CURRENCY)
        }
    }

    private fun handleAmountTextChanged(
        editText: EditText,
        textViewHelper: TextView,
        text: CharSequence?,
        originalTextSize: Float
    ) {
        textViewHelper.setText(text, TextView.BufferType.EDITABLE)
        editText.post {
            val textSize = if (text.isNullOrBlank()) originalTextSize else textViewHelper.textSize
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        }
    }
}
