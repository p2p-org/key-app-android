package org.p2p.wallet.main.ui.buy.moonpay

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.main.model.BuyData
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.scaleShort
import java.math.BigDecimal

class BuySolanaPresenter(
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) : BasePresenter<BuySolanaContract.View>(), BuySolanaContract.Presenter {

    private var amount: String = ""

    override fun loadSolData() {
        launch {
            val tokens = userInteractor.getUserTokens()
            val sol = tokens.firstOrNull { it.isSOL && tokenKeyProvider.publicKey == it.publicKey } ?: return@launch
            val data = BuyData(
                price = sol.usdRateOrZero.scaleMedium(),
                processingFee = BigDecimal(4.71).scaleShort(),
                networkFee = BigDecimal(0.01).scaleShort(),
                accountCreationCost = BigDecimal(0.3).scaleShort()
            )
            view?.showData(data)
        }
    }

    override fun setBuyAmount(amount: String) {
        this.amount = amount
    }
}