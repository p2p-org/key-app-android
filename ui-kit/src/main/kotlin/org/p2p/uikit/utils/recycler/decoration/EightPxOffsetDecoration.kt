package org.p2p.uikit.utils.recycler.decoration

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import android.graphics.Rect
import android.view.View
import kotlin.reflect.KClass
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.recycler.getItems
import org.p2p.uikit.utils.toPx

fun eightOffsetFinanceBlockDecoration() = EightPxOffsetDecoration(FinanceBlockCellModel::class)

class EightPxOffsetDecoration(
    private val itemCellType: KClass<out AnyCellItem>
) : ItemDecoration() {

    private val offset = 8.toPx()

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter ?: return
        val items = adapter.getItems()
        val position = parent.getChildAdapterPosition(view)
        val item = items.getOrNull(position) ?: return
        if (item::class != itemCellType) return

        val previousItem = items.getOrNull(position - 1) ?: return
        if (previousItem::class != itemCellType) return

        outRect.set(0, offset, 0, 0)
    }
}
