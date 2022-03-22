package org.p2p.wallet.renbtc.ui.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.recycler.adapter.BaseDiffAdapter
import org.p2p.wallet.common.ui.recycler.adapter.BaseViewHolder
import org.p2p.wallet.databinding.ItemRenTransactionBinding
import org.p2p.wallet.renbtc.model.RenTransaction
import org.p2p.wallet.renbtc.model.RenTransactionStatus

class RenTransactionsAdapter(
    private val onTransactionClicked: (RenTransaction) -> Unit
) : BaseDiffAdapter<RenTransaction>() {

    override val mDiffer: AsyncListDiffer<RenTransaction> =
        AsyncListDiffer(
            this,
            object :
                DiffUtil.ItemCallback<RenTransaction>() {
                override fun areItemsTheSame(oldItem: RenTransaction, newItem: RenTransaction) =
                    oldItem.transactionId == newItem.transactionId

                override fun areContentsTheSame(oldItem: RenTransaction, newItem: RenTransaction) =
                    oldItem == newItem
            }
        )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<RenTransaction> =
        ViewHolder(parent)

    private inner class ViewHolder(
        private val binding: ItemRenTransactionBinding
    ) : BaseViewHolder<RenTransaction>(binding.root) {

        constructor(parent: ViewGroup) : this(
            ItemRenTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBind(item: RenTransaction) {
            with(binding) {
                amountTextView.isVisible = false
                arrowImageView.isVisible = true
                titleTextView.text = item.getTransactionTitle(itemView.context)
                subTitleTextView.text = item.getLatestStatus()?.getStringResId(itemView.context)
                val color = if (item.getLatestStatus() is RenTransactionStatus.SuccessfullyMinted) {
                    itemView.context.getColor(R.color.colorGreen)
                } else {
                    itemView.context.getColor(R.color.backgroundDisabled)
                }
                subTitleTextView.setTextColor(color)

                root.setOnClickListener { onTransactionClicked(item) }
            }
        }
    }
}
