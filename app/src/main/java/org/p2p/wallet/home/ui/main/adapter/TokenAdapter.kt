package org.p2p.wallet.home.ui.main.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.R
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.HomeElementItem.Action
import org.p2p.wallet.home.model.HomeElementItem.Banner
import org.p2p.wallet.home.model.HomeElementItem.Claim
import org.p2p.wallet.home.model.HomeElementItem.Hidden
import org.p2p.wallet.home.model.HomeElementItem.Shown
import org.p2p.wallet.home.model.HomeElementItem.StrigaOnRampTokenItem
import org.p2p.wallet.home.model.HomeElementItem.Title

class TokenAdapter(
    private val glideManager: GlideManager,
    private val listener: HomeItemsClickListeners
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val DIFF_FIELD_TOGGLE_BUTTON = "DIFF_FIELD_TOGGLE_BUTTON"
        const val DIFF_FIELD_TOKEN_BALANCE = "DIFF_FIELD_TOKEN_BALANCE"
        const val DIFF_FIELD_HIDDEN_TOKEN_BALANCE = "DIFF_FIELD_HIDDEN_TOKEN_BALANCE"
        const val DIFF_FIELD_TITLE = "DIFF_FIELD_TITLE"
    }

    private val data = mutableListOf<HomeElementItem>()

    private var isZerosHidden: Boolean = true

    @Suppress("UNCHECKED_CAST")
    fun <T : HomeElementItem> updateItem(
        itemFilter: (HomeElementItem) -> Boolean,
        transform: (T) -> T,
        animateChanges: Boolean = false
    ) {
        val index = data.indexOfFirst(itemFilter)
        if (index != -1) {
            val old = data[index] as T
            val new = transform(old)
            data[index] = new
            if (animateChanges) {
                notifyItemChanged(index)
            } else {
                // workaround for disabling animation
                notifyItemChanged(index, Unit)
            }
        }
    }

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
        is Banner -> R.layout.item_home_banner
        is Title -> R.layout.item_main_header
        is Claim -> R.layout.item_token_to_claim
        is StrigaOnRampTokenItem -> R.layout.item_token_to_striga_onramp
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_token -> TokenViewHolder(parent, listener)
        R.layout.item_token_hidden -> TokenHiddenViewHolder(parent, listener)
        R.layout.item_token_group_button -> TokenButtonViewHolder(parent, listener)
        R.layout.item_main_header -> HeaderViewHolder(parent)
        R.layout.item_token_to_claim -> BridgeTokenToClaimViewHolder(parent, glideManager, listener)
        R.layout.item_home_banner -> SingleBannerViewHolder(parent, listener)
        R.layout.item_token_to_striga_onramp -> StrigaTokenToOnRampViewHolder(parent, glideManager, listener)
        else -> error("Unknown viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        when (holder) {
            is TokenViewHolder -> holder.onBind(item as Shown, isZerosHidden)
            is TokenHiddenViewHolder -> holder.onBind(item as Hidden, isZerosHidden)
            is TokenButtonViewHolder -> holder.onBind(item as Action)
            is SingleBannerViewHolder -> holder.onBind((item as Banner).banner)
            is HeaderViewHolder -> holder.onBind(item as Title)
            is BridgeTokenToClaimViewHolder -> holder.onBind(item as Claim)
            is StrigaTokenToOnRampViewHolder -> holder.onBind(item as StrigaOnRampTokenItem)
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
