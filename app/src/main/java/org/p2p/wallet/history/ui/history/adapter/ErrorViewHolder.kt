package org.p2p.wallet.history.ui.history.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.databinding.ItemErrorBinding

class ErrorViewHolder(
    binding: ItemErrorBinding
) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup) : this(
        ItemErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    private val errorTextView = binding.error
    private val retryButton = binding.retryButton

    fun onBind(errorText: String, onRetry: () -> Unit) {
        errorTextView.text = errorText
        retryButton.setOnClickListener { onRetry() }
    }

    fun onBind(@StringRes errorTextRes: Int, onRetry: () -> Unit) {
        errorTextView.setText(errorTextRes)
        retryButton.setOnClickListener { onRetry() }
    }
}