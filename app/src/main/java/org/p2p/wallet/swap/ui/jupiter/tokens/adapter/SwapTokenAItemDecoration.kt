package org.p2p.wallet.swap.ui.jupiter.tokens.adapter

import androidx.annotation.Px
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Canvas
import android.view.View
import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.uikit.components.finance_block.FinanceBlockViewHolder
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeBottomRounded
import org.p2p.uikit.utils.drawable.shape.shapeOutline
import org.p2p.uikit.utils.drawable.shape.shapeRectangle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shape.shapeTopRounded
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel

class SwapTokenAItemDecoration(
    @Px round: Float = 12f.toPx()
) : RecyclerView.ItemDecoration() {

    private val shapeTopRounded: ShapeAppearanceModel = shapeTopRounded(round)
    private val shapeRounded: ShapeAppearanceModel = shapeRoundedAll(round)
    private val shapeBottomRounded: ShapeAppearanceModel = shapeBottomRounded(round)
    private val shapeRectangle: ShapeAppearanceModel = shapeRectangle()

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.forEach {
            roundItem(it, parent)
        }
    }

    private fun roundItem(view: View, recyclerView: RecyclerView) {
        val viewHolder = recyclerView.getChildViewHolder(view) as? FinanceBlockViewHolder ?: return
        val adapterPosition = viewHolder.bindingAdapterPosition
        val token = viewHolder.item.payload as? SwapTokenModel ?: return

        val previousViewHolder = recyclerView.findViewHolderForAdapterPosition(adapterPosition - 1)
        val nextViewHolder = recyclerView.findViewHolderForAdapterPosition(adapterPosition + 1)

        val shape = when (token) {
            is SwapTokenModel.JupiterToken -> selectShapeForJupiterToken(
                previousItem = previousViewHolder,
                nextItem = nextViewHolder,
            )
            is SwapTokenModel.UserToken -> selectShapeForUserToken(
                previousItem = previousViewHolder,
                nextItem = nextViewHolder,
            )
        }

        if (viewHolder.itemView.foreground == null) {
            viewHolder.itemView.rippleForeground(shape)
        } else {
            viewHolder.itemView.shapeOutline(shape)
        }
    }

    private fun selectShapeForUserToken(
        previousItem: RecyclerView.ViewHolder?,
        nextItem: RecyclerView.ViewHolder?,
    ): ShapeAppearanceModel {
        val previousToken = (previousItem as? FinanceBlockViewHolder)?.item?.payload as? SwapTokenModel.UserToken
        val nextToken = (nextItem as? FinanceBlockViewHolder)?.item?.payload as? SwapTokenModel.UserToken

        return when {
            previousToken !is SwapTokenModel.UserToken &&
                nextToken !is SwapTokenModel.UserToken -> shapeRounded
            previousToken !is SwapTokenModel.UserToken &&
                nextToken is SwapTokenModel.UserToken -> shapeTopRounded
            previousToken is SwapTokenModel.UserToken &&
                nextToken !is SwapTokenModel.UserToken -> shapeBottomRounded
            previousToken is SwapTokenModel.UserToken &&
                nextToken is SwapTokenModel.UserToken -> shapeRectangle
            else -> shapeRectangle
        }
    }

    private fun selectShapeForJupiterToken(
        previousItem: RecyclerView.ViewHolder?,
        nextItem: RecyclerView.ViewHolder?,
    ): ShapeAppearanceModel {
        val previousToken = (previousItem as? FinanceBlockViewHolder)?.item?.payload as? SwapTokenModel.JupiterToken
        val nextToken = (nextItem as? FinanceBlockViewHolder)?.item?.payload as? SwapTokenModel.JupiterToken

        return when {
            previousToken !is SwapTokenModel.JupiterToken &&
                nextToken !is SwapTokenModel.JupiterToken -> shapeRounded
            previousToken !is SwapTokenModel.JupiterToken &&
                nextToken is SwapTokenModel.JupiterToken -> shapeTopRounded
            previousToken is SwapTokenModel.JupiterToken &&
                nextToken !is SwapTokenModel.JupiterToken -> shapeBottomRounded
            previousToken is SwapTokenModel.JupiterToken &&
                nextToken is SwapTokenModel.JupiterToken -> shapeRectangle
            else -> shapeRectangle
        }
    }
}
