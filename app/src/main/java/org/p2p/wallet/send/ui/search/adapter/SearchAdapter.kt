package org.p2p.wallet.send.ui.search.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.send.model.SearchResult

class SearchAdapter(
    private val onItemClicked: (SearchResult) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<SearchResult>()

    /* TODO: Will add diff later */
    @SuppressLint("NotifyDataSetChanged")
    fun setItems(results: List<SearchResult>) {
        data.clear()
        data.addAll(results)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SearchViewHolder(
            parent = parent,
            onItemClicked = onItemClicked
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SearchViewHolder).onBind(data[position])
    }
}
