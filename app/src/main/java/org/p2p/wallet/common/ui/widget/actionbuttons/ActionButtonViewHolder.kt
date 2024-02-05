package org.p2p.wallet.common.ui.widget.actionbuttons

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import org.p2p.wallet.databinding.ItemHomeButtonBinding
import org.p2p.wallet.utils.toPx
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class ActionButtonViewHolder(
    parent: ViewGroup,
    private val onButtonClicked: (ActionButton) -> Unit,
    private val itemsCount: Int,
    private val binding: ItemHomeButtonBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    private val displayMetrics = binding.context.resources.displayMetrics
    private val screenWidthMinusMargins = displayMetrics.widthPixels - (16.toPx() * 2)

    fun onBind(actionButton: ActionButton, position: Int) {
        binding.root.id = actionButton.viewId // for UI testing

        adjustMarginsForLowRes(position)

        binding.textViewTitle.setText(actionButton.textRes)
        binding.imageButtonAction.setImageResource(actionButton.iconRes)
        binding.imageButtonAction.setOnClickListener { onButtonClicked.invoke(actionButton) }
    }

    /**
     * Handling case, when display resolution is scaled down (dynamic resolution feature)
     * and we don't have enough space to fit all buttons on the screen.
     * This logic takes into account that by design, our buttons are centered on the screen,
     * so when buttons can fit on the screen, we don't change original layout.
     */
    private fun adjustMarginsForLowRes(position: Int) {
        val lp = binding.root.layoutParams as MarginLayoutParams
        val requiredSpace = ((lp.width + lp.leftMargin + lp.rightMargin) * itemsCount)

        // use "widthPixels" because there can be a case when difference
        // between requiredSpace and widthPixels is around 2 pixels (float conversions loss?)
        if (requiredSpace > displayMetrics.widthPixels) {
            if (position == itemsCount - 1) {
                lp.leftMargin = 0
                lp.rightMargin = 0
            } else {
                val totalMargin = screenWidthMinusMargins - (lp.width * itemsCount)
                val marginPerItem = totalMargin / (itemsCount - 1)
                lp.leftMargin = 0
                lp.rightMargin = marginPerItem
            }
            binding.root.layoutParams = lp
        }
    }
}
