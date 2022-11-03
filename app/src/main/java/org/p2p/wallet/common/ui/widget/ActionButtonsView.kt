package org.p2p.wallet.common.ui.widget

import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.wallet.R
import org.p2p.wallet.databinding.LayoutActionButtonsBinding

fun interface ActionButtonsViewClickListener {
    fun onActionButtonClicked(clickedButton: ActionButtonsView.ActionButton)
}

class ActionButtonsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class ActionButton {
        BUY_BUTTON, RECEIVE_BUTTON, SEND_BUTTON, SWAP_BUTTON
    }

    private val binding: LayoutActionButtonsBinding = inflateViewBinding()

    var listener: ActionButtonsViewClickListener? = null

    init {
        with(binding) {
            viewActionBuy.apply {
                textViewButtonTitle.setText(R.string.home_buy)
                imageButtonButtonIcon.setImageResource(R.drawable.ic_plus)
                imageButtonButtonIcon.setOnClickListener {
                    listener?.onActionButtonClicked(ActionButton.BUY_BUTTON)
                }
            }
            viewActionReceive.apply {
                textViewButtonTitle.setText(R.string.home_receive)
                imageButtonButtonIcon.setImageResource(R.drawable.ic_receive_simple)
                imageButtonButtonIcon.setOnClickListener {
                    listener?.onActionButtonClicked(ActionButton.RECEIVE_BUTTON)
                }
            }
            viewActionSend.apply {
                textViewButtonTitle.setText(R.string.home_send)
                imageButtonButtonIcon.setImageResource(R.drawable.ic_send_medium)
                imageButtonButtonIcon.setOnClickListener {
                    listener?.onActionButtonClicked(ActionButton.SEND_BUTTON)
                }
            }
            viewActionSwap.apply {
                textViewButtonTitle.setText(R.string.home_swap)
                imageButtonButtonIcon.setImageResource(R.drawable.ic_swap_medium)
                imageButtonButtonIcon.setOnClickListener {
                    listener?.onActionButtonClicked(ActionButton.SWAP_BUTTON)
                }
            }
        }
    }

    var isVisible: Boolean
        get() = binding.root.isVisible
        set(value) {
            binding.root.isVisible = value
        }

    fun setActionButtonVisible(button: ActionButton, isVisible: Boolean) {
        when (button) {
            ActionButton.BUY_BUTTON -> binding.viewActionBuy.viewContainer.isVisible = isVisible
            ActionButton.RECEIVE_BUTTON -> binding.viewActionReceive.viewContainer.isVisible = isVisible
            ActionButton.SEND_BUTTON -> binding.viewActionSend.viewContainer.isVisible = isVisible
            ActionButton.SWAP_BUTTON -> binding.viewActionSwap.viewContainer.isVisible = isVisible
        }
    }
}
