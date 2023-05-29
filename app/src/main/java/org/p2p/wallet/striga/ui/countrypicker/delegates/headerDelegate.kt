package org.p2p.wallet.striga.ui.countrypicker.delegates

import androidx.constraintlayout.widget.ConstraintLayout
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.databinding.ItemStrigaCountryBinding
import org.p2p.wallet.databinding.ItemStrigaCountryPickerHeaderBinding

private typealias HeaderInflateListener = ((financeBlock: TextView) -> Unit)
private typealias HeaderBindListener = ((view: TextView, item: StrigaCountryHeaderCellModel) -> Unit)

private val inflateHeaderViewBinding = { inflater: LayoutInflater, parent: ViewGroup ->
    ItemStrigaCountryPickerHeaderBinding.inflate(inflater, parent, false)
}

fun strigaHeaderDelegate(
    inflateListener: HeaderInflateListener? = null,
    onBindListener: HeaderBindListener? = null
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<StrigaCountryHeaderCellModel, AnyCellItem, ItemStrigaCountryPickerHeaderBinding>(
        viewBinding = inflateHeaderViewBinding
    ) {
        inflateListener?.invoke(binding.root)

        bind {
            binding.titleTextView.text = context.getString(item.titleResId)
            onBindListener?.invoke(binding.root, item)
        }
    }

private typealias CountryInflateListener = ((financeBlock: ConstraintLayout) -> Unit)
private typealias CountryBindListener = ((view: ConstraintLayout, item: StrigaCountryCellModel) -> Unit)
private typealias OnClickListener = ((selectedItem: Country) -> Unit)

private val inflateCountryViewBinding = { inflater: LayoutInflater, parent: ViewGroup ->
    ItemStrigaCountryBinding.inflate(inflater, parent, false)
}

fun strigaCountryDelegate(
    inflateListener: CountryInflateListener? = null,
    onBindListener: CountryBindListener? = null,
    onItemClickListener: OnClickListener? = null
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<StrigaCountryCellModel, AnyCellItem, ItemStrigaCountryBinding>(
        viewBinding = inflateCountryViewBinding
    ) {
        inflateListener?.invoke(binding.root)
        binding.root.setOnClickListener {
            onItemClickListener?.invoke(item.country)
        }
        bind {
            binding.emojiTextView.text = item.country.flagEmoji
            binding.textViewCountryName.text = item.country.name
            onBindListener?.invoke(binding.root, item)
        }
    }
