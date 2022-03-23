package org.p2p.wallet.swap.ui.orca

import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.StringRes
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

    private var isExpanded: Boolean = false

    init {
        orientation = VERTICAL

        with(binding) {
            headerView.setOnClickListener {
                showExpanded(!isExpanded)
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

            sourcePriceView.isVisible = isExpanded
            destinationPriceView.isVisible = isExpanded
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

    fun showFee(data: SwapFee?) {
        with(binding) {
            if (data == null) {
                accountCreationFeeView.isVisible = false
                return@with
            }

            accountCreationFeeView.isVisible = isExpanded

            if (data.isFreeTransactionAvailable) {
                transactionFeeTextView.setText(R.string.send_free_transaction)
                paidByTextView.isVisible = true
            } else {
                val spannedFee = SpanUtils.highlightText(
                    data.commonTransactionFee.orEmpty(),
                    data.approxTransactionFeeUsd.orEmpty(),
                    getColor(R.color.textIconSecondary)
                )
                transactionFeeTextView.text = spannedFee
                paidByTextView.isVisible = false
            }

            val accountCreationToken = data.accountCreationToken

            val fee = data.commonFee
            val approxFeeUsd = data.approxFeeUsd
            if (accountCreationToken != null && fee != null) {
                accountCreationFeeView.isVisible = isExpanded

                val account = context.getString(R.string.swap_account_creation_format, accountCreationToken)
                accountCreationTextView.text = account

                val spannedFee = SpanUtils.highlightText(
                    fee, approxFeeUsd, getColor(R.color.backgroundDisabled)
                )
                accountCreationTokenTextView.text = spannedFee
            } else {
                accountCreationFeeView.isVisible = false
            }
        }
    }

    fun showFeePayerToken(feePayerToken: String) {
        binding.feeTokenTextView.text = feePayerToken
    }

    fun showSlippage(slippage: Slippage) {
        binding.slippageTextView.text = slippage.percentValue
    }

    fun showTotal(data: SwapTotal?) {
        with(binding) {
            totalTextView.text = buildTotalText(data)

            if (data == null) {
                showExpanded(false)
                totalView.isVisible = false
                receiveView.isVisible = false
                totalFeeTextView.isVisible = false
                headerView.isEnabled = false
                return@with
            }

            headerView.isEnabled = true
            totalView.isVisible = isExpanded
            receiveView.isVisible = isExpanded

            atLeastTextView.text = SpanUtils.highlightText(
                data.fullReceiveAtLeast,
                data.approxReceiveAtLeast.orEmpty(),
                getColor(R.color.backgroundDisabled)
            )
            totalSourceTextView.text = SpanUtils.highlightText(
                data.fullTotal,
                data.approxTotalUsd.orEmpty(),
                getColor(R.color.backgroundDisabled)
            )

            val fullFee = data.fullFee
            if (fullFee != null) {
                totalFeeTextView.isVisible = isExpanded

                totalFeeTextView.text = SpanUtils.highlightText(
                    fullFee, data.approxFeeUsd, getColor(R.color.textIconSecondary)
                )
            } else {
                totalFeeTextView.isVisible = false
            }
        }
    }

    fun showError(@StringRes errorRes: Int?) {
        val error = errorRes?.let { context.getString(it) }
        binding.errorTextView withTextOrGone error
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

    private fun buildTotalText(total: SwapTotal?): SpannableString {
        val totalAmount = if (total != null) {
            if (total.fee.isNullOrEmpty()) total.total else "${total.total} + ${total.fee}"
        } else {
            context.getString(R.string.swap_total_zero_sol)
        }

        val totalDataText = context.getString(R.string.swap_total_format, totalAmount)
        return SpanUtils.setTextBold(totalDataText, totalAmount)
    }

    private fun showExpanded(isExpanded: Boolean) {
        with(binding) {
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
        }

        this.isExpanded = isExpanded
    }
}
