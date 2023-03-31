package org.p2p.wallet.history.ui.token.adapter.holders

import androidx.core.view.isVisible
import android.view.ViewGroup
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.UiKitDrawableCellModels
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.resources
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemHistoryUserSendLinksBinding
import org.p2p.wallet.history.ui.model.HistoryItem

class HistoryUserSendLinksViewHolder(
    parent: ViewGroup,
    private val onItemClicked: (HistoryItem.UserSendLinksItem) -> Unit,
    private val binding: ItemHistoryUserSendLinksBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(item: HistoryItem.UserSendLinksItem) = with(binding.root) {
        isVisible = true
        bind(item.toFinanceBlock())
        setOnClickListener { onItemClicked(item) }
    }

    private fun HistoryItem.UserSendLinksItem.toFinanceBlock(): FinanceBlockCellModel {
        val copyLink = ImageViewCellModel(
            icon = DrawableContainer(R.drawable.ic_copy_link),
            background = UiKitDrawableCellModels.shapeCircleWithTint(R.color.bg_rain)
        )
        val transactionsPlural = binding.resources.getQuantityString(
            R.plurals.plural_transaction, linksCount, linksCount
        )
        val leftSide = LeftSideCellModel.IconWithText(
            icon = IconWrapperCellModel.SingleIcon(
                icon = copyLink
            ),
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.transaction_history_send_via_link_title)
            ),
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(transactionsPlural)
            ),
        )
        val arrowRightIcon = ImageViewCellModel(DrawableContainer(R.drawable.ic_chevron_right))
        val rightSide = RightSideCellModel.IconWrapper(
            iconWrapper = IconWrapperCellModel.SingleIcon(arrowRightIcon)
        )

        val roundedBackground = DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(cornerSize = 16f.toPx())),
            tint = R.color.bg_snow
        )

        return FinanceBlockCellModel(
            leftSideCellModel = leftSide,
            rightSideCellModel = rightSide,
            background = roundedBackground
        )
    }
}
