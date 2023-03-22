package org.p2p.wallet.history.ui.history

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksLocalRepository
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.ifNotEmpty
import org.p2p.wallet.utils.toPx

class HistoryPresenter(
    private val userInteractor: UserInteractor,
    private val historyAnalytics: HistoryAnalytics,
    private val userSendLinksRepository: UserSendLinksLocalRepository
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    override fun attach(view: HistoryContract.View) {
        super.attach(view)
        historyAnalytics.onScreenOpened()
        checkForUserLinks()
    }

    private fun checkForUserLinks() {
        launch {
            try {
                val links = userSendLinksRepository.getUserLinks()
                if (links.isNotEmpty()) { // don't forget to revert
                    view?.showSendViaLinkBlock(createUserLinksBlock(links.size))
                }
            } catch (error: Throwable) {
                Timber.e(error, "Failed to load user links for send")
            }
        }
    }

    private fun createUserLinksBlock(linksCount: Int): FinanceBlockCellModel {
        val copyLink = ImageViewCellModel(DrawableContainer(R.drawable.ic_copy_link))
        val leftSide = LeftSideCellModel.IconWithText(
            icon = IconWrapperCellModel.SingleIcon(
                icon = copyLink
            ),
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.transaction_history_send_via_link_title)
            ),
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.transaction_history_send_via_link_subtitle, linksCount)
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

    override fun onBuyClicked() {
        launch {
            userInteractor.getTokensForBuy().ifNotEmpty {
                view?.showBuyScreen(it.first())
            }
        }
    }

    override fun onTransactionClicked(transactionId: String) {
        view?.openTransactionDetailsScreen(transactionId)
    }

    override fun onSellTransactionClicked(transactionId: String) {
        view?.openSellTransactionDetails(transactionId)
    }
}
