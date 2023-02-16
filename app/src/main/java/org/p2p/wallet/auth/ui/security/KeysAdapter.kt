package org.p2p.wallet.auth.ui.security

import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import org.p2p.wallet.databinding.ItemSecurityKeyBinding

@Deprecated("Old onboarding flow, delete someday")
class KeysAdapter : RecyclerView.Adapter<KeysAdapter.KeyViewHolder>() {

    private val data = mutableListOf<String>()

    fun setItems(new: List<String>) {
        data.clear()
        data.addAll(new)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        KeyViewHolder(
            ItemSecurityKeyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: KeyViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    @Deprecated("Old onboarding flow, delete someday")
    inner class KeyViewHolder(
        binding: ItemSecurityKeyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val nameTextView = binding.nameTextView
        private val indexTextView = binding.indexTextView

        @SuppressLint("SetTextI18n")
        fun bind(value: String) {
            indexTextView.text = "${bindingAdapterPosition + 1}. "
            nameTextView.text = value
        }
    }
}
