package org.p2p.uikit.utils.recycler.decoration

import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import android.graphics.Rect
import android.view.View
import org.p2p.uikit.utils.recycler.getItems
import org.p2p.uikit.utils.toPx

fun topOffsetDifferentClassDecoration(offset: Int = 8.toPx()) = TopOffsetDifferentClassDecoration(offset)

class TopOffsetDifferentClassDecoration(
    @Px private val offset: Int = 8.toPx(),
) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter ?: return
        val items = adapter.getItems()
        val position = parent.getChildAdapterPosition(view)
        val item = items.getOrNull(position) ?: return
        val itemClass = item::class

        val previousItem = items.getOrNull(position - 1) ?: return
        if (previousItem::class != itemClass) {
            outRect.set(0, offset, 0, 0)
        }
    }
}
