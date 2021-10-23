package org.p2p.wallet.renbtc.ui.statuses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import org.p2p.wallet.common.recycler.adapter.BaseDiffAdapter
import org.p2p.wallet.common.recycler.adapter.BaseViewHolder
import org.p2p.wallet.databinding.ItemReceiveStatusBinding
import org.p2p.wallet.renbtc.model.RenVMStatus
import java.util.Date

class ReceiveStatusesAdapter : BaseDiffAdapter<RenVMStatus>() {

    override val mDiffer: AsyncListDiffer<RenVMStatus> =
        AsyncListDiffer(
            this,
            object :
                DiffUtil.ItemCallback<RenVMStatus>() {
                override fun areItemsTheSame(oldItem: RenVMStatus, newItem: RenVMStatus) =
                    oldItem.date == newItem.date

                override fun areContentsTheSame(oldItem: RenVMStatus, newItem: RenVMStatus) =
                    oldItem == newItem
            }
        )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<RenVMStatus> =
        ViewHolder(parent)

    private inner class ViewHolder(
        private val binding: ItemReceiveStatusBinding
    ) : BaseViewHolder<RenVMStatus>(binding.root) {

        constructor(parent: ViewGroup) : this(
            ItemReceiveStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBind(item: RenVMStatus) {
            when (item) {
                is RenVMStatus.SuccessfullyMinted -> {
                    binding.amountTextView.isVisible = true
                    binding.amountTextView.text = item.getMintedData()
                }
                else -> {
                    binding.amountTextView.isVisible = false
                }
            }

            binding.titleTextView.text = item.getStringResId(itemView.context)
            binding.subTitleTextView.text = Date(item.date).toString()
        }
    }
}