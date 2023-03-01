package org.p2p.wallet.home.ui.main.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.R
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.HomeElementItem.Action
import org.p2p.wallet.home.model.HomeElementItem.Banners
import org.p2p.wallet.home.model.HomeElementItem.Claim
import org.p2p.wallet.home.model.HomeElementItem.Hidden
import org.p2p.wallet.home.model.HomeElementItem.Shown
import org.p2p.wallet.home.model.HomeElementItem.Title

class TokenAdapter(
    private val glideManager: GlideManager,
    private val listener: OnHomeItemsClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val DIFF_FIELD_TOGGLE_BUTTON = "DIFF_FIELD_TOGGLE_BUTTON"
        const val DIFF_FIELD_TOKEN_BALANCE = "DIFF_FIELD_TOKEN_BALANCE"
        const val DIFF_FIELD_HIDDEN_TOKEN_BALANCE = "DIFF_FIELD_HIDDEN_TOKEN_BALANCE"
        const val DIFF_FIELD_TITLE = "DIFF_FIELD_TITLE"
    }

    private val data = mutableListOf<HomeElementItem>()

    private var isZerosHidden: Boolean = true

    fun setItems(new: List<HomeElementItem>, isZerosHidden: Boolean) {
        this.isZerosHidden = isZerosHidden
        val old = data.toMutableList()
        data.clear()
        data.addAll(new)
        DiffUtil.calculateDiff(TokenAdapterDiffCallback(old, data)).dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is Shown -> R.layout.item_token
        is Hidden -> R.layout.item_token_hidden
        is Action -> R.layout.item_token_group_button
        is Banners -> R.layout.item_banners
        is Title -> R.layout.item_main_header
        is Claim -> R.layout.item_token_to_claim
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_token -> TokenViewHolder(parent, listener)
        R.layout.item_token_hidden -> TokenHiddenViewHolder(parent, listener)
        R.layout.item_token_group_button -> TokenButtonViewHolder(parent, listener)
        R.layout.item_banners -> BannersViewHolder(parent, listener)
        R.layout.item_main_header -> HeaderViewHolder(parent)
        R.layout.item_token_to_claim -> TokenToClaimViewHolder(parent, glideManager, listener)
        else -> throw IllegalStateException("Unknown viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TokenViewHolder -> holder.onBind(data[position] as Shown, isZerosHidden)
            is TokenHiddenViewHolder -> holder.onBind(data[position] as Hidden, isZerosHidden)
            is TokenButtonViewHolder -> holder.onBind(data[position] as Action)
            is BannersViewHolder -> holder.onBind(data[position] as Banners)
            is HeaderViewHolder -> holder.onBind(data[position] as Title)
            is TokenToClaimViewHolder -> holder.onBind(data[position] as Claim)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val fields = payloads.firstOrNull() as? Set<String>

        if (fields.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        val item = data[position]
        fields.forEach { field ->
            when (field) {
                DIFF_FIELD_TOKEN_BALANCE -> (holder as TokenViewHolder).bindBalance(item as Shown)
                DIFF_FIELD_HIDDEN_TOKEN_BALANCE -> (holder as TokenHiddenViewHolder).bindBalance(item as Hidden)
                DIFF_FIELD_TOGGLE_BUTTON -> (holder as TokenButtonViewHolder).bindIcon(item as Action)
                DIFF_FIELD_TITLE -> (holder as HeaderViewHolder).onBind(item as Title)
            }
        }
    }
}
