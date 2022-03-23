package org.p2p.wallet.renbtc.ui.status

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import org.p2p.wallet.common.ui.recycler.adapter.BaseDiffAdapter
import org.p2p.wallet.common.ui.recycler.adapter.BaseViewHolder
import org.p2p.wallet.databinding.ItemRenTransactionBinding
import org.p2p.wallet.renbtc.model.RenTransactionStatus
import org.p2p.wallet.utils.DateTimeUtils

class RenStatusesAdapter : BaseDiffAdapter<RenTransactionStatus>() {

    override val mDiffer: AsyncListDiffer<RenTransactionStatus> =
        AsyncListDiffer(
            this,
            object :
                DiffUtil.ItemCallback<RenTransactionStatus>() {
                override fun areItemsTheSame(oldItem: RenTransactionStatus, newItem: RenTransactionStatus) =
                    oldItem.date == newItem.date

                override fun areContentsTheSame(oldItem: RenTransactionStatus, newItem: RenTransactionStatus) =
                    oldItem == newItem
            }
        )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<RenTransactionStatus> =
        ViewHolder(parent)

    private inner class ViewHolder(
        private val binding: ItemRenTransactionBinding
    ) : BaseViewHolder<RenTransactionStatus>(binding.root) {

        constructor(parent: ViewGroup) : this(
            ItemRenTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBind(item: RenTransactionStatus) {
            when (item) {
                is RenTransactionStatus.SuccessfullyMinted -> {
                    binding.amountTextView.isVisible = true
                    binding.amountTextView.text = item.getMintedData()
                }
                else -> {
                    binding.amountTextView.isVisible = false
                }
            }

            binding.arrowImageView.isVisible = false
            binding.titleTextView.text = item.getStringResId(itemView.context)
            binding.subTitleTextView.text = DateTimeUtils.convertTo12or24Format(item.date, itemView.context)
        }
    }
}
