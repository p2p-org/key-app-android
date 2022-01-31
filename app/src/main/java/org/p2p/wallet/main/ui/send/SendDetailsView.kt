package org.p2p.wallet.main.ui.send

import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetSendDetailsBinding
import org.p2p.wallet.main.model.SendTotal
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.withTextOrGone

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
            headerView.setOnClickListener {
                showExpanded(!isExpanded)
            }
            headerView.isEnabled = false
        }
    }

    fun showTotal(data: SendTotal?) {
        with(binding) {
            val buildTotalText = buildTotalText(data)
            totalTextView.text = buildTotalText

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

            // fixme: min receive should be shown after account creation fee
            receiveTextView.text = SpanUtils.highlightText(
                data.fullReceive,
                data.approxReceive.orEmpty(),
                context.getColor(R.color.textIconSecondary)
            )
            totalSourceTextView.text = SpanUtils.highlightText(
                data.fullTotal,
                data.approxTotalUsd.orEmpty(),
                context.getColor(R.color.textIconSecondary)
            )

            val fullFee = data.fullFee
            val approxFeeUsd = data.approxFeeUsd
            if (fullFee != null && approxFeeUsd != null) {
                paidByTextView.isVisible = false
                totalFeeTextView.isVisible = isExpanded

                val feeText = SpanUtils.highlightText(
                    fullFee, approxFeeUsd, context.getColor(R.color.textIconSecondary)
                )
                totalFeeTextView.text = feeText
                freeTextView.text = feeText
            } else {
                totalFeeTextView.isVisible = false
                paidByTextView.isVisible = true
                freeTextView.setText(R.string.send_free_transaction)
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

    private fun buildTotalText(total: SendTotal?): SpannableString {
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
            transactionFeeView.isVisible = isExpanded
            feeDividerView.isVisible = isExpanded
            receiveView.isVisible = isExpanded
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