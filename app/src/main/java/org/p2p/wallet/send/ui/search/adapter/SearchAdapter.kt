package org.p2p.wallet.send.ui.search.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.send.model.SearchResult

class SearchAdapter(
    private val onItemClicked: (SearchResult) -> Unit,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<SearchResult>()

    private class SearchAdapterDiffUtil(
        private val oldList: List<SearchResult>,
        private val newList: List<SearchResult>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].address == newList[newItemPosition].address
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return when {
                oldItem is SearchResult.UsernameFound && newItem is SearchResult.UsernameFound ->
                    oldItem.username == newItem.username && oldItem.date == newItem.date
                oldItem is SearchResult.AddressFound && newItem is SearchResult.AddressFound ->
                    oldItem.address == newItem.address && oldItem.date == newItem.date
                else ->
                    oldItem == newItem
            }
        }
    }

    fun setItems(results: List<SearchResult>) {
        val diffResult = DiffUtil.calculateDiff(SearchAdapterDiffUtil(oldList = data, newList = results))
        data.clear()
        data.addAll(results)
        diffResult.dispatchUpdatesTo(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearItems() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is SearchResult.InvalidDirectAddress,
        is SearchResult.OwnAddressError -> R.layout.item_search_invalid_result
        is SearchResult.AddressFound,
        is SearchResult.UsernameFound -> R.layout.item_search
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_search_invalid_result -> SearchErrorViewHolder(
                parent = parent
            )
            else -> SearchViewHolder(
                parent = parent,
                onItemClicked = onItemClicked,
                usernameDomainFeatureToggle = usernameDomainFeatureToggle
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchViewHolder -> holder.onBind(data[position])
            is SearchErrorViewHolder -> holder.onBind(data[position])
        }
    }
}
