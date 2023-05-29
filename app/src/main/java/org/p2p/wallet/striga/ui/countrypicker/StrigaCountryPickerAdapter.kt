package org.p2p.wallet.striga.ui.countrypicker

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemCountryCodeBinding
import org.p2p.wallet.databinding.ItemStrigaCountryPickerHeaderBinding
import org.p2p.wallet.striga.model.StrigaCountryPickerItem
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class StrigaCountryPickerAdapter(private val onItemClickListener: (StrigaCountryPickerItem.CountryItem) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<StrigaCountryPickerItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.fragment_striga_country_picker -> {
                CountryViewHolder(
                    binding = parent.inflateViewBinding(attachToRoot = false),
                    onItemClickListener = onItemClickListener
                )
            }
            R.layout.item_striga_country_picker_header -> {
                HeaderViewHolder(binding = parent.inflateViewBinding(attachToRoot = false))
            }
            else -> {
                throw IllegalStateException("No ViewHolder found for $viewType")
            }
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CountryViewHolder -> {
                val item = data[position] as StrigaCountryPickerItem.CountryItem
                holder.onBind(item)
            }
            is HeaderViewHolder -> {
                val item = data[position] as StrigaCountryPickerItem.HeaderItem
                holder.onBind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is StrigaCountryPickerItem.HeaderItem -> R.layout.item_striga_country_picker_header
            is StrigaCountryPickerItem.CountryItem -> R.layout.item_country_code
        }
    }

    override fun getItemCount(): Int = data.size

    fun setItems(items: List<StrigaCountryPickerItem>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    inner class CountryViewHolder(
        private val binding: ItemCountryCodeBinding,
        private val onItemClickListener: (StrigaCountryPickerItem.CountryItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: StrigaCountryPickerItem.CountryItem) = with(binding) {
            val country = item.country
            emojiTextView.text = country.flagEmoji
            textViewCountryName.text = country.name
            itemView.setOnClickListener {
                onItemClickListener.invoke(item)
            }
        }
    }

    inner class HeaderViewHolder(
        private val binding: ItemStrigaCountryPickerHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: StrigaCountryPickerItem.HeaderItem) {
            binding.titleTextView.setText(item.tileResId)
        }
    }
}
