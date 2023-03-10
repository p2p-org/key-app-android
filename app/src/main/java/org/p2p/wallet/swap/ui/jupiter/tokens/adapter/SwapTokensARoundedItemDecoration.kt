package org.p2p.wallet.swap.ui.jupiter.tokens.adapter
import androidx.annotation.Px
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Canvas
import android.view.View
import com.google.android.material.shape.ShapeAppearanceModel
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

class SwapTokensARoundedItemDecoration(
    @Px round: Float = 16f.toPx()
) : RecyclerView.ItemDecoration() {

    private val shapeTopRounded: ShapeAppearanceModel = shapeTopRounded(round)
    private val shapeRounded: ShapeAppearanceModel = shapeRoundedAll(round)
    private val shapeBottomRounded: ShapeAppearanceModel = shapeBottomRounded(round)
    private val shapeRectangle: ShapeAppearanceModel = shapeRectangle()

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.forEach {
            roundItem(view = it, recyclerView = parent)
        }
    }

    private fun roundItem(view: View, recyclerView: RecyclerView) {
        val viewHolder = recyclerView.getChildViewHolder(view).asFinanceCell ?: return
        val payload = viewHolder.getPayloadOrNull<SwapTokensCellModelPayload>() ?: return

        val adapterPosition = viewHolder.bindingAdapterPosition
        val previousViewHolder = recyclerView.findViewHolderForAdapterPosition(adapterPosition - 1)
        val nextViewHolder = recyclerView.findViewHolderForAdapterPosition(adapterPosition + 1)

        val shape = when {
            payload.isSearchResultItem -> selectShapeForSearchResultItem(
                previousItem = previousViewHolder,
                nextItem = nextViewHolder
            )
            payload.tokenModel is SwapTokenModel.JupiterToken -> selectShapeForJupiterToken(
                previousItem = previousViewHolder,
                nextItem = nextViewHolder,
            )
            payload.tokenModel is SwapTokenModel.UserToken -> selectShapeForUserToken(
                previousItem = previousViewHolder,
                nextItem = nextViewHolder,
            )
            else -> return
        }

        viewHolder.itemView.apply {
            if (foreground == null) {
                rippleForeground(shape)
            } else {
                shapeOutline(shape)
            }
        }
    }

    private fun selectShapeForSearchResultItem(
        previousItem: RecyclerView.ViewHolder?,
        nextItem: RecyclerView.ViewHolder?,
    ): ShapeAppearanceModel {
        val previousToken = previousItem.asFinanceCell
            ?.getPayload<SwapTokensCellModelPayload>()
            ?.tokenModel
        val nextToken = nextItem.asFinanceCell
            ?.getPayload<SwapTokensCellModelPayload>()
            ?.tokenModel

        val isSearchResultStarted =
            previousToken !is SwapTokenModel && nextToken is SwapTokenModel
        val isSearchResultEnded =
            previousToken is SwapTokenModel && nextToken !is SwapTokenModel
        val isSearchResultSingle =
            previousToken !is SwapTokenModel && nextToken !is SwapTokenModel
        return when {
            isSearchResultSingle -> shapeRounded
            isSearchResultStarted -> shapeTopRounded
            isSearchResultEnded -> shapeBottomRounded
            else -> shapeRectangle
        }
    }

    private fun selectShapeForUserToken(
        previousItem: RecyclerView.ViewHolder?,
        nextItem: RecyclerView.ViewHolder?,
    ): ShapeAppearanceModel {
        val previousToken = previousItem.asFinanceCell
            ?.getPayloadOrNull<SwapTokensCellModelPayload>()
            ?.tokenModel
        val nextToken = nextItem.asFinanceCell
            ?.getPayloadOrNull<SwapTokensCellModelPayload>()
            ?.tokenModel

        val isChosenTokenGroup =
            previousToken !is SwapTokenModel.UserToken &&
                nextToken !is SwapTokenModel.UserToken

        val isUserTokensGroupStarted =
            previousToken !is SwapTokenModel.UserToken &&
                nextToken is SwapTokenModel.UserToken

        val isUserTokensGroupFinished =
            previousToken is SwapTokenModel.UserToken &&
                nextToken !is SwapTokenModel.UserToken

        return when {
            isChosenTokenGroup -> shapeRounded
            isUserTokensGroupStarted -> shapeTopRounded
            isUserTokensGroupFinished -> shapeBottomRounded
            else -> shapeRectangle
        }
    }

    private fun selectShapeForJupiterToken(
        previousItem: RecyclerView.ViewHolder?,
        nextItem: RecyclerView.ViewHolder?,
    ): ShapeAppearanceModel {
        val previousToken = previousItem.asFinanceCell
            ?.getPayloadOrNull<SwapTokensCellModelPayload>()
            ?.tokenModel
        val nextToken = nextItem.asFinanceCell
            ?.getPayloadOrNull<SwapTokensCellModelPayload>()
            ?.tokenModel

        val isChosenTokenGroup =
            previousToken !is SwapTokenModel.JupiterToken &&
                nextToken !is SwapTokenModel.JupiterToken

        val isOtherTokensGroupStarted =
            previousToken !is SwapTokenModel.JupiterToken &&
                nextToken is SwapTokenModel.JupiterToken

        val isOtherTokensGroupFinished =
            previousToken is SwapTokenModel.JupiterToken &&
                nextToken !is SwapTokenModel.JupiterToken

        return when {
            isChosenTokenGroup -> shapeRounded
            isOtherTokensGroupStarted -> shapeTopRounded
            isOtherTokensGroupFinished -> shapeBottomRounded
            else -> shapeRectangle
        }
    }
}
