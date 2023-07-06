package org.p2p.wallet.jupiter.ui.settings.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import android.graphics.Rect
import android.view.View
import com.hannesdorfmann.adapterdelegates4.dsl.AdapterDelegateViewBindingViewHolder
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.jupiter.ui.settings.presenter.SwapSettingsPayload.MINIMUM_RECEIVED

class SwapSettingsDecorator : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val viewHolder =
            (parent.findContainingViewHolder(view) as? AdapterDelegateViewBindingViewHolder<*, *>) ?: return
        val item = viewHolder.item ?: return
        when {
            item is MainCellModel && item.payload == MINIMUM_RECEIVED ->
                outRect.set(0, 20.toPx(), 0, 0)
            item is SectionHeaderCellModel ->
                outRect.set(0, 20.toPx(), 0, 0)
        }
    }
}
