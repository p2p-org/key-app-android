package org.p2p.wallet.home.ui.container

import kotlinx.coroutines.CoroutineScope
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.deeplinks.ReferralDeeplinkHandler
import org.p2p.wallet.deeplinks.SwapDeeplinkHandler
import org.p2p.wallet.home.deeplinks.DeeplinkScreenNavigator
import org.p2p.wallet.home.deeplinks.MainFragmentDeeplinkHandler
import org.p2p.wallet.home.ui.wallet.analytics.MainScreenAnalytics
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.user.interactor.UserInteractor

class MainFragmentDeeplinkHandlerFactory(
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val userInteractor: UserInteractor,
    private val swapDeeplinkHandler: SwapDeeplinkHandler,
    private val referralDeeplinkHandler: ReferralDeeplinkHandler,
    private val mainScreenAnalytics: MainScreenAnalytics,
) {
    fun create(
        navigator: DeeplinkScreenNavigator?,
        scope: CoroutineScope,
        deeplinkTopLevelHandler: (target: DeeplinkTarget) -> Unit = {},
    ): MainFragmentDeeplinkHandler {
        return MainFragmentDeeplinkHandler(
            coroutineScope = scope,
            screenNavigator = navigator,
            tokenServiceCoordinator = tokenServiceCoordinator,
            userInteractor = userInteractor,
            swapDeeplinkHandler = swapDeeplinkHandler,
            referralDeeplinkHandler = referralDeeplinkHandler,
            deeplinkTopLevelHandler = deeplinkTopLevelHandler,
            mainScreenAnalytics = mainScreenAnalytics,
        )
    }
}
