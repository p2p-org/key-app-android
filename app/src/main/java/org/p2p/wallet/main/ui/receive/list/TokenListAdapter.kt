package org.p2p.wallet.main.ui.receive.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.p2p.wallet.databinding.ItemTokenListBinding
import org.p2p.wallet.user.model.TokenData

class TokenListAdapter : RecyclerView.Adapter<TokenListAdapter.ViewHolder>() {

    private val data = mutableListOf<TokenData>()

    fun setItems(new: List<TokenData>) {
        data.clear()
        data.addAll(new)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemTokenListBinding.inflate(LayoutInflater.from(parent.context))
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
        fun bind(value: TokenData) {

            textView.text = value.name
            symbolTextView.text = value.symbol
            Glide.with(imageView).load(value.iconUrl).into(imageView)
        }
    }
}