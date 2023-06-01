package org.p2p.wallet.auth.ui.phone.countrypicker

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.CountryCodeItem
import org.p2p.wallet.databinding.ItemCountryCodeBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class CountryCodePickerAdapter(private val onItemClickListener: (CountryCode) -> Unit) :
    RecyclerView.Adapter<CountryCodePickerAdapter.ViewHolder>() {

    private val data = mutableListOf<CountryCodeItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflateViewBinding(attachToRoot = false), onItemClickListener)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun setItems(items: List<CountryCodeItem>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ItemCountryCodeBinding,
        private val onItemClickListener: (CountryCode) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: CountryCodeItem) = with(binding) {
            val country = item.country
            emojiTextView.text = country.flagEmoji
            textViewCountryName.text = country.countryName
            textViewCountryCode.text = "+${country.phoneCode}"
            itemView.setOnClickListener {
                onItemClickListener.invoke(item.country)
            }
            imageViewCheck.isVisible = item.isSelected
        }
    }
}
