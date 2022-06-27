package org.p2p.wallet.swap.ui.orca

import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetSwapDetailsBinding
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.SwapFee
import org.p2p.wallet.swap.model.orca.SwapPrice
import org.p2p.wallet.swap.model.orca.SwapTotal
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.getColor
import org.p2p.wallet.utils.withTextOrGone

// todo: too complex functions, make it simpler
class SwapDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetSwapDetailsBinding.inflate(
        LayoutInflater.from(context), this
    )

    private var isGlobalExpanded: Boolean = false

    init {
        orientation = VERTICAL

        with(binding) {
            headerView.setOnClickListener {
                showExpanded(!isGlobalExpanded)
            }
            headerView.isEnabled = false
        }
    }

    fun showPrice(data: SwapPrice?) {
        with(binding) {
            if (data == null) {
                sourcePriceView.isVisible = false
                destinationPriceView.isVisible = false
                return@with
            }

            sourcePriceView.isVisible = isGlobalExpanded
            destinationPriceView.isVisible = isGlobalExpanded
            destinationNameTextView.text = context.getString(R.string.swap_price_format, data.destinationSymbol)
            destinationPriceTextView.text = SpanUtils.highlightText(
                data.fullSourcePrice,
                data.approxSourceUsd.orEmpty(),
                getColor(R.color.backgroundDisabled)
            )
            sourceNameTextView.text = context.getString(R.string.swap_price_format, data.sourceSymbol)
            sourcePriceTextView.text = SpanUtils.highlightText(
                data.fullDestinationPrice,
                data.approxDestinationUsd.orEmpty(),
                getColor(R.color.backgroundDisabled)
            )
        }
    }

    fun showFee(fee: SwapFee?) {
        if (fee == null) {
            binding.accountCreationFeeView.isVisible = false
            binding.errorTextView.isVisible = false
            binding.totalFeeTextView.isVisible = false
            return
        }

        with(binding) {
            accountCreationFeeView.isVisible = isGlobalExpanded
            paidByTextView.isVisible = fee.isFreeTransactionAvailable

            val feeText = if (fee.isFreeTransactionAvailable) {
                context.getString(R.string.send_free_transaction)
            } else {
                fee.transactionFee
            }

            transactionFeeTextView.text = feeText
            accountCreationFeeView.isVisible = isGlobalExpanded

            val accountText = context.getString(R.string.swap_account_creation_format, fee.accountCreationToken)
            accountCreationTextView.text = accountText

            val creationFeeUsdText = fee.accountCreationFeeUsd.orEmpty()
            val creationFeeText = "${fee.accountCreationFee} $creationFeeUsdText".trim()
            val spannedFee = SpanUtils.highlightText(
                creationFeeText, creationFeeUsdText, getColor(R.color.backgroundDisabled)
            )
            accountCreationTokenTextView.text = spannedFee
        }
    }

    fun showFeePayerToken(feePayerToken: String) {
        binding.feeTokenTextView.text = feePayerToken
    }

    fun showSlippage(slippage: Slippage) {
        binding.slippageTextView.text = slippage.percentValue
    }

    fun showTotal(data: SwapTotal?) {
        /*
         * Rare case, when source token is only available.
         * Destination is empty, we are setting the default state
         * */
        if (data == null) {
            val totalText = context.getString(R.string.swap_total_zero_sol)
            binding.totalTextView.text = buildTotalText(totalText)

            binding.showExpanded(false)
            binding.totalView.isVisible = false
            binding.receiveView.isVisible = false
            binding.totalFeeTextView.isVisible = false
            binding.headerView.isEnabled = false
            binding.errorTextView.isVisible = false
            return
        }

        val totalText = data.getFormattedTotal(split = false)
        binding.totalTextView.text = buildTotalText(totalText)

        binding.headerView.isEnabled = true
        binding.totalView.isVisible = isGlobalExpanded
        binding.receiveView.isVisible = isGlobalExpanded

        binding.atLeastTextView.text = SpanUtils.highlightText(
            data.fullReceiveAtLeast,
            data.approxReceiveAtLeast.orEmpty(),
            getColor(R.color.backgroundDisabled)
        )

        binding.totalFeeTextView.text = data.getFormattedTotal(split = true)
    }

    fun showError(errorText: String?) {
        binding.errorTextView withTextOrGone errorText
    }

    fun setOnPayFeeClickListener(callback: () -> Unit) {
        binding.feeTokenTextView.setOnClickListener { callback.invoke() }
    }

    fun setOnSlippageClickListener(callback: () -> Unit) {
        binding.slippageTextView.setOnClickListener { callback.invoke() }
    }

    fun setOnTransactionFeeClickListener(callback: () -> Unit) {
        binding.transactionFeeView.setOnClickListener { callback.invoke() }
    }

    private fun buildTotalText(totalAmount: String): SpannableString {
        val totalDataText = context.getString(R.string.swap_total_format, totalAmount)
        return SpanUtils.setTextBold(totalDataText, totalAmount)
    }

    private fun WidgetSwapDetailsBinding.showExpanded(isExpanded: Boolean) {
        destinationPriceView.isVisible = isExpanded
        sourcePriceView.isVisible = isExpanded
        priceDividerView.isVisible = isExpanded
        payFeeView.isVisible = isExpanded
        transactionFeeView.isVisible = isExpanded
        accountCreationFeeView.isVisible = isExpanded && accountCreationTokenTextView.text.isNotEmpty()
        feeDividerView.isVisible = isExpanded
        slippageView.isVisible = isExpanded
        receiveView.isVisible = isExpanded
        totalDividerView.isVisible = isExpanded
        totalView.isVisible = isExpanded
        totalFeeTextView.isVisible = isExpanded

        val rotationValue = if (isExpanded) 180f else 0f
        arrowImageView
            .animate()
            .rotation(rotationValue)
            .start()

        isGlobalExpanded = isExpanded
    }
}
