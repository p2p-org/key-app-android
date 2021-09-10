package com.p2p.wallet.main.ui.buy

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.launch

class BuyPresenter(
    private val token: Token?,
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