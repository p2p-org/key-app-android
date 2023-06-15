package org.p2p.wallet.home.ui.main.adapter

import androidx.recyclerview.widget.DiffUtil
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.ui.main.adapter.TokenAdapter.Companion.DIFF_FIELD_HIDDEN_TOKEN_BALANCE
import org.p2p.wallet.home.ui.main.adapter.TokenAdapter.Companion.DIFF_FIELD_TITLE
import org.p2p.wallet.home.ui.main.adapter.TokenAdapter.Companion.DIFF_FIELD_TOGGLE_BUTTON
import org.p2p.wallet.home.ui.main.adapter.TokenAdapter.Companion.DIFF_FIELD_TOKEN_BALANCE

class TokenAdapterDiffCallback(
    private val oldList: List<HomeElementItem>,
    private val newList: List<HomeElementItem>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return when {
            old is HomeElementItem.Shown && new is HomeElementItem.Shown ->
                old.token.publicKey == new.token.publicKey
            old is HomeElementItem.Hidden && new is HomeElementItem.Hidden ->
                old.token.publicKey == new.token.publicKey
            old is HomeElementItem.Action && new is HomeElementItem.Action ->
                // should pass in contents compare
                true
            else -> old == new
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return when {
            old is HomeElementItem.Shown && new is HomeElementItem.Shown ->
                old.token.publicKey == new.token.publicKey &&
                    old.token.mintAddress == new.token.mintAddress &&
                    old.token.visibility == new.token.visibility &&
                    old.token.total == new.token.total &&
                    old.token.totalInUsd == new.token.totalInUsd
            old is HomeElementItem.Hidden && new is HomeElementItem.Hidden ->
                old.token.publicKey == new.token.publicKey &&
                    old.token.mintAddress == new.token.mintAddress &&
                    old.token.visibility == new.token.visibility &&
                    old.token.total == new.token.total &&
                    old.token.totalInUsd == new.token.totalInUsd
            old is HomeElementItem.Action && new is HomeElementItem.Action ->
                old.state == new.state
            old is HomeElementItem.Title && new is HomeElementItem.Title ->
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
            old is HomeElementItem.Shown && new is HomeElementItem.Shown -> {
                if (old.token.total != new.token.total) fields.add(DIFF_FIELD_TOKEN_BALANCE)
                if (old.token.totalInUsd != new.token.totalInUsd) fields.add(DIFF_FIELD_TOKEN_BALANCE)
            }
            old is HomeElementItem.Hidden && new is HomeElementItem.Hidden -> {
                if (old.token.total != new.token.total) fields.add(DIFF_FIELD_HIDDEN_TOKEN_BALANCE)
                if (old.token.totalInUsd != new.token.totalInUsd) fields.add(DIFF_FIELD_HIDDEN_TOKEN_BALANCE)
            }
            old is HomeElementItem.Action && new is HomeElementItem.Action -> {
                if (old.state != new.state) fields.add(DIFF_FIELD_TOGGLE_BUTTON)
            }
            old is HomeElementItem.Title && new is HomeElementItem.Title -> {
                if (old.titleResId != new.titleResId) fields.add(DIFF_FIELD_TITLE)
            }
        }

        return if (fields.isEmpty()) super.getChangePayload(oldItemPosition, newItemPosition) else fields
    }
}
