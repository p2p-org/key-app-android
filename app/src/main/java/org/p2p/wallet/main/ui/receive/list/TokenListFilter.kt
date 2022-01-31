package org.p2p.wallet.main.ui.receive.list

import android.widget.Filter
import org.p2p.wallet.user.model.TokenData

class TokenListFilter(private val items: MutableList<TokenData>, val block: (List<TokenData>) -> Unit) : Filter() {

    override fun performFiltering(sequence: CharSequence?): FilterResults {
        var result = mutableListOf<TokenData>()
        if (sequence.isNullOrEmpty()) {
            result = items
        } else {
            items.forEach {
                if (it.name.contains(sequence, ignoreCase = true)) {
                    result.add(it)
                }
            }
        }
        val filterResult = FilterResults()
        filterResult.values = result
        return filterResult
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        val newList = results?.values as List<TokenData>
        block(newList)
    }

    fun update(newItems: List<TokenData>) {
        items.clear()
        items.addAll(newItems)
    }
}