package org.p2p.wallet.jupiter.ui.tokens.adapter

import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import android.view.View
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.MainCellViewHolder
import org.p2p.uikit.components.finance_block.asFinanceCell
import org.p2p.uikit.utils.recycler.getItems
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.ui.tokens.presenter.SwapTokensCellModelPayload

class SwapTokensOtherGroupDividerDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val currentViewHolder = parent.getChildViewHolder(view) as? MainCellViewHolder ?: return
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
        val previousCell = items.getOrNull(adapterPosition - 1) as? MainCellModel
        val nextCell = items.getOrNull(adapterPosition + 1) as? MainCellModel

        val isOtherTokensGroupStarted = currentItemPayload.tokenModel is SwapTokenModel.JupiterToken
        if (isOtherTokensGroupStarted) {
            addTopPaddingToOtherTokensGroup(outRect, previousCell, nextCell)
        }
    }

    private fun addTopPaddingToOtherTokensGroup(
        outRect: Rect,
        previousCell: MainCellModel?,
        nextCell: MainCellModel?
    ) {
        val previousToken = (previousCell?.payload as? SwapTokensCellModelPayload)?.tokenModel
        val nextToken = (nextCell?.payload as? SwapTokensCellModelPayload)?.tokenModel

        val isUserTokensGroupFinished =
            previousToken is SwapTokenModel.UserToken &&
                nextToken !is SwapTokenModel.UserToken
        if (isUserTokensGroupFinished) {
            outRect.top = 30
        }
    }
}
