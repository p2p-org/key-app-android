package org.p2p.wallet.main.ui.buy

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.launch

class BuyPresenter(
    private val token: Token.Active?,
    private val userInteractor: UserInteractor,
    private val environmentManager: EnvironmentManager
) : BasePresenter<BuyContract.View>(), BuyContract.Presenter {

    override fun loadData() {
        launch {
            val token = token ?: userInteractor.getUserTokens().firstOrNull { it.isSOL } ?: return@launch
            val url = environmentManager.getTransakUrl(token)
            view?.openWebView(url)
        }
    }
}