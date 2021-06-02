package com.p2p.wallet.main.ui.send

import com.p2p.wallet.R
import com.p2p.wallet.amount.scalePrice
import com.p2p.wallet.amount.toBigDecimalOrZero
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.interactor.MainInteractor
import com.p2p.wallet.main.model.TransactionResult
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.token.model.Token
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

    private var sourceAmount: String = "0"
    private var aroundValue: BigDecimal = BigDecimal.ZERO

    private var targetAddress: String = ""

    override fun setSourceToken(newToken: Token) {
        token = newToken

        calculateData(newToken)
    }

    override fun sendToken(targetAddress: String, amount: BigDecimal) {
        val token = token ?: return
        launch {
            try {
                view?.showLoading(true)
                val usdAmount = token.exchangeRate.toBigDecimal().times(amount)
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

    override fun setSourceAmount(amount: String) {
        sourceAmount = amount
        val token = token ?: return

        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = token.exchangeRate.toBigDecimal().times(decimalAmount)

        val isMoreThanBalance = decimalAmount.toBigInteger() > token.total.toBigInteger()
        val availableColor = if (isMoreThanBalance) R.color.colorRed else R.color.colorBlue

        view?.setAvailableTextColor(availableColor)
        view?.showAroundValue(aroundValue)

        calculateData(token)

        setButtonEnabled()
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

    override fun feedAvailableValue() {
        val token = token ?: return
        view?.updateInputValue(token.total.scalePrice())
    }

    override fun setNewTargetAddress(address: String) {
        targetAddress = address
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

    private fun calculateData(token: Token) {
        val isMoreThanBalance = sourceAmount.toBigDecimalOrZero() > token.total
        val availableColor = if (isMoreThanBalance) R.color.colorRed else R.color.colorBlue
        view?.setAvailableTextColor(availableColor)

        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = token.exchangeRate.toBigDecimal().times(decimalAmount)
        view?.showAroundValue(aroundValue)

        setButtonEnabled()
    }

    private fun setButtonEnabled() {
        val isMoreThanBalance = sourceAmount.toBigDecimalOrZero().compareTo(token?.total ?: BigDecimal.ZERO) > 1
        val isEnabled = sourceAmount.toBigDecimalOrZero()
            .compareTo(BigDecimal.ZERO) != 0 && !isMoreThanBalance && targetAddress.isNotBlank()
        view?.showButtonEnabled(isEnabled)
    }
}