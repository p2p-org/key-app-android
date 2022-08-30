package org.p2p.wallet.auth.widget

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.KeyEvent
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.ui.phone.maskwatcher.CountryCodeTextWatcher
import org.p2p.wallet.auth.ui.phone.maskwatcher.PhoneNumberTextWatcher
import org.p2p.wallet.databinding.WidgetPhoneInputViewBinding
import org.p2p.wallet.utils.orZero
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val EMOJI_NO_FLAG = "️\uD83C\uDFF4"
private const val CORNER_RADIUS = 20f
private const val STROKE_WIDTH = 1
private const val PLUS_SIGN = '+'

open class PhoneNumberInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    protected val binding = inflateViewBinding<WidgetPhoneInputViewBinding>()

    private lateinit var phoneTextWatcher: PhoneNumberTextWatcher
    private lateinit var countryCodeWatcher: CountryCodeTextWatcher

    private val bgRed = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.bg_rain))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rose))
    }

    private val bgNormal = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.rain))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rain))
    }

    init {
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.UiKitTextField, 0, 0)
        val labelText = styleAttrs.getString(R.styleable.UiKitTextField_labelText).orEmpty()
        if (labelText.isNotEmpty()) {
            binding.textViewLabel.text = labelText
            binding.textViewLabel.isVisible = true
        }
        val hintText = styleAttrs.getString(R.styleable.UiKitTextField_hintText).orEmpty()
        if (hintText.isNotEmpty()) {
            binding.textViewHint.text = hintText
            binding.textViewHint.isVisible = true
        }
        val textAppearance = styleAttrs.getResourceId(R.styleable.UiKitTextField_android_textAppearance, -1)
        if (textAppearance != -1) {
            binding.editTextPhoneNumber.setTextAppearance(textAppearance)
        }
        val text = styleAttrs.getText(R.styleable.UiKitTextField_android_text)
        if (!text.isNullOrEmpty()) {
            binding.editTextPhoneNumber.setText(text)
        }
        binding.inputViewContainer.background = bgNormal
        styleAttrs.recycle()
    }

    fun setText(text: String) {
        binding.editTextPhoneNumber.setText(text)
    }

    fun setHint(hint: String) {
        binding.editTextPhoneNumber.hint = hint
    }

    val text: Editable?
        get() = binding.editTextPhoneNumber.text

    val stringText: String?
        get() = text?.toString()

    val hint: CharSequence
        get() = binding.editTextPhoneNumber.hint

    val length: Int
        get() = binding.editTextPhoneNumber.length()

    fun setupViewState(
        countryCode: CountryCode?,
        onCountryCodeChanged: (String) -> Unit,
        onPhoneChanged: (String) -> Unit,
        onCountryClickListener: () -> Unit
    ) = with(binding) {

        countryCode?.phoneCode.let { editTextCountryCode.setText(it) }

        val flagEmoji = countryCode?.flagEmoji ?: EMOJI_NO_FLAG
        textViewFlagEmoji.text = flagEmoji

        val restoredNumber = editTextPhoneNumber.text.toString().orEmpty()
        val hint = restoredNumber.ifEmpty { countryCode?.getMaskWithoutCountryCode().orEmpty() }
        editTextPhoneNumber.setHintText(hint)
        numberHintTextView.setHintText(hint)

        countryPickerView.setOnClickListener {
            onCountryClickListener.invoke()
        }

        val originalTextSize = editTextPhoneNumber.textSize
        editTextPhoneNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize)

        phoneTextWatcher = PhoneNumberTextWatcher(binding.editTextPhoneNumber) { phoneNumber ->
            resizeInputs(phoneNumber, originalTextSize)

            numberHintTextView.updateNumber(phoneNumber)

            // Invoking callbacks
            val phone = phoneNumber.getFullPhoneNumber()
            onPhoneChanged.invoke(phone)
        }
        countryCodeWatcher = CountryCodeTextWatcher { countryCode ->
            onCountryCodeChanged.invoke(countryCode)
        }
        editTextPhoneNumber.addTextChangedListener(phoneTextWatcher)
        editTextPhoneNumber.onEmptyDelete = { moveCursorToCodeField() }

        editTextCountryCode.addTextChangedListener(countryCodeWatcher)

        val focusView = if (countryCode == null) editTextCountryCode else editTextPhoneNumber
        focusView.focusAndShowKeyboard()

        editTextPhoneNumber.setSelection(editTextPhoneNumber.text?.length.orZero())

        if (restoredNumber.isNotEmpty()) onPhoneChanged(restoredNumber.getFullPhoneNumber())
    }

    private fun WidgetPhoneInputViewBinding.resizeInputs(
        phoneNumber: String,
        originalTextSize: Float
    ) {
        // Use invisible auto size textView to handle editText text size
        autoSizeHelperTextView.setText(phoneNumber, TextView.BufferType.EDITABLE)
        val textSize = if (phoneNumber.isEmpty()) originalTextSize else autoSizeHelperTextView.textSize
        editTextPhoneNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        editTextCountryCode.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        numberHintTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        textViewPlusSign.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    private fun moveCursorToCodeField() = with(binding) {
        val currentText = editTextPhoneNumber.text.toString()
        if (currentText.isNotEmpty()) return@with

        with(editTextPhoneNumber) {
            editTextPhoneNumber.setSelection(editTextPhoneNumber.length())
            setHintText("")
        }

        with(editTextCountryCode) {
            setSelection(binding.editTextCountryCode.length())
            dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            focusAndShowKeyboard()
        }
    }

    fun updateViewState(countryCode: CountryCode?) = with(binding) {

        if (countryCode == null) {
            showError(getString(R.string.error_country_not_found))
            textViewFlagEmoji.text = EMOJI_NO_FLAG
            return@with
        }
        textViewFlagEmoji.text = countryCode.flagEmoji

        with(editTextCountryCode) {
            removeTextChangedListener(countryCodeWatcher)
            setText(countryCode.phoneCode)
        }
        numberHintTextView.setHintText(countryCode.getMaskWithoutCountryCode())

        with(editTextPhoneNumber) {
            addTextChangedListener(phoneTextWatcher)
            setHintText(countryCode.getMaskWithoutCountryCode())
            setSelection(length())
            focusAndShowKeyboard()
        }

        editTextCountryCode.addTextChangedListener(countryCodeWatcher)
        showError(null)
    }

    fun showError(text: String?) = with(binding) {
        textViewError.text = text
        textViewError.isVisible = !text.isNullOrEmpty()
        inputViewContainer.background = if (!text.isNullOrEmpty()) bgRed else bgNormal
    }

    fun onFoundNewCountry(countryCode: CountryCode) = with(binding) {
        // TODO implement if need find country outside the mask
    }

    private fun String.getFullPhoneNumber(): String {
        return PLUS_SIGN + binding.editTextCountryCode.text.toString().trim() + this.replace(" ", "")
    }
}
