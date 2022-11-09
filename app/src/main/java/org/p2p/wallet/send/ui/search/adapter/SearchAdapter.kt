package org.p2p.wallet.send.ui.search.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.send.model.SearchResult

class SearchAdapter(
    private val onItemClicked: (SearchResult) -> Unit,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle
) : RecyclerView.Adapter<SearchViewHolder>() {

    private val data = mutableListOf<SearchResult>()

    private class SearchAdapterDiffUtil(
        private val oldList: List<SearchResult>,
        private val newList: List<SearchResult>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] === newList[newItemPosition]
        }
    }

    fun setItems(results: List<SearchResult>) {
        val diffResult = DiffUtil.calculateDiff(SearchAdapterDiffUtil(oldList = data, newList = results))
        data.clear()
        data.addAll(results)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            parent = parent,
            onItemClicked = onItemClicked,
            usernameDomainFeatureToggle = usernameDomainFeatureToggle
        )
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.onBind(data[position])
    }
}
