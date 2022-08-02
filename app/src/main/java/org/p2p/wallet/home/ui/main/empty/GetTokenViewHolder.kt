package org.p2p.wallet.home.ui.main.empty

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.databinding.ItemGetTokenBinding
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.ui.main.adapter.OnHomeItemsClickListener

class GetTokenViewHolder(
    private val binding: ItemGetTokenBinding,
    private val listener: OnHomeItemsClickListener
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        listener: OnHomeItemsClickListener
    ) : this(
        binding = ItemGetTokenBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener = listener
    )

    fun onBind(item: HomeElementItem.Shown, isZerosHidden: Boolean) = with(binding) {
        // TODO PWN-4400 add logic of tokens here
    }
}
