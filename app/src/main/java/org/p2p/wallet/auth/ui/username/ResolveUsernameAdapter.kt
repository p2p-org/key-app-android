package org.p2p.wallet.auth.ui.username

import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.auth.model.ResolveUsername

class ResolveUsernameAdapter(
    private val onItemClicked: (ResolveUsername) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<ResolveUsername>()

    fun setItems(names: List<ResolveUsername>) {
        data.clear()
        data.addAll(names)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ResolveUsernameViewHolder(
            parent = parent,
            onItemClicked = onItemClicked
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ResolveUsernameViewHolder).onBind(data[position])
    }

    override fun getItemCount(): Int = data.size
}