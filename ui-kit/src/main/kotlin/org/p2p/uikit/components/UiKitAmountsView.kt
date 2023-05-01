package org.p2p.uikit.components

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.EditText
import android.widget.TextView
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetAmountsViewBinding
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.showSoftKeyboard

enum class FocusField {
    TOKEN,
    CURRENCY
}

private const val MAX_CURRENCY_FRACTION_LENGTH = 2

class UiKitAmountsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val focusField: FocusField
        get() = if (binding.editTextTokenAmount.hasFocus()) {
            FocusField.TOKEN
        } else {
            FocusField.CURRENCY
        }

    private val binding = inflateViewBinding<WidgetAmountsViewBinding>()

    private lateinit var tokenTextWatcher: TextWatcher
    private lateinit var currencyTextWatcher: TextWatcher

    var tokenSymbol: String? = null
        set(value) {
            field = value
            binding.textViewToken.text = value
        }

    var currencyCode: String? = null
        set(value) {
            field = value
            binding.textViewCurrency.text = value
        }

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_amounts_view)

        with(binding) {
            val originalTextSize = editTextTokenAmount.textSize
            AmountFractionTextWatcher.installOn(editTextTokenAmount, maxDecimalsAllowed = 2) { amount ->
                handleAmountTextChanged(editTextTokenAmount, textViewTokenAutoSizeHelper, amount, originalTextSize)
            }
            editTextCurrencyAmount.doOnTextChanged { text, _, _, _ ->
                handleAmountTextChanged(editTextCurrencyAmount, textViewCurrencyAutoSizeHelper, text, originalTextSize)
            }
        }
    }

    fun setTokenAmount(tokenAmount: String?) = with(binding.editTextTokenAmount) {
        removeTextChangedListener(tokenTextWatcher)
        setText(tokenAmount)
        addTextChangedListener(tokenTextWatcher)
    }

    fun setCurrencyAmount(currencyAmount: String?) = with(binding.editTextCurrencyAmount) {
        removeTextChangedListener(currencyTextWatcher)
        setText(currencyAmount)
        addTextChangedListener(currencyTextWatcher)
    }

    fun setOnTokenAmountChangeListener(onTokenAmountChange: (String) -> Unit) {
        tokenTextWatcher = AmountFractionTextWatcher.installOn(binding.editTextTokenAmount) {
            val amountWithoutSpaces = it.replace(" ", "")
            onTokenAmountChange(amountWithoutSpaces)
        }
    }

    fun setOnCurrencyAmountChangeListener(onCurrencyAmountChange: (String) -> Unit) {
        currencyTextWatcher = AmountFractionTextWatcher.installOn(
            binding.editTextCurrencyAmount,
            maxDecimalsAllowed = MAX_CURRENCY_FRACTION_LENGTH
        ) {
            val amountWithoutSpaces = it.replace(" ", "")
            onCurrencyAmountChange(amountWithoutSpaces)
        }
    }

    fun setOnSelectTokenClickListener(onSelectTokenClick: () -> Unit) {
        binding.textViewToken.setOnClickListener { onSelectTokenClick() }
        binding.imageViewSelectToken.setOnClickListener { onSelectTokenClick() }
    }

    fun setOnSelectCurrencyClickListener(onSelectCurrencyClick: () -> Unit) {
        binding.textViewCurrency.setOnClickListener { onSelectCurrencyClick() }
        binding.imageViewSelectCurrency.setOnClickListener { onSelectCurrencyClick() }
    }

    fun setOnFocusChangeListener(onFocusChanged: (focusField: FocusField) -> Unit) {
        binding.editTextTokenAmount.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) onFocusChanged(FocusField.TOKEN)
        }

        binding.editTextCurrencyAmount.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) onFocusChanged(FocusField.CURRENCY)
        }
    }

    fun requestFocus(focusField: FocusField) {
        when (focusField) {
            FocusField.CURRENCY -> {
                binding.editTextCurrencyAmount.apply {
                    requestFocus()
                    showSoftKeyboard()
                    setSelection(text?.length ?: 0)
                }
            }
            FocusField.TOKEN -> {
                binding.editTextTokenAmount.apply {
                    requestFocus()
                    showSoftKeyboard()
                    setSelection(text?.length ?: 0)
                }
            }
        }
    }

    fun setCurrencySelectIconVisible(enabled: Boolean) {
        binding.imageViewSelectCurrency.isVisible = enabled
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
