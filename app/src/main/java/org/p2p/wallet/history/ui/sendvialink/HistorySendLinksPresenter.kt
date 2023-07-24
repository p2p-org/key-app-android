package org.p2p.wallet.history.ui.sendvialink

import org.threeten.bp.ZonedDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.formatToken
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.ui.delegates.HistoryDateCellModel
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksLocalRepository
import org.p2p.wallet.infrastructure.sendvialink.model.UserSendLink

class HistorySendLinksPresenter(
    private val userSendLinksRepository: UserSendLinksLocalRepository
) : BasePresenter<HistorySendLinksContract.View>(),
    HistorySendLinksContract.Presenter {

    private val mainCellHorizontalMargin = 12.toPx()

    override fun attach(view: HistorySendLinksContract.View) {
        super.attach(view)

        launch {
            val userLinksModels: List<AnyCellItem> = getUserLinksModels()
            view.showUserLinks(userLinksModels)
        }
    }

    private suspend fun getUserLinksModels(): List<AnyCellItem> {
        return userSendLinksRepository.getUserLinks()
            .sortedByDescending(UserSendLink::dateCreated)
            .groupByCreationDate()
            .flatMap { (_, links) ->
                val dateHistoryModel = links.first().dateCreated.toZonedDateTime().toCellModel()
                val linksModels = links.map { it.toCellModel() }
                listOf(dateHistoryModel) + linksModels
            }
    }

    private fun ZonedDateTime.toCellModel(): HistoryDateCellModel {
        return HistoryDateCellModel(this)
    }

    private fun UserSendLink.toCellModel(): MainCellModel {
        val icon = ImageViewCellModel(
            icon = DrawableContainer(R.drawable.ic_copy_link),
            iconTint = R.color.bg_night,
            background = DrawableCellModel(
                drawable = shapeDrawable(shapeCircle()),
                tint = R.color.bg_rain
            )
        )

        val leftSide = LeftSideCellModel.IconWithText(
            icon = IconWrapperCellModel.SingleIcon(
                icon = icon
            ),
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.transaction_history_send_via_link_title)
            ),
        )

        val tokenAmount = TextContainer.invoke("${amount.formatToken(token.decimals)} ${token.tokenSymbol}")

        val rightSide = RightSideCellModel.TwoLineText(
            firstLineText = TextViewCellModel.Raw(tokenAmount, maxLines = 2)
        )
        return MainCellModel(
            leftSideCellModel = leftSide,
            rightSideCellModel = rightSide,
            payload = uuid,
            horizontalMargins = mainCellHorizontalMargin
        )
    }

    private fun List<UserSendLink>.groupByCreationDate(): Map<String, List<UserSendLink>> {
        // only sane way to group links by date, doesn't work with ZonedDateTime or Calendar
        val dateFormatForGroup = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return groupBy { dateFormatForGroup.format(Date(it.dateCreated)) }
    }
}
