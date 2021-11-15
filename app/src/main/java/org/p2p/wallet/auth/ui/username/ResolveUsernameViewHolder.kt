package org.p2p.wallet.auth.ui.username

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.auth.model.ResolveUsername
import org.p2p.wallet.databinding.ItemResolveUsernameBinding

class ResolveUsernameViewHolder(
    binding: ItemResolveUsernameBinding,
    private val onItemClicked: (ResolveUsername) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        onItemClicked: (ResolveUsername) -> Unit
    ) : this(
        binding = ItemResolveUsernameBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onItemClicked = onItemClicked
    )

    private val walletImageView: ImageView = binding.walletImageView
    private val usernameTextView: TextView = binding.usernameTextView
    private val addressTextView: TextView = binding.addressTextView

    fun onBind(item: ResolveUsername) {
        usernameTextView.text = item.name
        addressTextView.text = item.owner
        itemView.setOnClickListener { onItemClicked(item) }
    }
}