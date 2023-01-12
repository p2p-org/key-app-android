package org.p2p.wallet.send.ui.main

import androidx.annotation.StringRes
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetSendDetailsBinding
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.withTextOrGone

@Deprecated("Will be removed, old design flow")
class SendDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetSendDetailsBinding.inflate(
        LayoutInflater.from(context), this
    )

    private var isExpanded: Boolean = false

    init {
        orientation = VERTICAL

        with(binding) {
            headerView.isEnabled = false
        }
    }

    fun showTotal(data: SendFeeTotal?) {
        with(binding) {
            val buildTotalText = buildTotalText(data)
            totalTextView.text = buildTotalText

            if (data == null) {
                totalFeeTextView.tag = null
                showExpanded(false, data)
                headerView.isEnabled = false
                totalSourceTextView.text = emptyString()
                headerView.setOnClickListener(null)
                return@with
            }

            headerView.isEnabled = true
            totalView.isVisible = isExpanded
            receiveView.isVisible = isExpanded
            totalFeeTextView.isVisible = isExpanded && data.showAdditionalFee

            val color = context.getColor(R.color.textIconSecondary)
            receiveTextView.text = SpanUtils.highlightText(
                data.fullReceive,
                data.approxReceive,
                color
            )
            totalSourceTextView.text = SpanUtils.highlightText(
                data.fullTotal,
                data.approxTotalUsd.orEmpty(),
                color
            )

            if (data.sendFee != null) {
                accountCreationFeeView.isVisible = isExpanded
                val feeText = SpanUtils.highlightText(
                    data.sendFee.accountCreationFeeUsd,
                    data.sendFee.getApproxAccountCreationFeeUsd().orEmpty(),
                    color
                )
                accountCreationTokenTextView.text = feeText
                totalFeeTextView.text = feeText
            } else {
                totalFeeTextView.isVisible = false
                paidByTextView.isVisible = true
                freeTextView.setText(R.string.send_free_transaction)
                accountCreationFeeView.isVisible = false
            }

            headerView.setOnClickListener {
                showExpanded(!isExpanded, data)
            }
        }
    }

    fun showError(@StringRes errorRes: Int?) {
        val error = errorRes?.let { context.getString(it) }
        binding.errorTextView withTextOrGone error
    }

    fun setOnPaidClickListener(callback: () -> Unit) {
        binding.transactionFeeView.setOnClickListener { callback() }
    }

    private fun buildTotalText(total: SendFeeTotal?): SpannedString {
        val totalAmount = total?.getTotalFee { context.getString(it) }
            ?: context.getString(R.string.swap_total_zero_sol)
        val totalText = context.getString(R.string.swap_total)
        return buildSpannedString {
            color(getColor(R.color.textIconSecondary)) { append(totalText) }
            append(" ")
            bold { append(totalAmount) }
        }
    }

    private fun showExpanded(isExpanded: Boolean, data: SendFeeTotal?) {
        with(binding) {
            transactionFeeView.isVisible = isExpanded
            feeDividerView.isVisible = isExpanded
            receiveView.isVisible = isExpanded
            totalView.isVisible = isExpanded

            accountCreationFeeView.isVisible = isExpanded && data?.showAccountCreation == true
            totalFeeTextView.isVisible = isExpanded && data?.showAdditionalFee == true

            val rotationValue = if (isExpanded) 180f else 0f
            arrowImageView
                .animate()
                .rotation(rotationValue)
                .start()
        }

        this.isExpanded = isExpanded
    }
}
