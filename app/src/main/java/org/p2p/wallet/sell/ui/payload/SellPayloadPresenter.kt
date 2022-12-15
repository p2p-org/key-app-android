package org.p2p.wallet.sell.ui.payload

import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.moonpay.model.MoonpayWidgetUrlBuilder
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellFiatCurrency
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.launch

private val MIN_AMOUNT_TO_SELL = BigDecimal.valueOf(20)
private val MAX_AMOUNT_TO_SELL = BigDecimal.valueOf(100)

class SellPayloadPresenter(
    private val sellInteractor: SellInteractor,
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val moonpayWidgetUrlBuilder: MoonpayWidgetUrlBuilder
) : BasePresenter<SellPayloadContract.View>(),
    SellPayloadContract.Presenter {

    sealed interface ViewState {
        object Initial : ViewState

        data class Content(
            val tokenToSell: Token.Active,
            val tokenAmountToSell: BigDecimal,
            val fiatCurrency: MoonpaySellFiatCurrency,
        ) : ViewState
    }

    private var state: ViewState = ViewState.Initial

    override fun load() {
        launch {
            try {
                view?.showLoading(isVisible = true)
                val userTransactionInProcess = getUserTransactionInProcess()
                if (userTransactionInProcess != null) {
                    // make readable in https://p2pvalidator.atlassian.net/browse/PWN-6354
                    view?.navigateToSellLock(
                        solAmount = userTransactionInProcess.amounts.tokenAmount,
                        usdAmount = userTransactionInProcess.amounts.usdAmount.toPlainString(),
                        moonpayAddress = tokenKeyProvider.publicKey.toBase58Instance()
                    )
                    return@launch
                }
                if (state == ViewState.Initial) {
                    initView()
                }
            } catch (e: Throwable) {
                Timber.e("Error on init view $e")
            } finally {
                view?.showLoading(isVisible = false)
            }
        }
    }

    private suspend fun getUserTransactionInProcess(): MoonpaySellTransaction? {
        val userTransactions = sellInteractor.loadUserSellTransactions()
        return userTransactions.find { it.status == MoonpaySellTransaction.TransactionStatus.WAITING_FOR_DEPOSIT }
    }

    private suspend fun initView() {
        val solToken = userInteractor.getUserSolToken() ?: return
        view?.showAvailableSolToSell(solToken.total)
        view?.setMinSolToSell(MIN_AMOUNT_TO_SELL, solToken.tokenSymbol.uppercase())

        state = ViewState.Content(
            tokenToSell = solToken,
            tokenAmountToSell = MIN_AMOUNT_TO_SELL,
            fiatCurrency = MoonpaySellFiatCurrency.USD
        )
    }

    override fun cashOut() {
        val userAddress = tokenKeyProvider.publicKey.toBase58Instance()

        val moonpayUrl = moonpayWidgetUrlBuilder.buildSellWidgetUrl(
            tokenSymbol = Constants.SOL_SYMBOL,
            userAddress = userAddress,
            fiatSymbol = "usd",
            tokenAmountToSell = "0.005",
        )
        view?.showMoonpayWidget(url = moonpayUrl)
    }
}
