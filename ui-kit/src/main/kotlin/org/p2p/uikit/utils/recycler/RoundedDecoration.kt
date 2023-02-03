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

interface RoundedItem {
    fun needDecorate(): Boolean
    fun roundingHash(): String
}

class RoundedDecoration(roundDp: Float) : RecyclerView.ItemDecoration() {

    private val shapeTopRounded: ShapeAppearanceModel = shapeTopRounded(roundDp)
    private val shapeRounded: ShapeAppearanceModel = shapeRoundedAll(roundDp)
    private val shapeBottomRounded: ShapeAppearanceModel = shapeBottomRounded(roundDp)
    private val shapeRectangle: ShapeAppearanceModel = shapeRectangle()

    private fun RoundedItem?.noNeedToDecorate(): Boolean {
        return this?.needDecorate() != true
    }

    private fun RoundedItemAdapterInterface.tryGetItemHash(position: Int): String {
        return getRoundedItem(position).getItemHash()
    }

    private fun RoundedItem?.getItemHash(): String {
        return this?.roundingHash().toString()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.forEach {
            roundItem(it, parent)
        }
    }

    private fun roundItem(view: View, recyclerView: RecyclerView) {
        val viewHolder = recyclerView.getChildViewHolder(view)
        val adapter = recyclerView.adapter as? RoundedItemAdapterInterface ?: return

        val currentHolderItem = adapter.getRoundedItem(viewHolder.absoluteAdapterPosition)
        if (currentHolderItem.noNeedToDecorate()) return

        val previousHolderItemHash = adapter.tryGetItemHash(viewHolder.absoluteAdapterPosition - 1)
        val currentHolderItemHash = currentHolderItem.getItemHash()
        val nextHolderItemHash = adapter.tryGetItemHash(viewHolder.absoluteAdapterPosition + 1)

        val shape = selectShape(
            previousHolderItemHash = previousHolderItemHash,
            currentHolderItemHash = currentHolderItemHash,
            nextHolderItemHash = nextHolderItemHash,
        )

        if (viewHolder.itemView.foreground == null) {
            viewHolder.itemView.rippleBackground(shape)
        } else {
            viewHolder.itemView.shapeOutline(shape)
        }
    }

    private fun selectShape(
        previousHolderItemHash: String,
        currentHolderItemHash: String,
        nextHolderItemHash: String,
    ): ShapeAppearanceModel {
        return when {
            previousHolderItemHash != currentHolderItemHash &&
                currentHolderItemHash != nextHolderItemHash -> shapeRounded
            previousHolderItemHash != currentHolderItemHash &&
                currentHolderItemHash == nextHolderItemHash -> shapeTopRounded
            previousHolderItemHash == currentHolderItemHash &&
                currentHolderItemHash != nextHolderItemHash -> shapeBottomRounded
            previousHolderItemHash == currentHolderItemHash &&
                currentHolderItemHash == nextHolderItemHash -> shapeRectangle
            else -> shapeRectangle
        }
    }
}
