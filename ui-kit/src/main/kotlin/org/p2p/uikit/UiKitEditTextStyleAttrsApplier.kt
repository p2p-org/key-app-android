package org.p2p.uikit

import androidx.core.content.res.getDrawableOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.view.isVisible
import android.content.res.TypedArray
import android.text.method.DigitsKeyListener
import org.p2p.uikit.databinding.WidgetUiKitEdittextBinding

internal class UiKitEditTextStyleAttrsApplier {
    fun apply(attrs: TypedArray, binding: WidgetUiKitEdittextBinding) = with(binding) {
        val labelText = attrs.getString(R.styleable.UiKitEditText_labelText).orEmpty()
        if (labelText.isNotEmpty()) {
            textViewLabel.text = labelText
            textViewLabel.isVisible = true
        }
        val hintText = attrs.getString(R.styleable.UiKitEditText_hintText).orEmpty()
        if (hintText.isNotEmpty()) {
            editTextField.hint = hintText
        }
        val textAppearance = attrs.getResourceId(R.styleable.UiKitEditText_android_textAppearance, -1)
        if (textAppearance != -1) {
            editTextField.setTextAppearance(textAppearance)
        }

        val text = attrs.getText(R.styleable.UiKitEditText_android_text)
        if (!text.isNullOrEmpty()) {
            editTextField.setText(text)
        }

        root.isEnabled = attrs.getBoolean(R.styleable.UiKitEditText_android_enabled, true)

        if (attrs.hasValue(R.styleable.UiKitEditText_android_drawableEnd)) {
            val iconEnd = attrs.getDrawableOrThrow(R.styleable.UiKitEditText_android_drawableEnd)
            imageViewIconEnd.isVisible = true
            imageViewIconEnd.setImageDrawable(iconEnd)
        }

        if (attrs.hasValue(R.styleable.UiKitEditText_android_inputType)) {
            val inputType = attrs.getIntOrThrow(R.styleable.UiKitEditText_android_inputType)
            editTextField.inputType = inputType
        }
        if (attrs.hasValue(R.styleable.UiKitEditText_android_digits)) {
            val digits = attrs.getString(R.styleable.UiKitEditText_android_digits)
            digits?.also {
                editTextField.keyListener = DigitsKeyListener.getInstance(digits)
            }
        }
        if (attrs.hasValue(R.styleable.UiKitEditText_android_nextFocusDown)) {
            val nextFocusDown = attrs.getResourceIdOrThrow(R.styleable.UiKitEditText_android_nextFocusDown)
            editTextField.nextFocusDownId = nextFocusDown
        }
        if (attrs.hasValue(R.styleable.UiKitEditText_android_imeOptions)) {
            val imeOptions = attrs.getIntOrThrow(R.styleable.UiKitEditText_android_imeOptions)
            editTextField.imeOptions = imeOptions
        }
    }
}
