package org.p2p.uikit.components.edittext.v2

import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.getStringOrThrow
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.text.method.DigitsKeyListener
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetUiKitEditTextNewBinding
import org.p2p.uikit.utils.withImageOrGone
import org.p2p.uikit.utils.withTextOrGone

internal class NewUiKitEditTextStyleAttrsApplier {
    private companion object {
        private val ATTR_LABEL_TEXT = R.styleable.NewUiKitEditText_labelText
        private val ATTR_HINT_TEXT = R.styleable.NewUiKitEditText_inputHintText
        private val ATTR_INPUT_TEXT = R.styleable.NewUiKitEditText_inputText
        private val ATTR_END_DRAWABLE = R.styleable.NewUiKitEditText_inputEndDrawable

        private val ATTR_INPUT_TEXT_COLOR = R.styleable.NewUiKitEditText_inputTextColor
        private val ATTR_INPUT_TEXT_STYLE = R.styleable.NewUiKitEditText_inputTextAppearance
        private val ATTR_INPUT_BACKGROUND_TINT = R.styleable.NewUiKitEditText_inputBackgroundTint

        private val ATTR_IS_INPUT_ENABLED = R.styleable.NewUiKitEditText_android_enabled
        private val ATTR_INPUT_TYPE = R.styleable.NewUiKitEditText_android_inputType
        private val ATTR_INPUT_DIGITS = R.styleable.NewUiKitEditText_android_digits
        private val ATTR_NEXT_FOCUS_DOWN = R.styleable.NewUiKitEditText_android_nextFocusDown
        private val ATTR_IME_OPTIONS = R.styleable.NewUiKitEditText_android_imeOptions
    }

    /**
     * Apply attrs values and styles
     * use xml-values if they are not set explicitly
     */
    fun applyToView(attrs: TypedArray, binding: WidgetUiKitEditTextNewBinding) = with(binding) {
        // input values
        attrs.getString(ATTR_LABEL_TEXT, ifExists = textViewLabel::withTextOrGone)
        attrs.getString(ATTR_HINT_TEXT, editTextField::setHint)
        attrs.getString(ATTR_INPUT_TEXT, editTextField::setText)
        attrs.getResourceId(ATTR_END_DRAWABLE, imageViewIconEnd::withImageOrGone)
        attrs.getString(ATTR_INPUT_DIGITS) { editTextField.keyListener = DigitsKeyListener.getInstance(it) }

        // styling
        attrs.getColor(ATTR_INPUT_TEXT_COLOR, editTextField::setTextColor)
        attrs.getResourceId(ATTR_INPUT_TEXT_STYLE, editTextField::setTextAppearance)
        attrs.getColorStateList(ATTR_INPUT_BACKGROUND_TINT, containerInputView::setBackgroundTintList)

        attrs.getBoolean(ATTR_IS_INPUT_ENABLED, true).also {
            root.isEnabled = it
            editTextField.isClickable = it
            editTextField.isFocusable = it
        }
        attrs.getIntId(ATTR_INPUT_TYPE, editTextField::setInputType)

        attrs.getResourceId(ATTR_NEXT_FOCUS_DOWN, editTextField::setNextFocusDownId)
        attrs.getIntId(ATTR_IME_OPTIONS, editTextField::setImeOptions)
    }

    private fun TypedArray.getResourceId(index: Int, ifExists: (Int) -> Unit) {
        if (hasValue(index)) ifExists.invoke(getResourceIdOrThrow(index))
    }

    private fun TypedArray.getIntId(index: Int, ifExists: (Int) -> Unit) {
        if (hasValue(index)) ifExists.invoke(getIntOrThrow(index))
    }

    private fun TypedArray.getString(index: Int, ifExists: (String) -> Unit) {
        if (hasValue(index)) ifExists.invoke(getStringOrThrow(index))
    }

    private fun TypedArray.getDrawable(index: Int, ifExists: (Drawable) -> Unit) {
        if (hasValue(index)) ifExists.invoke(getDrawableOrThrow(index))
    }

    private fun TypedArray.getColor(index: Int, ifExists: (Int) -> Unit) {
        if (hasValue(index)) ifExists.invoke(getColorOrThrow(index))
    }

    private fun TypedArray.getColorStateList(index: Int, ifExists: (ColorStateList) -> Unit) {
        if (hasValue(index)) ifExists.invoke(ColorStateList.valueOf(getColorOrThrow(index)))
    }
}
