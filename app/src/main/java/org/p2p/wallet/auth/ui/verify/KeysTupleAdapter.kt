package org.p2p.wallet.auth.ui.verify

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.p2p.wallet.databinding.ItemSecurityKeyBinding

@Deprecated("Old onboarding flow, delete someday")
class KeysTupleAdapter : RecyclerView.Adapter<KeysTupleAdapter.KeyViewHolder>() {

    private val data = mutableListOf<Pair<String, Boolean>>()
    var onItemClicked: ((String) -> Unit)? = null

    fun setItems(new: List<Pair<String, Boolean>>) {
        data.clear()
        data.addAll(new)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = KeyViewHolder(
        ItemSecurityKeyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: KeysTupleAdapter.KeyViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    @Deprecated("Old onboarding flow, delete someday")
    inner class KeyViewHolder(
        binding: ItemSecurityKeyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val nameTextView = binding.nameTextView
        private val indexTextView = binding.indexTextView
        private val container = binding.container

        fun bind(item: Pair<String, Boolean>) {
            val keyName = item.first
            val isKeySelected = item.second

            indexTextView.isVisible = false

            nameTextView.text = keyName
            nameTextView.isSelected = isKeySelected
            container.isSelected = isKeySelected

            itemView.setOnClickListener {
                onItemClicked?.invoke(keyName)
            }
        }
    }
}
