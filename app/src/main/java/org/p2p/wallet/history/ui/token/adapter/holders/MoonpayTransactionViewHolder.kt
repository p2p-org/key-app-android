package org.p2p.wallet.history.ui.token.adapter.holders

import androidx.recyclerview.widget.RecyclerView
import android.content.res.ColorStateList
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemHistoryMoonpayTransactionBinding
import org.p2p.wallet.history.ui.token.adapter.MoonpayTransactionItem
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class MoonpayTransactionViewHolder(
    parent: ViewGroup,
    private val binding: ItemHistoryMoonpayTransactionBinding = parent.inflateViewBinding(attachToRoot = false)
) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(item: MoonpayTransactionItem) {
        with(binding) {
            renderStatusIcon(item)
            renderTitleAndSubtitle(item)
            renderAmounts(item)
        }
    }

    private fun ItemHistoryMoonpayTransactionBinding.renderStatusIcon(item: MoonpayTransactionItem) {
        val backgroundRes: Int
        val iconColor: Int
        if (item is MoonpayTransactionItem.TransactionFailedItem) {
            backgroundRes = org.p2p.uikit.R.drawable.bg_rounded_solid_rose20_24
            iconColor = R.color.icons_rose
        } else {
            backgroundRes = org.p2p.uikit.R.drawable.bg_rounded_solid_rain_24
            iconColor = R.color.icons_night
        }

        imageViewTransactionStatusIcon.setBackgroundResource(backgroundRes)
        imageViewTransactionStatusIcon.imageTintList = ColorStateList.valueOf(getColor(iconColor))
    }

    private fun ItemHistoryMoonpayTransactionBinding.renderTitleAndSubtitle(item: MoonpayTransactionItem) {
        val title: String
        val subtitle: String
        when (item) {
            is MoonpayTransactionItem.WaitingForDepositItem -> {
                title = getString(
                    R.string.transaction_history_moonpay_waiting_for_deposit_title,
                    item.amountInSol
                )
                subtitle = getString(
                    R.string.transaction_history_moonpay_waiting_for_deposit_subtitle,
                    item.moonpayWalletAddress.base58Value.cutMiddle()
                )
            }
            is MoonpayTransactionItem.TransactionPendingItem -> {
                title = getString(R.string.transaction_history_moonpay_pending_title)
                subtitle = getString(R.string.transaction_history_moonpay_completed_subtitle)
            }
            is MoonpayTransactionItem.TransactionCompletedItem -> {
                title = getString(R.string.transaction_history_moonpay_completed_title)
                subtitle = getString(R.string.transaction_history_moonpay_completed_subtitle)
            }
            is MoonpayTransactionItem.TransactionFailedItem -> {
                title = getString(R.string.transaction_history_moonpay_failed_title)
                subtitle = getString(R.string.transaction_history_moonpay_failed_subtitle)
            }
        }

        layoutTransactionDetails.titleTextView.text = title
        layoutTransactionDetails.subtitleTextView.text = subtitle
    }

    private fun renderAmounts(item: MoonpayTransactionItem) {
        binding.layoutTransactionDetails.valueTextView.text = binding.getString(
            R.string.transaction_history_moonpay_amount_usd,
            item.amountInUsd.toPlainString()
        )
        binding.layoutTransactionDetails.totalTextView.text = binding.getString(
            R.string.transaction_history_moonpay_amount_sol,
            item.amountInSol.toPlainString()
        )
    }
}
