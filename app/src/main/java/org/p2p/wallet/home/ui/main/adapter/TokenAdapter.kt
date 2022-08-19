package org.p2p.wallet.home.ui.main.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.HomeElementItem.Action
import org.p2p.wallet.home.model.HomeElementItem.Banners
import org.p2p.wallet.home.model.HomeElementItem.Hidden
import org.p2p.wallet.home.model.HomeElementItem.Shown
import org.p2p.wallet.home.model.HomeElementItem.Title
import org.p2p.wallet.home.model.VisibilityState

private const val DIFF_FIELD_TOGGLE_BUTTON = "DIFF_FIELD_TOGGLE_BUTTON"
private const val DIFF_FIELD_TOKEN_BALANCE = "DIFF_FIELD_TOKEN_BALANCE"
private const val DIFF_FIELD_HIDDEN_TOKEN_BALANCE = "DIFF_FIELD_HIDDEN_TOKEN_BALANCE"
private const val DIFF_FIELD_TITLE = "DIFF_FIELD_TITLE"

class TokenAdapter(
    private val listener: OnHomeItemsClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<HomeElementItem>()

    private var isZerosHidden: Boolean = true

    fun setItems(new: List<HomeElementItem>, isZerosHidden: Boolean, state: VisibilityState) {
        this.isZerosHidden = isZerosHidden
        val old = data.toMutableList()
        data.clear()
        data.addAll(mapGroups(new, state))
        DiffUtil.calculateDiff(getDiffCallback(old, data)).dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is Shown -> R.layout.item_token
        is Hidden -> R.layout.item_token_hidden
        is Action -> R.layout.item_token_group_button
        is Banners -> R.layout.item_banners
        is Title -> R.layout.item_main_header
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_token -> TokenViewHolder(parent, listener)
        R.layout.item_token_hidden -> TokenHiddenViewHolder(parent, listener)
        R.layout.item_token_group_button -> TokenButtonViewHolder(parent, listener)
        R.layout.item_banners -> BannersViewHolder(parent, listener)
        R.layout.item_main_header -> HeaderViewHolder(parent)
        else -> throw IllegalStateException("Unknown viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TokenViewHolder -> holder.onBind(data[position] as Shown, isZerosHidden)
            is TokenHiddenViewHolder -> holder.onBind(data[position] as Hidden, isZerosHidden)
            is TokenButtonViewHolder -> holder.onBind(data[position] as Action)
            is BannersViewHolder -> holder.onBind(data[position] as Banners)
            is HeaderViewHolder -> holder.onBind(data[position] as Title)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val fields = (payloads.firstOrNull() as? Set<*>)?.filterIsInstance<String>()

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

    private fun mapGroups(tokens: List<HomeElementItem>, state: VisibilityState): List<HomeElementItem> {
        val result = mutableListOf<HomeElementItem>(Title(R.string.home_tokens))
        val groups = tokens.groupBy { it is Hidden }
        val hiddenTokens = groups[true].orEmpty()
        val visibleTokens = groups[false].orEmpty()

        result += visibleTokens
        result += Action(state)
        result += hiddenTokens

        return result
    }

    private fun getDiffCallback(
        oldList: List<HomeElementItem>,
        newList: List<HomeElementItem>
    ) = object : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return when {
                old is Shown && new is Shown ->
                    old.token.publicKey == new.token.publicKey
                old is Hidden && new is Hidden ->
                    old.token.publicKey == new.token.publicKey
                old is Action && new is Action ->
                    // should pass in contents compare
                    true
                else -> old == new
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return when {
                old is Shown && new is Shown ->
                    old.token.publicKey == new.token.publicKey &&
                        old.token.mintAddress == new.token.mintAddress &&
                        old.token.visibility == new.token.visibility &&
                        old.token.total == new.token.total &&
                        old.token.totalInUsd == new.token.totalInUsd
                old is Hidden && new is Hidden ->
                    old.token.publicKey == new.token.publicKey &&
                        old.token.mintAddress == new.token.mintAddress &&
                        old.token.visibility == new.token.visibility &&
                        old.token.total == new.token.total &&
                        old.token.totalInUsd == new.token.totalInUsd
                old is Action && new is Action ->
                    old.state == new.state
                old is Banners && new is Banners ->
                    old.banners.size == new.banners.size
                old is Title && new is Title ->
                    old.titleResId == new.titleResId
                else -> false
            }
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]

            val fields = mutableSetOf<String>()

            when {
                old is Shown && new is Shown -> {
                    if (old.token.total != new.token.total) fields.add(DIFF_FIELD_TOKEN_BALANCE)
                }
                old is Hidden && new is Hidden -> {
                    if (old.token.total != new.token.total) fields.add(DIFF_FIELD_HIDDEN_TOKEN_BALANCE)
                }
                old is Action && new is Action -> {
                    if (old.state != new.state) fields.add(DIFF_FIELD_TOGGLE_BUTTON)
                }
                old is Title && new is Title -> {
                    if (old.titleResId != new.titleResId) fields.add(DIFF_FIELD_TITLE)
                }
            }

            return if (fields.isEmpty()) super.getChangePayload(oldItemPosition, newItemPosition) else fields
        }
    }
}
