package org.p2p.wallet.home.ui.new.adapter

import androidx.recyclerview.widget.DiffUtil
import org.p2p.wallet.home.model.SelectTokenItem
import org.p2p.wallet.home.ui.new.adapter.NewSelectTokenAdapter.Companion.DIFF_FIELD_ROUND_STATE

class NewTokenAdapterDiffCallback(
    private val oldList: List<SelectTokenItem>,
    private val newList: List<SelectTokenItem>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return when {
            old is SelectTokenItem.SelectableToken && new is SelectTokenItem.SelectableToken -> {
                old.token.publicKey == new.token.publicKey &&
                    old.state == new.state &&
                    old.token.total == new.token.total
            }
            old is SelectTokenItem.CategoryTitle && new is SelectTokenItem.CategoryTitle ->
                old.titleRes == new.titleRes
            else -> old == new
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return when {
            old is SelectTokenItem.SelectableToken && new is SelectTokenItem.SelectableToken -> {
                old.token.publicKey == new.token.publicKey &&
                    old.state == new.state &&
                    old.token.total == new.token.total
            }
            old is SelectTokenItem.CategoryTitle && new is SelectTokenItem.CategoryTitle ->
                old.titleRes == new.titleRes
            else -> false
        }
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        val fields = mutableSetOf<String>()

        when {
            old is SelectTokenItem.SelectableToken && new is SelectTokenItem.SelectableToken -> {
                if (old.state != new.state) fields.add(DIFF_FIELD_ROUND_STATE)
            }
        }

        return if (fields.isEmpty()) super.getChangePayload(oldItemPosition, newItemPosition) else fields
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size
}
