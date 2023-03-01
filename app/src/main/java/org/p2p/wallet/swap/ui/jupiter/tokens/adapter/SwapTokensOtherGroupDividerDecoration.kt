package org.p2p.wallet.swap.ui.jupiter.tokens.adapter

import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import android.view.View
import org.p2p.uikit.components.finance_block.FinanceBlockViewHolder
import org.p2p.uikit.components.finance_block.asFinanceCell
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensCellModelPayload

class SwapTokensOtherGroupDividerDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val currentViewHolder = parent.getChildViewHolder(view) as? FinanceBlockViewHolder ?: return
        val currentItemPayload = currentViewHolder.asFinanceCell?.getPayload<SwapTokensCellModelPayload>() ?: return

        // no need for paddings if we search tokens
        if (currentItemPayload.isSearchResultItem) {
            super.getItemOffsets(outRect, view, parent, state)
            return
        }

        val adapterPosition = currentViewHolder.bindingAdapterPosition
        val previousViewHolder = parent.findViewHolderForAdapterPosition(adapterPosition - 1)
        val nextViewHolder = parent.findViewHolderForAdapterPosition(adapterPosition + 1)

        val isOtherTokensGroupStarted = currentItemPayload.tokenModel is SwapTokenModel.JupiterToken
        if (isOtherTokensGroupStarted) {
            addTopPaddingToOtherTokensGroup(outRect, previousViewHolder, nextViewHolder)
        }
    }

    private fun addTopPaddingToOtherTokensGroup(
        outRect: Rect,
        previousViewHolder: RecyclerView.ViewHolder?,
        nextViewHolder: RecyclerView.ViewHolder?
    ) {
        val previousToken = previousViewHolder.asFinanceCell
            ?.getPayload<SwapTokensCellModelPayload>()
            ?.tokenModel
        val nextToken = nextViewHolder.asFinanceCell
            ?.getPayload<SwapTokensCellModelPayload>()
            ?.tokenModel

        val isUserTokensGroupFinished =
            previousToken is SwapTokenModel.UserToken &&
                nextToken !is SwapTokenModel.UserToken
        if (isUserTokensGroupFinished) {
            outRect.top = 30
        }
    }
}
