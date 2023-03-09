package org.p2p.wallet.swap.ui.jupiter.main.widget

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.EditText
import android.widget.TextView
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.core.utils.DEFAULT_DECIMAL
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
) : ConstraintLayout(context, attrs) {

    private val binding = inflateViewBinding<WidgetSwapBinding>()
    private val initInputType: Int
    private var internalOnAmountChanged: ((newAmount: String) -> Unit)? = null
    private var currentAmountCell: TextViewCellModel? = null
    var onAmountChanged: (newAmount: String) -> Unit = {}
    var onAllAmountClick: () -> Unit = {}
    var onChangeTokenClick: () -> Unit = {}
    var onInputClicked: () -> Unit = {}

    init {
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
        }
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

    private fun bindLoading(model: SwapWidgetModel.Loading) = with(binding) {
        isEnabled = !model.isStatic
        textViewWidgetTitle.bindOrGone(model.widgetTitle)
        textViewAvailableAmountTitle.isVisible = false
        textViewAvailableAmountValue.isVisible = false
        textViewCurrencyName.bindOrGone(model.currencySkeleton)
        textViewCurrencyName.setOnClickListener(null)
        textViewChangeCurrency.setOnClickListener(null)
        bindLoadingInput(model.amountSkeleton)
        textViewBalance.bindOrGone(model.balanceSkeleton)
        textViewFiatAmount.isVisible = false
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
        val amountMaxDecimals = model.amountMaxDecimals ?: DEFAULT_DECIMAL
        updateFormatter(amountMaxDecimals)
        val newAmountRaw = (newAmount as? TextViewCellModel.Raw)?.text?.getString(context)
        val oldAMountRaw = editTextAmount.text?.toString() ?: emptyString()
        val newTextColorRes = (newAmount as? TextViewCellModel.Raw)?.textColor
        val oldTextColorRes = (currentAmountCell as? TextViewCellModel.Raw)?.textColor
        if (currentAmountCell is TextViewCellModel.Skeleton ||
            newAmountRaw != oldAMountRaw ||
            newTextColorRes != oldTextColorRes
        ) {
            internalOnAmountChanged = null
            editTextAmount.bindOrGone(newAmount)
            currentAmountCell = newAmount
            editTextAmount.setSelection(editTextAmount.text.length)
            internalOnAmountChanged = { onAmountChanged(it) }
        }
    }

    private fun bindLoadingInput(skeleton: TextViewCellModel.Skeleton) = with(binding) {
        val readOnly = true
        val inputType = InputType.TYPE_NULL
        editTextAmount.setReadOnly(readOnly, inputType)
        internalOnAmountChanged = null
        editTextAmount.bindOrGone(skeleton)
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
}
