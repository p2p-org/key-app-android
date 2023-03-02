package org.p2p.wallet.swap.ui.jupiter.tokens.adapter

import androidx.annotation.Px
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.uikit.components.finance_block.FinanceBlockViewHolder
import org.p2p.uikit.components.finance_block.asFinanceCell
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeBottomRounded
import org.p2p.uikit.utils.drawable.shape.shapeOutline
import org.p2p.uikit.utils.drawable.shape.shapeRectangle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shape.shapeTopRounded
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

        val adapterPosition = currentViewHolder.layoutPosition // fix position, bottom padding added only on scroll
        val previousViewHolder = parent.findViewHolderForLayoutPosition(adapterPosition - 1)
        val nextViewHolder = parent.findViewHolderForLayoutPosition(adapterPosition + 1)

        val shouldCheckForBottomPadding = currentItemPayload.hasPopularLabel
        if (shouldCheckForBottomPadding) {
            addBottomPaddingToPopularGroup(outRect, previousViewHolder, nextViewHolder)
        }
    }

    private fun addBottomPaddingToPopularGroup(
        outRect: Rect,
        previousViewHolder: RecyclerView.ViewHolder?,
        nextViewHolder: RecyclerView.ViewHolder?
    ) {
        val nextTokenPayload = nextViewHolder
            .asFinanceCell
            ?.getPayload<SwapTokensCellModelPayload>()

        val isPopularGroupFinished = nextTokenPayload?.hasPopularLabel == false
        if (isPopularGroupFinished) {
            outRect.bottom = 30
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.forEach {
            roundItem(view = it, recyclerView = parent)
        }
    }

    private fun roundItem(view: View, recyclerView: RecyclerView) {
        val viewHolder = recyclerView.getChildViewHolder(view) as? FinanceBlockViewHolder ?: return
        val adapterPosition = viewHolder.bindingAdapterPosition
        val payload = viewHolder.item.payload as? SwapTokensCellModelPayload ?: return

        val previousViewHolder = recyclerView.findViewHolderForLayoutPosition(adapterPosition - 1)
        val nextViewHolder = recyclerView.findViewHolderForAdapterPosition(adapterPosition + 1)

        val shape = when {
            payload.hasPopularLabel -> selectShapeForPopularToken(
                previousItem = previousViewHolder,
                nextItem = nextViewHolder,
            )
            else -> selectShapeForUserToken(
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

    private fun selectShapeForUserToken(
        previousItem: RecyclerView.ViewHolder?,
        nextItem: RecyclerView.ViewHolder?,
    ): ShapeAppearanceModel {
        val previousToken = getPayloadFromViewHolder(previousItem)?.tokenModel
        val nextToken = getPayloadFromViewHolder(nextItem)?.tokenModel

        val isChosenTokenGroup = previousToken !is SwapTokenModel &&
            nextToken !is SwapTokenModel

        val isOtherTokensGroupStarted =
            previousToken !is SwapTokenModel &&
                nextToken is SwapTokenModel

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
        val previousToken = getPayloadFromViewHolder(previousItem)
        val nextToken = getPayloadFromViewHolder(nextItem)

        val isPopularTokensGroupStarted =
            previousToken?.tokenModel !is SwapTokenModel &&
                nextToken?.hasPopularLabel == true

        val isPopularTokensGroupFinished =
            previousToken?.hasPopularLabel == true &&
                nextToken?.hasPopularLabel == false

        return when {
            isPopularTokensGroupStarted -> shapeTopRounded
            isPopularTokensGroupFinished -> shapeBottomRounded
            else -> shapeRectangle
        }
    }

    private fun getPayloadFromViewHolder(viewHolder: RecyclerView.ViewHolder?): SwapTokensCellModelPayload? {
        return (viewHolder as? FinanceBlockViewHolder)?.getPayload()
    }
}
