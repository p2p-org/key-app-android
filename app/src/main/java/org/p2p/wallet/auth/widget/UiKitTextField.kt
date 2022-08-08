package org.p2p.wallet.auth.widget

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import org.p2p.uikit.utils.dip
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetUikitTextFieldBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

open class UiKitTextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    protected val binding = inflateViewBinding<WidgetUikitTextFieldBinding>()

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
        cornerRadius = 12f
        setStroke(dip(1), context.getColor(R.color.bg_rose))
    }

    private val bgGreen = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 12f
        setStroke(dip(1), context.getColor(R.color.bg_mint))
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
            binding.uikitEditText.setTextAppearance(textAppearance)
        }
        val text = styleAttrs.getText(R.styleable.UiKitTextField_android_text)
        if (!text.isNullOrEmpty()) {
            binding.uikitEditText.setText(text)
        }
        val inputType = styleAttrs.getInt(R.styleable.UiKitTextField_android_inputType, -1)
        if (inputType != -1) {
            binding.uikitEditText.inputType = inputType
        }
        styleAttrs.recycle()
    }

    fun setText(text: String) {
        binding.uikitEditText.setText(text)
    }

    fun setHint(hint: String) {
        binding.uikitEditText.hint = hint
    }

    fun getText(): Editable? = binding.uikitEditText.text

    fun getHint(): CharSequence? = binding.uikitEditText.hint

    fun length(): Int = binding.uikitEditText.length()
}
