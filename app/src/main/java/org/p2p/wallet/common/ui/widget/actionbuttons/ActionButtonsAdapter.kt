package org.p2p.wallet.common.ui.widget.actionbuttons

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

class ActionButtonsAdapter(
    private val onButtonClicked: (ActionButton) -> Unit
) : RecyclerView.Adapter<ActionButtonViewHolder>() {

    private val buttons = mutableListOf<ActionButton>()

    fun setItems(newItems: List<ActionButton>) {
        buttons.clear()
        buttons += newItems
        notifyItemInserted(buttons.size)
    }

    override fun getItemCount(): Int = buttons.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionButtonViewHolder =
        ActionButtonViewHolder(
            parent = parent,
            onButtonClicked = onButtonClicked,
            itemsCount = buttons.size
        )

    override fun onBindViewHolder(holder: ActionButtonViewHolder, position: Int) {
        holder.onBind(buttons[position], position)
    }
}
