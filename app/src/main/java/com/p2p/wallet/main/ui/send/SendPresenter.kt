package com.p2p.wallet.main.ui.send

import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.main.interactor.MainInteractor
import com.p2p.wallet.main.model.TransactionResult
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import kotlin.properties.Delegates

class SendPresenter(
    private val mainInteractor: MainInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<SendContract.View>(), SendContract.Presenter {

    companion object {
        private const val DESTINATION_USD = "USD"
    }

    private var token: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    override fun setSourceToken(newToken: Token) {
        token = newToken
    }

    override fun sendToken(targetAddress: String, amount: BigDecimal) {
        val token = token ?: return
        launch {
            try {
                view?.showLoading(true)
                val usdAmount = token.exchangeRate.times(amount)
                val result =
                    mainInteractor.sendToken(targetAddress, amount, usdAmount, token.decimals, token.tokenSymbol)
                handleResult(result)
            } catch (e: Throwable) {
                Timber.e(e, "Error sending token")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            val tokens = userInteractor.getTokens()
            val source = tokens.firstOrNull() ?: return@launch
            val exchangeRate = userInteractor.getPriceByToken(source.tokenSymbol, DESTINATION_USD)
            token = source.copy(exchangeRate = exchangeRate)
            view?.showFullScreenLoading(false)
        }
    }

    override fun loadTokensForSelection() {
        launch {
            val tokens = userInteractor.getTokens()
            view?.navigateToTokenSelection(tokens)
        }
    }

    override fun onAmountChanged(amount: BigDecimal) {
        val token = token ?: return
        view?.updateState(token, amount)
    }

    private fun handleResult(result: TransactionResult) {
        when (result) {
            is TransactionResult.Success -> {
                val info = TransactionInfo(
                    transactionId = result.transactionId,
                    status = R.string.main_send_success,
                    message = R.string.main_send_transaction_confirmed,
                    iconRes = R.drawable.ic_success,
                    amount = result.amount,
                    usdAmount = result.usdAmount,
                    tokenSymbol = result.tokenSymbol
                )
                view?.showSuccess(info)
            }
            is TransactionResult.WrongWallet ->
                view?.showWrongWalletError()
            is TransactionResult.Error ->
                view?.showErrorMessage(result.messageRes)
        }
    }
}