package org.p2p.wallet.send.ui.search.adapter

import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.view.ViewGroup
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.send.model.SearchResult

class SearchAdapter(
    private val onItemClicked: (SearchResult) -> Unit,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle
) : RecyclerView.Adapter<SearchViewHolder>() {

    private val data = mutableListOf<SearchResult>()

    /* TODO: Will add diff later */
    @SuppressLint("NotifyDataSetChanged")
    fun setItems(results: List<SearchResult>) {
        data.clear()
        data.addAll(results)
        notifyDataSetChanged()
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
