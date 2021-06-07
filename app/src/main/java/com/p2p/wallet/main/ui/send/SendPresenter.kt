package com.p2p.wallet.main.ui.send

import com.p2p.wallet.R
import com.p2p.wallet.amount.isMoreThan
import com.p2p.wallet.amount.isZero
import com.p2p.wallet.amount.scalePrice
import com.p2p.wallet.amount.toBigDecimalOrZero
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.interactor.MainInteractor
import com.p2p.wallet.main.model.CurrencyMode
import com.p2p.wallet.main.model.TransactionResult
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.Token.Companion.USD_SYMBOL
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
        private const val VALID_ADDRESS_LENGTH = 24
    }

    private var token: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    private var inputAmount: String = "0"

    private var tokenAmount: BigDecimal = BigDecimal.ZERO
    private var usdAmount: BigDecimal = BigDecimal.ZERO

    private var mode: CurrencyMode by Delegates.observable(CurrencyMode.Token("")) { _, _, newValue ->
        view?.showCurrencyMode(newValue)
    }

    private var address: String = ""

    override fun setSourceToken(newToken: Token) {
        token = newToken
        calculateData(newToken)
    }

    override fun send() {
        val token = token ?: throw IllegalStateException("Token cannot be null!")

        launch {
            try {
                view?.showLoading(true)
                val usdAmount = token.usdRate.multiply(tokenAmount)
                val result = mainInteractor.sendToken(
                    target = address,
                    amount = tokenAmount,
                    usdAmount = usdAmount,
                    decimals = token.decimals,
                    tokenSymbol = token.tokenSymbol
                )
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
            token = source.copy(usdRate = exchangeRate)

            mode = CurrencyMode.Token(source.tokenSymbol)

            view?.showFullScreenLoading(false)
        }
    }

    override fun setNewSourceAmount(amount: String) {
        inputAmount = amount

        val token = token ?: return
        calculateData(token)
    }

    override fun loadTokensForSelection() {
        launch {
            val tokens = userInteractor.getTokens()
            view?.navigateToTokenSelection(tokens)
        }
    }

    override fun loadAvailableValue() {
        val token = token ?: return

        val totalAvailable = when (mode) {
            is CurrencyMode.Dollar -> token.totalInUsd
            is CurrencyMode.Token -> token.total.scalePrice()
        }
        view?.showInputValue(totalAvailable)
    }

    override fun setNewTargetAddress(address: String) {
        this.address = address

        calculateData(token!!)
    }

    override fun switchCurrency() {
        val token = token ?: return
        mode = when (mode) {
            is CurrencyMode.Token -> CurrencyMode.Dollar
            is CurrencyMode.Dollar -> CurrencyMode.Token(token.tokenSymbol)
        }

        calculateData(token)
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
        when (mode) {
            is CurrencyMode.Token -> {
                tokenAmount = inputAmount.toBigDecimalOrZero()
                usdAmount = tokenAmount.multiply(token.usdRate)

                val usdAround = tokenAmount.times(token.usdRate).scalePrice()
                view?.showUsdAroundValue(usdAround)
                view?.showAvailableValue(token.total, token.tokenSymbol)

                val isMoreThanBalance = tokenAmount.isMoreThan(token.total)
                val isNotZero = !tokenAmount.isZero()
                val isEnabled = isNotZero && !isMoreThanBalance
                setButtonEnabled(isEnabled, isMoreThanBalance)
            }
            is CurrencyMode.Dollar -> {
                usdAmount = inputAmount.toBigDecimalOrZero()
                tokenAmount = usdAmount.div(token.usdRate).scalePrice()

                val tokenAround = if (usdAmount.isZero()) {
                    BigDecimal.ZERO
                } else {
                    usdAmount.div(token.usdRate).scalePrice()
                }
                view?.showTokenAroundValue(tokenAround, token.tokenSymbol)

                view?.showAvailableValue(token.totalInUsd, USD_SYMBOL)

                val isMoreThanBalance = usdAmount.isMoreThan(token.totalInUsd)
                val isNotZero = !usdAmount.isZero()
                val isEnabled = isNotZero && !isMoreThanBalance
                setButtonEnabled(isEnabled, isMoreThanBalance)
            }
        }
    }

    private fun setButtonEnabled(isEnabled: Boolean, isMoreThanBalance: Boolean) {
        val isValidAddress = isAddressValid(address)
        val availableColor = if (isMoreThanBalance) R.attr.colorAccentWarning else R.attr.colorAccentPrimary
        view?.setAvailableTextColor(availableColor)
        view?.showButtonEnabled(isEnabled && isValidAddress)
    }

    private fun isAddressValid(address: String): Boolean =
        address.length > VALID_ADDRESS_LENGTH
}