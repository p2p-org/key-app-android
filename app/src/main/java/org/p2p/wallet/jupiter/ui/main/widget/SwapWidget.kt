package org.p2p.wallet.jupiter.ui.main.widget

import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import android.widget.TextView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.core.glide.GlideManager
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.core.utils.SOL_DECIMALS
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.utils.drawable.shape.shapeRounded16dp
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bindOrGone
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetSwapBinding

class SwapWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), KoinComponent {

    private val binding = inflateViewBinding<WidgetSwapBinding>()
    private val initInputType: Int
    private var internalOnAmountChanged: ((newAmount: String) -> Unit)? = null
    private var currentAmountCell: TextViewCellModel? = null
    private val glideManager: GlideManager by inject()

    var onAmountChanged: (newAmount: String) -> Unit = {}
    var onAllAmountClick: () -> Unit = {}
    var onChangeTokenClick: () -> Unit = {}
    var onInputClicked: () -> Unit = {}

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SwapWidget)

        minHeight = 120.toPx()
        background = shapeDrawable(shapeRounded16dp())
        backgroundTintList = backgroundTint()
        with(binding) {
            textViewWidgetTitle.setTextColor(widgetTitleTint())
            initInputType = editTextAmount.inputType
            editTextAmount.doAfterTextChanged { resizeInput(it) }
            viewEditTextClickable.setOnClickListener {
                if (isEnabled) editTextAmount.focusAndShowKeyboard(true)
            }
            editTextAmount.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
                val activity = (context as? Activity)
                if (activity != null && activity.currentFocus == null && !hasFocus) v.hideKeyboard()
            }

            editTextAmount.setOnClickListener { onInputClicked() }
            textViewShadowAutoSize.setOnClickListener { onInputClicked() }

            if (attributes.hasValue(R.styleable.SwapWidget_balanceVisible)) {
                textViewBalance.isVisible = attributes.getBoolean(R.styleable.SwapWidget_balanceVisible, true)
            }
            if (attributes.hasValue(R.styleable.SwapWidget_fiatAmountVisible)) {
                textViewFiatAmount.isVisible = attributes.getBoolean(R.styleable.SwapWidget_fiatAmountVisible, true)
            }
            if (attributes.hasValue(R.styleable.SwapWidget_enableChangeCurrency)) {
                textViewChangeCurrency.isVisible =
                    attributes.getBoolean(R.styleable.SwapWidget_enableChangeCurrency, true)
            }
        }

        attributes.recycle()
    }

    fun bind(model: SwapWidgetModel) {
        when (model) {
            is SwapWidgetModel.Content -> bindContent(model)
            is SwapWidgetModel.Loading -> bindLoading(model)
        }
    }

    fun focusAndShowKeyboard() {
        binding.editTextAmount.focusAndShowKeyboard(true)
    }

    fun setAmount(amount: String) {
        binding.editTextAmount.setText(amount)
    }

    fun setAmountTextColor(textColor: Int) {
        binding.editTextAmount.setTextColor(textColor)
    }

    fun setAmountTextColorRes(@ColorRes textColorRes: Int) {
        binding.editTextAmount.setTextColor(binding.getColor(textColorRes))
    }

    private fun bindLoading(model: SwapWidgetModel.Loading) = with(binding) {
        isEnabled = !model.isStatic
        textViewWidgetTitle.bindOrGone(model.widgetTitle)
        textViewAvailableAmountTitle.isVisible = false
        textViewAvailableAmountValue.isVisible = false
        textViewCurrencyName.bindOrGone(model.currencySkeleton)
        textViewCurrencyName.setOnClickListener(null)
        textViewChangeCurrency.setOnClickListener(null)

        bindLoadingInput(model.amountSkeleton)
        if (textViewBalance.isVisible) {
            textViewBalance.bindOrGone(model.balanceSkeleton)
        }
        if (textViewFiatAmount.isVisible) {
            textViewFiatAmount.isVisible = false
        }
    }

    private fun bindContent(model: SwapWidgetModel.Content) = with(binding) {
        isEnabled = !model.isStatic
        textViewWidgetTitle.bindOrGone(model.widgetTitle)
        textViewAvailableAmountTitle.isVisible = model.availableAmount != null
        textViewAvailableAmountValue.bindOrGone(model.availableAmount)

        textViewAvailableAmountTitle.setOnClickListener { onAllAmountClick() }
        textViewAvailableAmountValue.setOnClickListener { onAllAmountClick() }

        textViewCurrencyName.setOnClickListener { onChangeTokenClick() }
        textViewChangeCurrency.setOnClickListener { onChangeTokenClick() }
        textViewCurrencyName.bindOrGone(model.currencyName)

        glideManager.load(
            imageView = imageViewTokenIcon,
            url = model.tokenUrl,
            size = 32
        )

        if (model.amount is TextViewCellModel.Skeleton) {
            bindLoadingInput(model.amount)
        } else {
            bindInput(model, model.amount)
        }
        textViewBalance.bindOrGone(model.balance)
        textViewFiatAmount.bindOrGone(model.fiatAmount)
    }

    private fun bindInput(model: SwapWidgetModel.Content, newAmount: TextViewCellModel) = with(binding) {
        val readOnly = model.isStatic || newAmount is TextViewCellModel.Skeleton
        val inputType = if (readOnly) InputType.TYPE_NULL else initInputType
        editTextAmount.setReadOnly(readOnly, inputType)
        val amountMaxDecimals = model.amountMaxDecimals ?: SOL_DECIMALS
        updateFormatter(amountMaxDecimals)
        val oldAmountRaw = editTextAmount.text?.toString() ?: emptyString()
        val newAmountRaw = (newAmount as? TextViewCellModel.Raw)?.text?.getString(context)
        val newTextColorRes = (newAmount as? TextViewCellModel.Raw)?.textColor
        val oldTextColorRes = (currentAmountCell as? TextViewCellModel.Raw)?.textColor
        internalOnAmountChanged = null
        if (currentAmountCell is TextViewCellModel.Skeleton ||
            checkAmountsAreNotEqualExcludingTrailingDot(newAmountRaw, oldAmountRaw) ||
            newTextColorRes != oldTextColorRes
        ) {
            editTextAmount.bindOrGone(newAmount, force = true)
            currentAmountCell = newAmount
            editTextAmount.setSelection(editTextAmount.text.length)
        }
        internalOnAmountChanged = { onAmountChanged(it) }
    }

    private fun bindLoadingInput(skeleton: TextViewCellModel.Skeleton) = with(binding) {
        val readOnly = true
        val inputType = InputType.TYPE_NULL
        AmountFractionTextWatcher.uninstallFrom(binding.editTextAmount)
        editTextAmount.setReadOnly(readOnly, inputType)
        internalOnAmountChanged = null
        editTextAmount.bindOrGone(skeleton, force = true)
        currentAmountCell = skeleton
    }

    private fun EditText.setReadOnly(readOnly: Boolean, inputType: Int = InputType.TYPE_NULL) {
        isFocusable = !readOnly
        isFocusableInTouchMode = !readOnly
        this.inputType = inputType
    }

    private fun updateFormatter(amountMaxDecimals: Int) {
        AmountFractionTextWatcher.uninstallFrom(binding.editTextAmount)
        AmountFractionTextWatcher.installOn(
            editText = binding.editTextAmount,
            maxDecimalsAllowed = amountMaxDecimals,
            maxIntLength = Int.MAX_VALUE,
            onValueChanged = { internalOnAmountChanged?.invoke(it) }
        )
    }

    private fun resizeInput(text: CharSequence?) = with(binding) {
        textViewShadowAutoSize.setText(text, TextView.BufferType.EDITABLE)
        val textSize = textViewShadowAutoSize.textSize
        editTextAmount.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    private fun backgroundTint(): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled),
        )
        val colors = intArrayOf(
            getColor(R.color.bg_snow),
            getColor(R.color.bg_snow_60),
        )
        return ColorStateList(states, colors)
    }

    private fun widgetTitleTint(): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled),
        )
        val colors = intArrayOf(
            getColor(R.color.text_mountain),
            getColor(R.color.text_silver),
        )
        return ColorStateList(states, colors)
    }

    /**
     * Consider two amounts are not equal if they are not equal excluding trailing dot.
     * We should respect trailing dot in user input as it can be used to enter fractional part.
     * Example: 6. == 6; 6.0 != 6; 6.0 == 6.0
     */
    private fun checkAmountsAreNotEqualExcludingTrailingDot(a: String?, b: String?): Boolean {
        val aWithoutTrailingDot = a?.removeSuffix(".")
        val bWithoutTrailingDot = b?.removeSuffix(".")
        return aWithoutTrailingDot != bWithoutTrailingDot
    }
}
