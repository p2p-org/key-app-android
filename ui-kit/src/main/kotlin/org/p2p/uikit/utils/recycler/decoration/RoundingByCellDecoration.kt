package org.p2p.uikit.utils.recycler.decoration

import androidx.annotation.Px
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Canvas
import android.view.View
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.reflect.KClass
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeOutline
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.recycler.getItems
import org.p2p.uikit.utils.toPx

fun roundingByCellMainCellDecoration(@Px round: Float = 16f.toPx()) =
    RoundingByCellDecoration(MainCellModel::class, round)

class RoundingByCellDecoration(
    private val itemCellType: KClass<out AnyCellItem>,
    @Px round: Float = 16f.toPx(),
) : RecyclerView.ItemDecoration() {

    private val shapeRounded: ShapeAppearanceModel = shapeRoundedAll(round)

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.forEach {
            roundItem(view = it, parent = parent)
        }
    }

    private fun roundItem(view: View, parent: RecyclerView) {
        val adapter = parent.adapter ?: return
        val viewHolder = parent.getChildViewHolder(view) ?: return
        val items = adapter.getItems()
        val position = parent.getChildAdapterPosition(view)
        val item = items.getOrNull(position) ?: return
        if (item::class != itemCellType) return

        viewHolder.itemView.apply {
            if (foreground == null) {
                rippleForeground(shapeRounded)
            } else {
                shapeOutline(shapeRounded)
            }
        }
    }
}
