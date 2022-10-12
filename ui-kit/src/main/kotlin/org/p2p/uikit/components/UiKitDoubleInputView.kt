package org.p2p.uikit.components

import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import org.p2p.uikit.databinding.WidgetDoubleInputViewBinding
import org.p2p.uikit.utils.emptyString
import org.p2p.uikit.utils.inflateViewBinding
import java.math.BigDecimal

class UiKitDoubleInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: WidgetDoubleInputViewBinding = inflateViewBinding()

    private var textWatcherInput: TextWatcher? = null
    private var textWatcherOutput: TextWatcher? = null

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

            textViewInputToken.setOnClickListener { editTextInput.requestFocus() }
            textViewOutputToken.setOnClickListener { editTextOutput.requestFocus() }
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

    fun setOutputLabelText(text: String, amount: BigDecimal) {
        binding.textViewOutputLabel.text = text
        binding.textViewOutputLabel.setOnClickListener {
            binding.updateInputText(amount.toString(), removeListener = false)
        }
    }

    fun setOutputLabelText(@StringRes textRes: Int) {
        binding.textViewOutputLabel.setText(textRes)
    }

    fun setInputData(
        inputSymbol: String,
        outputSymbol: String,
        inputRate: Float
    ) = with(binding) {
        textViewInputToken.hint = inputSymbol
        textViewOutputToken.hint = outputSymbol

        val originalInputTextSize = editTextInput.textSize
        textWatcherInput = editTextInput.doOnTextChanged { text, _, _, _ ->
            resizeInput(text, inputSymbol, originalInputTextSize)

            val inputValue = text?.toString()?.toFloatOrNull() ?: 0f
            calculateInput(inputValue, inputRate)
        }

        val originalOutputTextSize = editTextOutput.textSize
        textWatcherOutput = editTextOutput.doOnTextChanged { text, _, _, _ ->
            resizeOutput(text, inputSymbol, originalOutputTextSize)

            val outputValue = text?.toString()?.toFloatOrNull() ?: 0f
            val outputRate = 1 / inputRate
            calculateOutput(outputValue, outputRate)
        }
    }

    fun setBottomMessageText(@StringRes textRes: Int) {
        binding.textViewBottomMessage.setText(textRes)
    }

    /*
    * todo: maybe move math to presenter? workaround
    * */
    private fun WidgetDoubleInputViewBinding.calculateInput(inputValue: Float, inputRate: Float) {
        if (inputValue == 0f) {
            updateOutputText(emptyString())
            return
        }

        val output = inputValue * inputRate
        updateOutputText(output.toString())
    }

    private fun WidgetDoubleInputViewBinding.calculateOutput(outputValue: Float, outputRate: Float) {
        if (outputRate == 0f) return

        if (outputValue == 0f) {
            updateInputText(emptyString())
            return
        }

        val output = outputValue * outputRate
        updateInputText(output.toString())
    }

    private fun WidgetDoubleInputViewBinding.resizeInput(
        text: CharSequence?,
        inputSymbol: String,
        originalInputTextSize: Float
    ) {
        textViewAutoSizeInput.text = text.appendSymbol(inputSymbol)

        val textSize = if (text.isNullOrBlank()) originalInputTextSize else textViewAutoSizeInput.textSize

        editTextInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        textViewInputToken.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    private fun WidgetDoubleInputViewBinding.resizeOutput(
        text: CharSequence?,
        inputSymbol: String,
        originalOutputTextSize: Float
    ) {
        textViewAutoSizeOutput.text = text.appendSymbol(inputSymbol)

        val textSize = if (text.isNullOrBlank()) originalOutputTextSize else textViewAutoSizeOutput.textSize

        editTextOutput.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        textViewOutputToken.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    private fun WidgetDoubleInputViewBinding.updateInputText(outputText: String, removeListener: Boolean = true) {
        if (removeListener) editTextInput.removeTextChangedListener(textWatcherInput)
        editTextInput.setText(outputText)
        if (removeListener) editTextInput.addTextChangedListener(textWatcherInput)
    }

    private fun WidgetDoubleInputViewBinding.updateOutputText(outputText: String, removeListener: Boolean = true) {
        if (removeListener) editTextOutput.removeTextChangedListener(textWatcherOutput)
        editTextOutput.setText(outputText)
        if (removeListener) editTextOutput.addTextChangedListener(textWatcherOutput)
    }

    private fun CharSequence?.appendSymbol(symbol: String) = "${this?.toString().orEmpty()} $symbol"
}
