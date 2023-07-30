package org.p2p.uikit.components.edittext.v2

import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.widget.EditText
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.emptyString
import org.p2p.uikit.databinding.WidgetUiKitEditTextNewBinding
import org.p2p.uikit.utils.SimpleTagTextWatcher

/**
 * Allows to change state of the view by settings text / listeners and other stuff
 */
class NewUiKitEditTextMutator(
    private val binding: WidgetUiKitEditTextNewBinding,
) {

    private var currentViewTag: Any? = null

    private var endDrawableStrategy: NewUiKitEditTextDrawableStrategy = NewUiKitEditTextDrawableStrategy.NONE

    private val tipStateRenderer = NewUiKitEditTextTipStateRenderer(binding)

    init {
        binding.editTextField.addTextChangedListener {
            if (endDrawableStrategy == NewUiKitEditTextDrawableStrategy.SHOW_ON_TEXT) {
                setEndDrawableIsVisible(!it.isNullOrEmpty())
            }
        }
    }

    fun setViewTag(tag: Any?) {
        currentViewTag = tag
    }

    fun setEnabled(isEnable: Boolean): NewUiKitEditTextMutator = apply {
        with(binding.editTextField) {
            binding.root.isEnabled = isEnable
            this.isEnabled = isEnabled
            isFocusable = isEnable
            isFocusableInTouchMode = isEnable
        }
    }

    fun setDigits(digits: String): NewUiKitEditTextMutator = apply {
        binding.editTextField.keyListener = DigitsKeyListener.getInstance(digits)
    }

    fun addOnTextChangedListener(block: (Editable) -> Unit): NewUiKitEditTextMutator = apply {
        val tag = currentViewTag ?: return this
        val editTextWatcher = object : SimpleTagTextWatcher(tag) {
            override fun afterTextChanged(tag: Any, text: Editable) {
                if (currentViewTag == tag) {
                    block(text)
                }
            }
        }
        binding.editTextField.addTextChangedListener(editTextWatcher)
    }

    fun addTextWatcher(textWatcher: (EditText) -> TextWatcher): NewUiKitEditTextMutator = apply {
        binding.editTextField.addTextChangedListener(textWatcher.invoke(binding.editTextField))
    }

    fun setOnClickListener(onClickListener: () -> Unit) {
        binding.root.setOnClickListener { onClickListener.invoke() }
    }

    /**
     * @param errorMessage pass null if you need to hide error
     */
    fun setErrorTip(errorMessage: TextContainer?): NewUiKitEditTextMutator = apply {
        tipStateRenderer.renderState(
            if (errorMessage != null) {
                NewUiKitEditTextTipState.Error(errorMessage)
            } else {
                NewUiKitEditTextTipState.NoTip
            }
        )
    }


    fun setInfoTip(tipMessage: TextContainer?): NewUiKitEditTextMutator = apply {
        tipStateRenderer.renderState(
            if (tipMessage != null) {
                NewUiKitEditTextTipState.Information(tipMessage)
            } else {
                NewUiKitEditTextTipState.NoTip
            }
        )
    }

    fun setSuccessTip(successMessage: TextContainer?): NewUiKitEditTextMutator = apply {
        tipStateRenderer.renderState(
            if (successMessage != null) {
                NewUiKitEditTextTipState.Success(successMessage)
            } else {
                NewUiKitEditTextTipState.NoTip
            }
        )
    }

    fun setInputText(text: String): NewUiKitEditTextMutator = apply {
        with(binding) {
            root.tag = null
            binding.editTextField.setText(text)
            root.tag = currentViewTag
        }
    }

    fun setHint(hint: CharSequence) {
        binding.editTextField.hint = hint
    }

    fun setEndDrawableIsVisible(isVisible: Boolean): NewUiKitEditTextMutator = apply {
        binding.imageViewIconEnd.isVisible = isVisible
    }

    fun setDrawable(@DrawableRes drawableRes: Int): NewUiKitEditTextMutator = apply {
        binding.imageViewIconEnd.setImageResource(drawableRes)
    }

    fun setDrawableStrategy(strategy: NewUiKitEditTextDrawableStrategy): NewUiKitEditTextMutator = apply {
        this.endDrawableStrategy = strategy
    }

    fun setDrawableClickListener(listener: NewUiKitEditTextMutator.() -> Unit): NewUiKitEditTextMutator = apply {
        binding.imageViewIconEnd.setOnClickListener { listener.invoke(this) }
    }

    fun clearInput() {
        binding.editTextField.setText(emptyString())
    }
}
