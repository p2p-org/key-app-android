package org.p2p.wallet.history.ui.token.adapter.holders

import androidx.core.view.isVisible
import android.content.res.ColorStateList
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemHistoryMoonpayTransactionBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class MoonpayTransactionViewHolder(
    parent: ViewGroup,
    private val onItemClicked: (SellTransactionViewDetails) -> Unit,
    private val binding: ItemHistoryMoonpayTransactionBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {
    fun onBind(item: HistoryItem.MoonpayTransactionItem) = with(binding) {
        root.setOnClickListener { onItemClicked.invoke(item.transactionDetails) }
        renderStatusIcon(item)
        renderTitleAndSubtitle(item)
        renderAmounts(item)
    }

    private fun ItemHistoryMoonpayTransactionBinding.renderStatusIcon(item: HistoryItem.MoonpayTransactionItem) {
        val iconRes: Int
        val backgroundRes: Int
        val iconColor: Int
        when (item.status) {
            SellTransactionStatus.FAILED -> {
                iconRes = R.drawable.ic_alert_rounded
                backgroundRes = org.p2p.uikit.R.drawable.bg_rounded_solid_rose20_24
                iconColor = R.color.icons_rose
            }
            SellTransactionStatus.WAITING_FOR_DEPOSIT -> {
                iconRes = R.drawable.ic_alert_rounded
                backgroundRes = org.p2p.uikit.R.drawable.bg_rounded_solid_rain_24
                iconColor = R.color.icons_night
            }
            else -> {
                iconRes = R.drawable.ic_action_schedule_filled
                backgroundRes = org.p2p.uikit.R.drawable.bg_rounded_solid_rain_24
                iconColor = R.color.icons_night
            }
        }

        imageViewTransactionStatusIcon.setImageResource(iconRes)
        imageViewTransactionStatusIcon.setBackgroundResource(backgroundRes)
        imageViewTransactionStatusIcon.imageTintList = ColorStateList.valueOf(getColor(iconColor))
    }

    private fun ItemHistoryMoonpayTransactionBinding.renderTitleAndSubtitle(item: HistoryItem.MoonpayTransactionItem) {
        val titleStatus: String
        val subtitleReceiver: String
        when (item.status) {
            SellTransactionStatus.WAITING_FOR_DEPOSIT -> {
                titleStatus = getString(
                    R.string.transaction_history_moonpay_waiting_for_deposit_title,
                    item.amountInSol
                )
                subtitleReceiver = getString(
                    R.string.transaction_history_moonpay_waiting_for_deposit_subtitle,
                    item.transactionDetails.receiverAddress.cutMiddle()
                )
            }
            SellTransactionStatus.PENDING -> {
                titleStatus = getString(R.string.transaction_history_moonpay_pending_title)
                subtitleReceiver = item.transactionDetails.receiverAddress
            }
            SellTransactionStatus.COMPLETED -> {
                titleStatus = getString(R.string.transaction_history_moonpay_completed_title)
                subtitleReceiver = item.transactionDetails.receiverAddress
            }
            SellTransactionStatus.FAILED -> {
                titleStatus = getString(R.string.transaction_history_moonpay_failed_title)
                subtitleReceiver = getString(R.string.transaction_history_moonpay_failed_subtitle)
            }
        }

        layoutTransactionDetails.textViewTitle.text = titleStatus
        layoutTransactionDetails.textViewSubtitle.text = subtitleReceiver
    }

    private fun renderAmounts(item: HistoryItem.MoonpayTransactionItem) {
        binding.layoutTransactionDetails.textViewValue.text = binding.getString(
            R.string.transaction_history_moonpay_amount_fiat,
            item.amountInFiat,
            item.transactionDetails.fiatAbbreviation.uppercase()
        )
        binding.layoutTransactionDetails.textViewTotal.isVisible = false // hide SOL amount
    }
}
