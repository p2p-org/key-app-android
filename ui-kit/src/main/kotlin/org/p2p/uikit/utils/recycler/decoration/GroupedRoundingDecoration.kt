package org.p2p.uikit.utils.recycler.decoration

import androidx.annotation.Px
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import android.graphics.Canvas
import android.view.View
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.reflect.KClass
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeBottomRounded
import org.p2p.uikit.utils.drawable.shape.shapeOutline
import org.p2p.uikit.utils.drawable.shape.shapeRectangle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shape.shapeTopRounded
import org.p2p.uikit.utils.recycler.getItems
import org.p2p.uikit.utils.toPx

fun groupedRoundingMainCellDecoration(@Px round: Float = 16f.toPx()) =
    GroupedRoundingDecoration(MainCellModel::class, round)

class GroupedRoundingDecoration(
    private val itemCellType: KClass<out AnyCellItem>,
    @Px round: Float = 16f.toPx(),
) : ItemDecoration() {

    private val shapeTopRounded: ShapeAppearanceModel = shapeTopRounded(round)
    private val shapeRounded: ShapeAppearanceModel = shapeRoundedAll(round)
    private val shapeBottomRounded: ShapeAppearanceModel = shapeBottomRounded(round)
    private val shapeRectangle: ShapeAppearanceModel = shapeRectangle()

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.forEach {
            roundItem(view = it, parent = parent)
        }
    }

    fun roundItem(view: View, parent: RecyclerView) {
        val adapter = parent.adapter ?: return
        val viewHolder = parent.getChildViewHolder(view) ?: return
        val items = adapter.getItems()
        val position = parent.getChildAdapterPosition(view)
        val item = items.getOrNull(position) ?: return
        if (item::class != itemCellType) return

        val previousItem = items.getOrNull(position - 1)
        val nextItem = items.getOrNull(position + 1)

        val shape = selectShape(
            previous = previousItem,
            current = item,
            next = nextItem,
        )

        viewHolder.itemView.apply {
            if (foreground == null) {
                rippleForeground(shape)
            } else {
                shapeOutline(shape)
            }
        }
    }

    private fun selectShape(
        previous: AnyCellItem?,
        current: AnyCellItem,
        next: AnyCellItem?,
    ): ShapeAppearanceModel {
        val currentClass = current::class
        val previousClass = previous?.let { previous::class }
        val nextClass = next?.let { next::class }
        return when {
            previousClass != currentClass &&
                currentClass != nextClass -> shapeRounded
            previousClass != currentClass &&
                currentClass == nextClass -> shapeTopRounded
            previousClass == currentClass &&
                currentClass != nextClass -> shapeBottomRounded
            previousClass == currentClass &&
                currentClass == nextClass -> shapeRectangle
            else -> shapeRectangle
        }
    }
}
