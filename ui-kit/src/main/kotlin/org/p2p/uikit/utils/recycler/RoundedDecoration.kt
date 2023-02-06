package org.p2p.uikit.utils.recycler

import android.graphics.Canvas
import android.view.View
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.uikit.utils.rippleBackground
import org.p2p.uikit.utils.shapeBottomRounded
import org.p2p.uikit.utils.shapeOutline
import org.p2p.uikit.utils.shapeRectangle
import org.p2p.uikit.utils.shapeRoundedAll
import org.p2p.uikit.utils.shapeTopRounded

private const val DEFAULT_ROUNDING_GROUP = "RoundedItem"

interface RoundedItem {
    fun needDecorate(): Boolean = true
    fun roundingGroup(): String = DEFAULT_ROUNDING_GROUP
}

class RoundedDecoration(roundDp: Float) : RecyclerView.ItemDecoration() {

    private val shapeTopRounded: ShapeAppearanceModel = shapeTopRounded(roundDp)
    private val shapeRounded: ShapeAppearanceModel = shapeRoundedAll(roundDp)
    private val shapeBottomRounded: ShapeAppearanceModel = shapeBottomRounded(roundDp)
    private val shapeRectangle: ShapeAppearanceModel = shapeRectangle()

    private fun RoundedItem?.noNeedToDecorate(): Boolean {
        return this == null || !this.needDecorate()
    }

    private fun RoundedItemAdapterInterface.tryGetItemHash(position: Int): String? {
        return getRoundedItem(position).getItemRoundingGroup()
    }

    private fun RoundedItem?.getItemRoundingGroup(): String? {
        return this?.roundingGroup()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.forEach {
            roundItem(it, parent)
        }
    }

    private fun roundItem(view: View, recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter as? RoundedItemAdapterInterface ?: return
        val viewHolder = recyclerView.getChildViewHolder(view)

        val currentHolderItem = adapter.getRoundedItem(viewHolder.bindingAdapterPosition)
        if (currentHolderItem.noNeedToDecorate()) return

        val previousHolderRoundingGroup = adapter.tryGetItemHash(viewHolder.bindingAdapterPosition - 1)
        val currentHolderRoundingGroup = currentHolderItem.getItemRoundingGroup()
        val nextHolderRoundingGroup = adapter.tryGetItemHash(viewHolder.bindingAdapterPosition + 1)

        val shape = selectShape(
            previousHolderRoundingGroup = previousHolderRoundingGroup,
            currentHolderRoundingGroup = currentHolderRoundingGroup,
            nextHolderRoundingGroup = nextHolderRoundingGroup,
        )

        if (viewHolder.itemView.foreground == null) {
            viewHolder.itemView.rippleBackground(shape)
        } else {
            viewHolder.itemView.shapeOutline(shape)
        }
    }

    private fun selectShape(
        previousHolderRoundingGroup: String?,
        currentHolderRoundingGroup: String?,
        nextHolderRoundingGroup: String?,
    ): ShapeAppearanceModel {
        return when {
            previousHolderRoundingGroup != currentHolderRoundingGroup &&
                currentHolderRoundingGroup != nextHolderRoundingGroup -> shapeRounded
            previousHolderRoundingGroup != currentHolderRoundingGroup &&
                currentHolderRoundingGroup == nextHolderRoundingGroup -> shapeTopRounded
            previousHolderRoundingGroup == currentHolderRoundingGroup &&
                currentHolderRoundingGroup != nextHolderRoundingGroup -> shapeBottomRounded
            previousHolderRoundingGroup == currentHolderRoundingGroup &&
                currentHolderRoundingGroup == nextHolderRoundingGroup -> shapeRectangle
            else -> shapeRectangle
        }
    }
}
