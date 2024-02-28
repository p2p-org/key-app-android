package org.p2p.wallet.receive.list

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.core.token.TokenMetadata
import org.p2p.core.utils.Constants
import org.p2p.core.utils.Constants.SOL_NAME
import org.p2p.wallet.databinding.ItemTokenListBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class TokenListAdapter(
    private val glideManager: GlideManager
) : RecyclerView.Adapter<TokenListAdapter.ViewHolder>() {

    private val data = mutableListOf<TokenMetadata>()

    fun setItems(new: List<TokenMetadata>) {
        val old = ArrayList(data)
        data.clear()
        data.addAll(new)
        DiffUtil.calculateDiff(getDiffCallback(old, data))
            .dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent, glideManager)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(
        parent: ViewGroup,
        private val glideManager: GlideManager,
        binding: ItemTokenListBinding = parent.inflateViewBinding(attachToRoot = false)
    ) : RecyclerView.ViewHolder(binding.root) {

        private val imageView = binding.imageView
        private val textView = binding.textView
        private val symbolTextView = binding.symbolTextView

        fun bind(value: TokenMetadata) {
            if (value.mintAddress == Constants.WRAPPED_SOL_MINT) {
                textView.text = SOL_NAME
            } else {
                textView.text = value.name
            }
            symbolTextView.text = value.symbol
            glideManager.load(imageView, value.iconUrl)
        }
    }

    private fun getDiffCallback(
        oldList: List<TokenMetadata>,
        newList: List<TokenMetadata>
    ) = object : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return old.mintAddress == new.mintAddress
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size
    }
}
