package org.p2p.uikit.components

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetDoubleInputViewBinding
import org.p2p.uikit.utils.emptyString
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.vibrate
import java.math.BigDecimal
import java.math.RoundingMode

private const val DECIMAL_SCALE_LONG = 9

class UiKitDoubleInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: WidgetDoubleInputViewBinding = inflateViewBinding()

    private var textWatcherInput: TextWatcher? = null
    private var textWatcherOutput: TextWatcher? = null

    private var originalTextSize: Float

    private var maxAmountOutputLabelText: String = emptyString()
    private var defaultOutputLabelText: String = emptyString()
    private var maxInputAmount: BigDecimal = BigDecimal.ZERO

    var amountsHandler: ((input: BigDecimal, output: BigDecimal) -> Unit)? = null

    init {
        with(binding) {
            /*
             * We need to remove text listeners if user focused and started entering some number for output or vice-versa
             * */
            editTextInput.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    editTextOutput.removeTextChangedListener(textWatcherOutput)
                    editTextInput.addTextChangedListener(textWatcherInput)
                }
            }

            editTextOutput.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    editTextInput.removeTextChangedListener(textWatcherInput)
                    editTextOutput.addTextChangedListener(textWatcherOutput)
                }
            }

            textViewInputToken.setOnClickListener { editTextInput.focusAndShowKeyboard() }
            textViewOutputToken.setOnClickListener { editTextOutput.focusAndShowKeyboard() }

            originalTextSize = editTextInput.textSize
        }
    }

    fun setInputLabelText(text: String) {
        binding.textViewInputLabel.text = text
    }

    fun setInputLabelText(@StringRes textRes: Int) {
        binding.textViewInputLabel.setText(textRes)
    }

    fun setOutputLabelText(text: String) {
        binding.textViewOutputLabel.text = text
    }

    fun setOutputLabelText(
        text: String,
        amount: BigDecimal,
        textMaxAmount: String
    ) {
        defaultOutputLabelText = text
        maxAmountOutputLabelText = textMaxAmount
        maxInputAmount = amount
        with(binding) {
            textViewOutputLabel.text = text
            textViewOutputLabel.setOnClickListener {
                if (textViewOutputLabel.text != maxAmountOutputLabelText) {
                    textViewOutputLabel.text = maxAmountOutputLabelText
                    updateInputText(
                        outputText = amount.scaleLong(),
                        removeListener = false
                    )
                    context.vibrate()
                }
            }
        }
    }

    fun acceptMaxAmount() {
        binding.textViewOutputLabel.callOnClick()
    }

    fun setOutputLabelText(@StringRes textRes: Int) {
        binding.textViewOutputLabel.setText(textRes)
    }

    fun setInputData(
        inputSymbol: String,
        outputSymbol: String,
        inputRate: Double
    ) = with(binding) {
        textViewInputToken.hint = inputSymbol
        textViewOutputToken.hint = outputSymbol

        clearTextWatchers()

        textWatcherInput = editTextInput.doOnTextChanged { text, _, _, _ ->
            resizeInput(text, inputSymbol)

            val inputValue = text?.toString()?.toDoubleOrNull() ?: 0.0
            calculateInput(inputValue, inputRate)
        }

        textWatcherOutput = editTextOutput.doOnTextChanged { text, _, _, _ ->
            resizeOutput(text, inputSymbol)

            val outputValue = text?.toString()?.toDoubleOrNull() ?: 0.0
            val outputRate = 1 / inputRate
            calculateOutput(outputValue, outputRate)
        }
    }

    private fun clearTextWatchers() = with(binding) {
        textWatcherInput?.let {
            editTextInput.removeTextChangedListener(it)
        }

        textWatcherOutput?.let {
            editTextOutput.removeTextChangedListener(it)
        }
    }

    fun setBottomMessageText(text: String) {
        binding.textViewBottomMessage.text = text
    }

    fun setBottomMessageText(@StringRes textRes: Int) {
        binding.textViewBottomMessage.setText(textRes)
    }

    /*
    * todo: maybe move math to presenter? workaround
    * */
    private fun WidgetDoubleInputViewBinding.calculateInput(inputValue: Double, inputRate: Double) {
        if (inputValue == 0.0) {
            val zero = inputValue.toBigDecimal()
            updateOutputText(emptyString())
            checkInputMaxValue(zero)
            amountsHandler?.invoke(zero, zero)
            return
        }

        val outputValue = BigDecimal(inputValue * inputRate)
        val output = outputValue.scaleLong()
        updateOutputText(output)
        val inputBigDecimal = inputValue.toBigDecimal()
        amountsHandler?.invoke(inputBigDecimal, outputValue)
        checkInputMaxValue(inputBigDecimal)
    }

    private fun WidgetDoubleInputViewBinding.calculateOutput(outputValue: Double, outputRate: Double) {
        val outputBigDecimal = outputValue.toBigDecimal()
        if (outputRate == 0.0) return

        if (outputValue == 0.0) {
            updateInputText(emptyString())
            amountsHandler?.invoke(outputBigDecimal, outputBigDecimal)
            checkInputMaxValue(outputBigDecimal)
            return
        }

        val inputValue = BigDecimal(outputValue * outputRate)
        val input = inputValue.scaleLong()
        updateInputText(input)
        amountsHandler?.invoke(inputValue, outputBigDecimal)
        checkInputMaxValue(inputValue)
    }

    private fun WidgetDoubleInputViewBinding.checkInputMaxValue(outputValue: BigDecimal) {
        textViewOutputLabel.apply {
            if (outputValue == maxInputAmount) {
                text = maxAmountOutputLabelText
                setTextColor(getColor(R.color.text_night))
            } else {
                text = defaultOutputLabelText
                setTextColor(getColor(R.color.text_sky))
            }
        }
        setInputRelatedTextColors(outputValue > maxInputAmount)
    }

    private fun WidgetDoubleInputViewBinding.setInputRelatedTextColors(isBiggerThenMax: Boolean) {
        val inputCheckedTextColor = getColor(
            if (isBiggerThenMax) {
                R.color.text_rose
            } else {
                R.color.text_night
            }
        )
        editTextInput.setTextColor(inputCheckedTextColor)
        editTextOutput.setTextColor(inputCheckedTextColor)
        textViewBottomMessage.setTextColor(
            getColor(
                if (isBiggerThenMax) {
                    R.color.text_rose
                } else {
                    R.color.text_mountain
                }
            )
        )
    }

    private fun WidgetDoubleInputViewBinding.resizeInput(
        text: CharSequence?,
        inputSymbol: String
    ) {
        textViewAutoSizeInput.setText(text.appendSymbol(inputSymbol), TextView.BufferType.EDITABLE)

        val textSize = if (text.isNullOrBlank()) originalTextSize else textViewAutoSizeInput.textSize
        editTextInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        textViewInputToken.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    private fun WidgetDoubleInputViewBinding.resizeOutput(
        text: CharSequence?,
        inputSymbol: String
    ) {
        textViewAutoSizeOutput.setText(text.appendSymbol(inputSymbol), TextView.BufferType.EDITABLE)

        val textSize = if (text.isNullOrBlank()) originalTextSize else textViewAutoSizeOutput.textSize
        editTextOutput.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        textViewOutputToken.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    private fun WidgetDoubleInputViewBinding.updateInputText(outputText: String, removeListener: Boolean = true) {
        if (removeListener) editTextInput.removeTextChangedListener(textWatcherInput)

        editTextInput.setText(outputText)
        resizeInput(outputText, textViewInputToken.hint.toString())

        if (removeListener) editTextInput.addTextChangedListener(textWatcherInput)
    }

    private fun WidgetDoubleInputViewBinding.updateOutputText(outputText: String, removeListener: Boolean = true) {
        if (removeListener) editTextOutput.removeTextChangedListener(textWatcherOutput)

        editTextOutput.setText(outputText)
        resizeOutput(outputText, textViewOutputToken.hint.toString())

        if (removeListener) editTextOutput.addTextChangedListener(textWatcherOutput)
    }

    private fun CharSequence?.appendSymbol(symbol: String) = "${this?.toString().orEmpty()} $symbol"

    private fun BigDecimal.scaleLong(): String =
        setScale(DECIMAL_SCALE_LONG, RoundingMode.HALF_EVEN)
            .stripTrailingZeros()
            .toPlainString()
}
