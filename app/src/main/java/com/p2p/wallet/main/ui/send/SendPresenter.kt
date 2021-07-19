package com.p2p.wallet.main.ui.send

import com.p2p.wallet.R
import com.p2p.wallet.utils.isMoreThan
import com.p2p.wallet.utils.isZero
import com.p2p.wallet.utils.scaleAmount
import com.p2p.wallet.utils.scalePrice
import com.p2p.wallet.utils.scaleShort
import com.p2p.wallet.utils.toBigDecimalOrZero
import com.p2p.wallet.utils.toLamports
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.interactor.MainInteractor
import com.p2p.wallet.main.model.CurrencyMode
import com.p2p.wallet.main.model.TransactionResult
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.Token.Companion.USD_SYMBOL
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.properties.Delegates

class SendPresenter(
    private val initialToken: Token?,
    private val mainInteractor: MainInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<SendContract.View>(), SendContract.Presenter {

    companion object {
        private const val DESTINATION_USD = "USD"
        private const val VALID_ADDRESS_LENGTH = 24
        private const val ROUNDING_VALUE = 6
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

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            val source = initialToken ?: userInteractor.getTokens().firstOrNull() ?: return@launch
            val exchangeRate = userInteractor.getPriceByToken(source.tokenSymbol, DESTINATION_USD)
            token = source.copy(usdRate = exchangeRate)

            mode = CurrencyMode.Token(source.tokenSymbol)

            view?.showFullScreenLoading(false)
        }
    }

    override fun setSourceToken(newToken: Token) {
        token = newToken
        calculateData(newToken)
    }

    override fun send() {
        val token = token ?: throw IllegalStateException("Token cannot be null!")

        launch {
            try {
                view?.showLoading(true)
                val result = mainInteractor.sendTransaction(
                    ownerAddress = address.toPublicKey(),
                    token = token,
                    lamports = tokenAmount.toLamports(token.decimals)
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
            is CurrencyMode.Usd -> token.totalInUsd
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
            is CurrencyMode.Token -> CurrencyMode.Usd
            is CurrencyMode.Usd -> CurrencyMode.Token(token.tokenSymbol)
        }

        calculateData(token)
    }

    private fun handleResult(result: TransactionResult) {
        when (result) {
            is TransactionResult.Success -> {
                val info = TransactionInfo(
                    transactionId = result.signature,
                    status = R.string.main_send_success,
                    message = R.string.main_send_transaction_confirmed,
                    iconRes = R.drawable.ic_success,
                    amount = tokenAmount,
                    usdAmount = token!!.usdRate.multiply(tokenAmount).scaleAmount(),
                    tokenSymbol = token!!.tokenSymbol
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

                val usdAround = tokenAmount.times(token.usdRate.scaleShort())
                view?.showUsdAroundValue(usdAround)
                view?.showAvailableValue(token.total, token.tokenSymbol)

                val isMoreThanBalance = tokenAmount.isMoreThan(token.total)
                val isNotZero = !tokenAmount.isZero()
                val isEnabled = isNotZero && !isMoreThanBalance
                setButtonEnabled(isEnabled, isMoreThanBalance)
            }
            is CurrencyMode.Usd -> {
                usdAmount = inputAmount.toBigDecimalOrZero()
                tokenAmount = if (token.usdRate.isZero()) {
                    BigDecimal.ZERO
                } else {
                    usdAmount.divide(token.usdRate, ROUNDING_VALUE, RoundingMode.HALF_EVEN).stripTrailingZeros()
                }

                val tokenAround = if (usdAmount.isZero() || token.usdRate.isZero()) {
                    BigDecimal.ZERO
                } else {
                    usdAmount.divide(token.usdRate, ROUNDING_VALUE, RoundingMode.HALF_EVEN).stripTrailingZeros()
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