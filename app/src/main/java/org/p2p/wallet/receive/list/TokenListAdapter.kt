package org.p2p.wallet.receive.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.p2p.wallet.databinding.ItemTokenListBinding
import org.p2p.core.token.TokenMetadata
import org.p2p.core.utils.Constants.SOL_NAME

class TokenListAdapter : RecyclerView.Adapter<TokenListAdapter.ViewHolder>() {

    private val data = mutableListOf<TokenMetadata>()

    fun setItems(new: List<TokenMetadata>) {
        val old = ArrayList(data)
        data.clear()
        data.addAll(new)
        DiffUtil.calculateDiff(getDiffCallback(old, data)).dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemTokenListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(
        binding: ItemTokenListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val imageView = binding.imageView
        private val textView = binding.textView
        private val symbolTextView = binding.symbolTextView

        fun bind(value: TokenMetadata) {
            // TODO temporary solution
            if (value.symbol == "SOL") {
                textView.text = SOL_NAME
            } else {
                textView.text = value.name
            }
            symbolTextView.text = value.symbol
            Glide.with(imageView).load(value.iconUrl).into(imageView)
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
            return old.name == new.name && old.symbol == new.symbol
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size
    }
}
