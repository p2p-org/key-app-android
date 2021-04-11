package com.p2p.wowlet.auth.ui.securitykey.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.databinding.ItemSecretKeyBinding

class PhrasesAdapter : RecyclerView.Adapter<PhrasesAdapter.PhraseViewHolder>() {

    private val data = mutableListOf<String>()

    fun setItems(new: List<String>) {
        data.clear()
        data.addAll(new)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PhraseViewHolder(
            ItemSecretKeyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PhraseViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class PhraseViewHolder(
        binding: ItemSecretKeyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val phraseTextView = binding.phraseTextView

        fun bind(value: String) {
            phraseTextView.text = value
        }
    }
}