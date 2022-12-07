package org.p2p.wallet.newsend

import kotlinx.coroutines.launch
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.core.token.Token
import org.p2p.wallet.user.interactor.UserInteractor
import kotlin.properties.Delegates

class NewSendPresenter(
    private val userInteractor: UserInteractor,
    private val browseAnalytics: BrowseAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
) : BasePresenter<NewSendContract.View>(), NewSendContract.Presenter {

    private var token: Token.Active? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showTokenToSend(newValue)
    }

    init {
        launch {
            token = userInteractor.getUserTokens().first()
        }
    }

    override fun onTokenClicked() {
        loadTokensForSelection()
    }

    override fun setTokenToSend(newToken: Token.Active) {
        token = newToken
    }

    private fun loadTokensForSelection() {
        launch {
            val tokens = userInteractor.getUserTokens()
            val result = tokens.filter { token -> !token.isZero }
            browseAnalytics.logTokenListViewed(
                lastScreenName = analyticsInteractor.getPreviousScreenName(),
                tokenListLocation = BrowseAnalytics.TokenListLocation.SEND
            )

            view?.navigateToTokenSelection(result, token)
        }
    }
}
