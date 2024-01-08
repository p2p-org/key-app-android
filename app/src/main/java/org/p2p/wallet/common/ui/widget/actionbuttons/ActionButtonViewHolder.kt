package org.p2p.wallet.common.ui.widget.actionbuttons

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.wallet.databinding.ItemHomeButtonBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class ActionButtonViewHolder(
    parent: ViewGroup,
    private val onButtonClicked: (ActionButton) -> Unit,
    private val binding: ItemHomeButtonBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(actionButton: ActionButton) {
        binding.root.id = actionButton.viewId // for UI testing

        binding.textViewTitle.setText(actionButton.textRes)
        binding.imageButtonAction.setImageResource(actionButton.iconRes)
        binding.imageButtonAction.setOnClickListener { onButtonClicked.invoke(actionButton) }
    }
}
