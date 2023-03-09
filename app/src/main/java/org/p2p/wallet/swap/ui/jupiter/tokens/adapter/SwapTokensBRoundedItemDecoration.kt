package org.p2p.wallet.swap.ui.jupiter.tokens.adapter

import androidx.annotation.Px
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.finance_block.FinanceBlockViewHolder
import org.p2p.uikit.components.finance_block.asFinanceCell
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeBottomRounded
import org.p2p.uikit.utils.drawable.shape.shapeOutline
import org.p2p.uikit.utils.drawable.shape.shapeRectangle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shape.shapeTopRounded
import org.p2p.uikit.utils.recycler.getItems
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensCellModelPayload

class SwapTokensBRoundedItemDecoration(
    @Px round: Float = 12f.toPx()
) : RecyclerView.ItemDecoration() {

    private val shapeTopRounded: ShapeAppearanceModel = shapeTopRounded(round)
    private val shapeRounded: ShapeAppearanceModel = shapeRoundedAll(round)
    private val shapeBottomRounded: ShapeAppearanceModel = shapeBottomRounded(round)
    private val shapeRectangle: ShapeAppearanceModel = shapeRectangle()

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val currentViewHolder = parent.getChildViewHolder(view) as? FinanceBlockViewHolder ?: return
        val currentItemPayload = currentViewHolder.asFinanceCell
            ?.getPayload<SwapTokensCellModelPayload>()
            ?: return

        // no need for paddings if we search tokens
        if (currentItemPayload.isSearchResultItem) {
            super.getItemOffsets(outRect, view, parent, state)
            return
        }

        val adapterPosition = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter ?: return
        val items = adapter.getItems()
        val previousCell = items.getOrNull(adapterPosition - 1) as? FinanceBlockCellModel

        val shouldCheckForBottomPadding = !currentItemPayload.hasPopularLabel

        if (shouldCheckForBottomPadding) {
            addTopPaddingToNotPopularGroup(
                outRect = outRect,
                previousCell = previousCell,
            )
        }
    }

    private fun addTopPaddingToNotPopularGroup(
        outRect: Rect,
        previousCell: FinanceBlockCellModel?,
    ) {
        val prevTokenPayload = previousCell?.payload as? SwapTokensCellModelPayload
        val isPopularGroupFinished = prevTokenPayload?.hasPopularLabel == true
        if (isPopularGroupFinished) {
            outRect.top = 30
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.forEach {
            roundItem(view = it, recyclerView = parent)
        }
    }

    private fun roundItem(view: View, recyclerView: RecyclerView) {
        val viewHolder = recyclerView.getChildViewHolder(view) as? FinanceBlockViewHolder ?: return
        val payload = viewHolder.getPayload<SwapTokensCellModelPayload>()

        val adapterPosition = viewHolder.layoutPosition
        val previousViewHolder = recyclerView.findViewHolderForLayoutPosition(adapterPosition - 1)
        val nextViewHolder = recyclerView.findViewHolderForAdapterPosition(adapterPosition + 1)

        val shape = when {
            payload.hasPopularLabel -> selectShapeForPopularToken(
                previousItem = previousViewHolder,
                nextItem = nextViewHolder,
            )
            else -> selectShapeForOtherToken(
                previousItem = previousViewHolder,
                nextItem = nextViewHolder,
            )
        }

        viewHolder.itemView.apply {
            if (foreground == null) {
                rippleForeground(shape)
            } else {
                shapeOutline(shape)
            }
        }
    }

    private fun selectShapeForOtherToken(
        previousItem: RecyclerView.ViewHolder?,
        nextItem: RecyclerView.ViewHolder?,
    ): ShapeAppearanceModel {
        val previousPayload = previousItem.asFinanceCell
            ?.getPayload<SwapTokensCellModelPayload>()
        val nextPayload = nextItem.asFinanceCell
            ?.getPayload<SwapTokensCellModelPayload>()

        val previousToken = previousPayload?.tokenModel
        val nextToken = nextPayload?.tokenModel

        val isChosenTokenGroup =
            previousToken !is SwapTokenModel &&
                nextToken !is SwapTokenModel

        val isOtherTokensGroupStarted =
            previousPayload?.hasPopularLabel == true &&
                nextPayload?.hasPopularLabel == false

        val isOtherTokensGroupFinished =
            previousToken is SwapTokenModel &&
                nextToken !is SwapTokenModel

        return when {
            isChosenTokenGroup -> shapeRounded
            isOtherTokensGroupStarted -> shapeTopRounded
            isOtherTokensGroupFinished -> shapeBottomRounded
            else -> shapeRectangle
        }
    }

    private fun selectShapeForPopularToken(
        previousItem: RecyclerView.ViewHolder?,
        nextItem: RecyclerView.ViewHolder?,
    ): ShapeAppearanceModel {
        val previousItemPayload = previousItem.asFinanceCell
            ?.getPayload<SwapTokensCellModelPayload>()
        val nextItemPayload = nextItem.asFinanceCell
            ?.getPayload<SwapTokensCellModelPayload>()

        val isPopularTokensGroupStarted =
            previousItem !is FinanceBlockViewHolder &&
                nextItemPayload?.hasPopularLabel == true

        val isPopularTokensGroupFinished =
            previousItemPayload?.hasPopularLabel == true &&
                nextItemPayload?.hasPopularLabel == false

        return when {
            isPopularTokensGroupStarted -> shapeTopRounded
            isPopularTokensGroupFinished -> shapeBottomRounded
            else -> shapeRectangle
        }
    }
}
