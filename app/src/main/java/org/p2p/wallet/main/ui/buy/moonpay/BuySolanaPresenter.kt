package org.p2p.wallet.main.ui.buy.moonpay

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.main.model.BuyCurrency
import org.p2p.wallet.main.model.BuyData
import org.p2p.wallet.main.model.MoonpayBuyResult
import org.p2p.wallet.main.repository.MoonpayRepository
import org.p2p.wallet.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.wallet.utils.Constants.USD_SYMBOL
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.toBigDecimalOrZero
import timber.log.Timber

private const val DELAY_IN_MS = 250L

class BuySolanaPresenter(
    private val moonpayRepository: MoonpayRepository,
) : BasePresenter<BuySolanaContract.View>(), BuySolanaContract.Presenter {

    private var amount: String = "0"

    override fun loadData() {
        launch {
            try {
                view?.showLoading(true)
                val price = moonpayRepository.getCurrencyAskPrice("eth").scaleShort()
                view?.showTokenPrice("$USD_SYMBOL$price")
            } catch (e: Throwable) {
                Timber.e(e, "Error loading currency ask price")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun onContinueClicked() {
        view?.navigateToMoonpay(amount)
    }

    override fun setBuyAmount(amount: String) {
        this.amount = amount
        calculate()
    }

    private fun calculate() {
        val parsedAmount = amount.toBigDecimalOrZero()
        if (parsedAmount.isZero()) return

        launch {
            try {
                delay(DELAY_IN_MS)
                view?.showLoading(true)
                val baseCurrencyCode = USD_READABLE_SYMBOL.lowercase()
                when (val result = moonpayRepository.getCurrency(amount, "eth", baseCurrencyCode)) {
                    is MoonpayBuyResult.Success -> handleSuccess(result.data)
                    is MoonpayBuyResult.Error -> view?.showMessage(result.message)
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error loading buy currency data")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun handleSuccess(info: BuyCurrency) {
        val data = BuyData(
            price = info.price.scaleShort(),
            receiveAmount = info.receiveAmount.scaleShort(),
            processingFee = info.feeAmount.scaleShort(),
            networkFee = info.networkFeeAmount.scaleShort(),
            extraFee = info.extraFeeAmount.scaleShort(),
            accountCreationCost = null,
            total = info.totalAmount.scaleShort()
        )
        view?.showData(data)
    }
}