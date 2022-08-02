package org.p2p.wallet.auth.ui.phone.maskwatcher

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.databinding.WidgetPhoneInputTextFieldBinding
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private val EMOJI_NO_FLAG = "️\uD83C\uDFF4"

open class PhoneNumberTextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    protected val binding = inflateViewBinding<WidgetPhoneInputTextFieldBinding>()

    private val hintText: String? = null
    private var textOffset = 0f
    private var spaceSize = 0f
    private var numberSize = 0f
    private val paint = Paint()
    private val rect = Rect()

    private lateinit var bg: GradientDrawable
    private lateinit var bgDisabled: GradientDrawable
    private lateinit var bgFocused: GradientDrawable
    private val bgRed = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 20f
        setColor(context.getColor(R.color.bg_rain))
        setStroke(1, context.getColor(R.color.bg_rose))
    }

    private val bgGreen = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 20f
        setColor(context.getColor(R.color.bg_rain))
        setStroke(1, context.getColor(R.color.bg_mint))
    }

    private val bgNormal = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 20f
        setColor(context.getColor(R.color.rain))
        setStroke(1, context.getColor(R.color.bg_rain))
    }

    init {
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.UiKitTextField, 0, 0)
        val labelText = styleAttrs.getString(R.styleable.UiKitTextField_labelText).orEmpty()
        if (labelText.isNotEmpty()) {
            binding.labelTextView.text = labelText
            binding.labelTextView.isVisible = true
        }
        val hintText = styleAttrs.getString(R.styleable.UiKitTextField_hintText).orEmpty()
        if (hintText.isNotEmpty()) {
            binding.holderTextView.text = hintText
            binding.holderTextView.isVisible = true
        }
        val textAppearance = styleAttrs.getResourceId(R.styleable.UiKitTextField_android_textAppearance, -1)
        if (textAppearance != -1) {
            binding.uikitEditText.setTextAppearance(textAppearance)
        }
        val text = styleAttrs.getText(R.styleable.UiKitTextField_android_text)
        if (!text.isNullOrEmpty()) {
            binding.uikitEditText.setText(text)
        }
        binding.inputViewContainer.background = bgNormal
        styleAttrs.recycle()
    }

    fun setText(text: String) {
        binding.uikitEditText.setText(text)
    }

    fun setHint(hint: String) {
        binding.uikitEditText.hint = hint
    }

    fun getText() = binding.uikitEditText.text

    fun getHint() = binding.uikitEditText.hint

    fun length() = binding.uikitEditText.length()

    private lateinit var phoneTextWatcher: PhoneTextWatcher
    private lateinit var countryCodeWatcher: CountryCodeWatcher

    fun setup(
        countryCode: CountryCode?,
        onCountryCodeChanged: (String) -> Unit,
        onPhoneChanged: (String) -> Unit,
        onCountryClickListener: () -> Unit
    ) = with(binding) {

        countryCode?.phoneCode.let { codeEditText.setText(it) }

        val flagEmoji = countryCode?.flagEmoji ?: EMOJI_NO_FLAG
        emojiTextView.text = flagEmoji

        val hint = countryCode?.getMaskWithoutCountryCode().orEmpty()
        uikitEditText.setHintText(hint)

        countryPickerView.setOnClickListener {
            onCountryClickListener.invoke()
        }

        phoneTextWatcher = PhoneTextWatcher(binding.uikitEditText) {
            val phone = "+${codeEditText.text?.trim()}${it.trim()}"
            onPhoneChanged.invoke(phone)
        }
        countryCodeWatcher = CountryCodeWatcher { countryCode ->
            onCountryCodeChanged.invoke(countryCode)
        }
        uikitEditText.addTextChangedListener(phoneTextWatcher)
        uikitEditText.onEmptyDelete = { moveCursorToCodeField() }

        codeEditText.addTextChangedListener(countryCodeWatcher)

        val focusView = if (countryCode == null) codeEditText else uikitEditText
        focusView.focusAndShowKeyboard()
    }

    private fun moveCursorToCodeField() = with(binding) {
        val currentText = uikitEditText.text.toString()
        if (currentText.isNotEmpty()) return@with

        with(uikitEditText) {
            uikitEditText.setSelection(uikitEditText.length())
            setHintText("")
        }

        with(codeEditText) {
            setSelection(binding.codeEditText.length())
            dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            focusAndShowKeyboard()
        }
    }

    fun update(countryCode: CountryCode) = with(binding) {
        emojiTextView.text = countryCode.flagEmoji

        with(codeEditText) {
            removeTextChangedListener(countryCodeWatcher)
            setText(countryCode.phoneCode)
        }

        with(uikitEditText) {
            addTextChangedListener(phoneTextWatcher)
            setHintText(countryCode.getMaskWithoutCountryCode())
            setSelection(length())
            focusAndShowKeyboard()
        }

        codeEditText.addTextChangedListener(countryCodeWatcher)
        showError(null)
    }

    fun showNoCountry() = with(binding) {
        emojiTextView.text = EMOJI_NO_FLAG
        showError(getString(R.string.error_country_not_found))
    }

    fun showError(text: String?) = with(binding) {
        errorTextView.text = text
        errorTextView.isVisible = !text.isNullOrEmpty()
        inputViewContainer.background = if (!text.isNullOrEmpty()) bgRed else bgNormal
    }

    fun onFoundNewCountry(countryCode: CountryCode) = with(binding) {
        // TODO implement if need find country outside the mask
    }
}
