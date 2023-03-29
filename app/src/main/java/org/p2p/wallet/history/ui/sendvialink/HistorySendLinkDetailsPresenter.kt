package org.p2p.wallet.history.ui.sendvialink

import android.content.Context
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.wallet.common.date.toDateString
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.ui.sendvialink.HistorySendLinkDetailsContract.ViewState
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksLocalRepository
import org.p2p.wallet.infrastructure.sendvialink.model.UserSendLink

class HistorySendLinkDetailsPresenter(
    private val linkUuid: String,
    private val userSendLinksRepository: UserSendLinksLocalRepository,
    private val context: Context,
) : BasePresenter<HistorySendLinkDetailsContract.View>(),
    HistorySendLinkDetailsContract.Presenter {

    override fun attach(view: HistorySendLinkDetailsContract.View) {
        super.attach(view)
        launch {
            try {
                view.renderState(ViewState.Loading)
                val linkDetails = loadLinkDetails()
                view.renderState(linkDetails.toContentState())
            } catch (loadFailed: Throwable) {
                Timber.e(loadFailed, "Failed to fetch info for link")
                view.close()
            }
        }
    }

    private fun UserSendLink.toContentState(): ViewState.Content {
        return ViewState.Content(
            link = link,
            iconUrl = token.iconUrl,
            formattedAmountUsd = "${amountInUsd.formatFiat()} $",
            formattedTokenAmount = "${amount.formatToken(token.decimals)} ${token.tokenSymbol}",
            formattedDate = dateCreated.toZonedDateTime().toDateString(context)
        )
    }

    private suspend fun loadLinkDetails(): UserSendLink {
        val userLink = userSendLinksRepository.getUserLinkById(linkUuid)
        return requireNotNull(userLink) { "Link for uuid $linkUuid not found" }
    }
}
