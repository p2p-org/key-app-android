package org.p2p.uikit.utils.recycler

import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import android.view.View
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.uikit.utils.rippleBackground
import org.p2p.uikit.utils.shapeBottomRounded
import org.p2p.uikit.utils.shapeRectangle
import org.p2p.uikit.utils.shapeRoundedAll
import org.p2p.uikit.utils.shapeTopRounded

interface RoundItem {
    fun needDecorate(): Boolean
}

class RoundedDecoration(roundDp: Float) : RecyclerView.ItemDecoration() {

    private val shapeTopRounded: ShapeAppearanceModel = shapeTopRounded(roundDp)
    private val shapeRounded: ShapeAppearanceModel = shapeRoundedAll(roundDp)
    private val shapeBottomRounded: ShapeAppearanceModel = shapeBottomRounded(roundDp)
    private val shapeRectangle: ShapeAppearanceModel = shapeRectangle()

    private fun RecyclerView.ViewHolder.noNeedToDecorate(): Boolean {
        return !(this is RoundItem && this.needDecorate())
    }

    private fun RecyclerView.Adapter<*>.tryGetViewType(position: Int): Int {
        return try {
            getItemViewType(position)
        } catch (e: Exception) {
            RecyclerView.INVALID_TYPE
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, recyclerView: RecyclerView, state: RecyclerView.State) {
        val viewHolder = recyclerView.getChildViewHolder(view)
        if (viewHolder.noNeedToDecorate()) return
        val adapter = recyclerView.adapter ?: return

        val previousHolderItemType = adapter.tryGetViewType(viewHolder.bindingAdapterPosition - 1)
        val currentHolderItemType = adapter.getItemViewType(viewHolder.bindingAdapterPosition)
        val nextHolderItemType = adapter.tryGetViewType(viewHolder.bindingAdapterPosition + 1)

        viewHolder.itemView.rippleBackground(
            selectShape(
                previousHolderItemType = previousHolderItemType,
                currentHolderItemType = currentHolderItemType,
                nextHolderItemType = nextHolderItemType,
            )
        )
    }

    private fun selectShape(
        previousHolderItemType: Int,
        currentHolderItemType: Int,
        nextHolderItemType: Int,
    ): ShapeAppearanceModel {

        return when {
            previousHolderItemType != currentHolderItemType &&
                currentHolderItemType != nextHolderItemType -> shapeRounded
            previousHolderItemType != currentHolderItemType &&
                currentHolderItemType == nextHolderItemType -> shapeTopRounded
            previousHolderItemType == currentHolderItemType &&
                currentHolderItemType != nextHolderItemType -> shapeBottomRounded
            previousHolderItemType == currentHolderItemType &&
                currentHolderItemType == nextHolderItemType -> shapeRectangle
            else -> shapeRectangle
        }
    }
}
