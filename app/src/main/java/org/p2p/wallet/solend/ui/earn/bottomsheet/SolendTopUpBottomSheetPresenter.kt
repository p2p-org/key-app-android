package org.p2p.wallet.solend.ui.earn.bottomsheet

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.core.token.Token
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.interactor.UserTokensInteractor

class SolendTopUpBottomSheetPresenter(
    private val deposit: SolendDepositToken,
    private val userInteractor: UserInteractor,
    private val userTokensInteractor: UserTokensInteractor
) : BasePresenter<SolendTopUpBottomSheetContract.View>(),
    SolendTopUpBottomSheetContract.Presenter {

    private var currentToken: Token? = null
    private var solToken: Token? = null

    override fun attach(view: SolendTopUpBottomSheetContract.View) {
        super.attach(view)
        launch {
            currentToken = userInteractor.getTokensForBuy(listOf(deposit.tokenSymbol)).firstOrNull()
            solToken = userTokensInteractor.getUserSolToken()
        }
    }

    override fun onBuyClicked() {
        val tokenToBuy = currentToken?.takeIf { it.isUSDC } ?: solToken
        tokenToBuy?.let { token ->
            view?.showBuyScreen(token)
        }
    }

    override fun onReceiveClicked() {
        currentToken?.let { token ->
            view?.showReceiveScreen(token)
        }
    }
}
