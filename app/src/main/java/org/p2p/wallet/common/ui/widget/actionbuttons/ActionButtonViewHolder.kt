package org.p2p.wallet.common.ui.widget.actionbuttons

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemHomeButtonBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class ActionButtonViewHolder(
    private val parent: ViewGroup,
    private val onButtonClicked: (ActionButton) -> Unit,
    private val binding: ItemHomeButtonBinding = parent.inflateViewBinding(attachToRoot = false)
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(actionButton: ActionButton) {
        when (actionButton) {
            ActionButton.BUY_BUTTON -> {
                binding.textViewTitle.setText(R.string.home_buy)
                binding.imageButtonAction.setImageResource(R.drawable.ic_plus)
            }
            ActionButton.RECEIVE_BUTTON -> {
                binding.textViewTitle.setText(R.string.home_receive)
                binding.imageButtonAction.setImageResource(R.drawable.ic_receive_simple)
            }
            ActionButton.SEND_BUTTON -> {
                binding.textViewTitle.setText(R.string.home_send)
                binding.imageButtonAction.setImageResource(R.drawable.ic_send_medium)
            }
            ActionButton.SWAP_BUTTON -> {
                binding.textViewTitle.setText(R.string.home_swap)
                binding.imageButtonAction.setImageResource(R.drawable.ic_swap_medium)
            }
            ActionButton.SELL_BUTTON -> {
                binding.textViewTitle.setText(R.string.home_sell)
                binding.imageButtonAction.setImageResource(R.drawable.ic_action_sell)
            }
        }

        binding.imageButtonAction.setOnClickListener { onButtonClicked.invoke(actionButton) }
    }
}
