package org.p2p.wallet.auth.ui.phone.countrypicker

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.auth.ui.phone.model.CountryCodeAdapterItem
import org.p2p.wallet.databinding.ItemCountryCodeBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class CountryPickerAdapter(private val onItemClickListener: (CountryCodeAdapterItem) -> Unit) :
    RecyclerView.Adapter<CountryPickerAdapter.ViewHolder>() {

    private val data = mutableListOf<CountryCodeAdapterItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflateViewBinding(attachToRoot = false), onItemClickListener)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun setItems(items: List<CountryCodeAdapterItem>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ItemCountryCodeBinding,
        private val onItemClickListener: (CountryCodeAdapterItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: CountryCodeAdapterItem) = with(binding) {
            val country = item.country
            emojiTextView.text = country.flagEmoji
            nameTextView.text = country.name
            codeTextView.text = "+${country.phoneCode}"
            itemView.setOnClickListener {
                onItemClickListener.invoke(item)
            }
            checkImageView.isVisible = item.isSelected
        }
    }
}
