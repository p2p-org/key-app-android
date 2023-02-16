package org.p2p.wallet.swap.ui.jupiter.main.widget

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.util.AttributeSet
import org.p2p.core.common.TextContainer
import org.p2p.uikit.utils.drawable.shape.shapeRounded16dp
import org.p2p.uikit.utils.drawable.shapeDrawable
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

    init {
        minHeight = 120.toPx()
        background = shapeDrawable(shapeRounded16dp())
        backgroundTintList = backgroundTint()
        binding.textViewWidgetTitle.setTextColor(widgetTitleTint())
        initInputType = binding.editTextAmount.inputType
    }

    fun bind(model: SwapWidgetModel) {
        when (model) {
            is SwapWidgetModel.Content -> bindContent(model)
            is SwapWidgetModel.Loading -> bindLoading(model)
        }
    }

    private fun bindLoading(model: SwapWidgetModel.Loading) {
        isEnabled = !model.isStatic
        binding.textViewWidgetTitle.bindOrGone(model.widgetTitle)
        binding.textViewAvailableAmountTitle.isVisible = false
        binding.textViewAvailableAmountValue.isVisible = false
        binding.textViewCurrencyName.bindOrGone(model.currencySkeleton)
        bindInput(model, model.amountSkeleton)
        binding.textViewBalance.bindOrGone(model.balanceSkeleton)
        binding.textViewFiatAmount.isVisible = false
    }

    private fun bindContent(model: SwapWidgetModel.Content) {
        isEnabled = !model.isStatic
        binding.textViewWidgetTitle.bindOrGone(model.widgetTitle)
        binding.textViewAvailableAmountTitle.isVisible = model.availableAmount != null
        binding.textViewAvailableAmountValue.bindOrGone(model.availableAmount)
        binding.textViewCurrencyName.bindOrGone(model.currencyName)
        bindInput(model, model.amount)
        binding.textViewBalance.bindOrGone(model.balance)
        binding.textViewFiatAmount.bindOrGone(model.fiatAmount)
    }

    private fun bindInput(model: SwapWidgetModel, amount: TextViewCellModel?) {
        val isStatic = when (model) {
            is SwapWidgetModel.Content -> model.isStatic
            is SwapWidgetModel.Loading -> model.isStatic
        }
        val isNotEnabled = isStatic || model is SwapWidgetModel.Loading
        binding.editTextAmount.inputType = if (isNotEnabled) InputType.TYPE_NULL else initInputType
        binding.editTextAmount.isFocusable = !isNotEnabled
        binding.editTextAmount.bindOrGone(amount ?: TextViewCellModel.Raw(text = TextContainer("")))
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
