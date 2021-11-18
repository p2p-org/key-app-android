package org.p2p.wallet.main.ui.send

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.main.interactor.SearchInteractor
import org.p2p.wallet.main.interactor.SendInteractor
import org.p2p.wallet.main.model.CurrencyMode
import org.p2p.wallet.main.model.NetworkType
import org.p2p.wallet.main.model.SearchResult
import org.p2p.wallet.main.model.Target
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.Token.Companion.USD_SYMBOL
import org.p2p.wallet.main.model.TransactionResult
import org.p2p.wallet.main.ui.transaction.TransactionInfo
import org.p2p.wallet.renbtc.interactor.BurnBtcInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.cutEnd
import org.p2p.wallet.utils.isMoreThan
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.toBigDecimalOrZero
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.properties.Delegates

class SendPresenter(
    private val initialToken: Token.Active?,
    private val sendInteractor: SendInteractor,
    private val userInteractor: UserInteractor,
    private val searchInteractor: SearchInteractor,
    private val burnBtcInteractor: BurnBtcInteractor
) : BasePresenter<SendContract.View>(), SendContract.Presenter {

    companion object {
        private const val VALID_ADDRESS_LENGTH = 24
        private const val DESTINATION_USD = "USD"
        private const val SYMBOL_REN_BTC = "renBTC"
        private const val ROUNDING_VALUE = 6
    }

    private var token: Token.Active? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    private var inputAmount: String = "0"

    private var tokenAmount: BigDecimal = BigDecimal.ZERO
    private var usdAmount: BigDecimal = BigDecimal.ZERO

    private var mode: CurrencyMode = CurrencyMode.Own("")

    private var networkType: NetworkType = NetworkType.SOLANA

    private var target: SearchResult? = null

    private var calculationJob: Job? = null
    private var feeJob: Job? = null

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            val source = initialToken ?: userInteractor.getUserTokens().firstOrNull() ?: return@launch
            val exchangeRate = userInteractor.getPriceByToken(source.tokenSymbol, DESTINATION_USD)
            token = source.copy(usdRate = exchangeRate?.price)
            mode = CurrencyMode.Own(source.tokenSymbol)
            view?.showNetworkDestination(networkType)

            calculateFee()

            view?.showFullScreenLoading(false)
        }
    }

    override fun setSourceToken(newToken: Token.Active) {
        token = newToken

        if (newToken.tokenSymbol == SYMBOL_REN_BTC) {
            view?.showNetworkSelection()
        } else {
            view?.hideNetworkSelection()
        }

        calculateFee()
        calculateData(newToken)
    }

    override fun setTargetResult(result: SearchResult?) {
        target = result

        when (result) {
            is SearchResult.Full -> view?.showFullTarget(result.address, result.username)
            is SearchResult.AddressOnly -> view?.showAddressOnlyTarget(result.address)
            is SearchResult.EmptyBalance -> view?.showEmptyBalanceTarget(result.address.cutEnd())
            is SearchResult.Wrong -> view?.showWrongAddressTarget(result.address.cutEnd())
            else -> view?.showIdleTarget()
        }

        val sourceToken = token ?: return
        calculateData(sourceToken)
    }

    override fun validateTarget(value: String) {
        launch {
            try {
                view?.showSearchLoading(true)
                val target = Target(value)
                when (target.validation) {
                    Target.Validation.USERNAME -> searchByUsername(target.value)
                    Target.Validation.ADDRESS -> searchByAddress(target.value)
                    Target.Validation.EMPTY -> view?.showIdleTarget()
                    Target.Validation.INVALID -> view?.showWrongAddressTarget(target.value)
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error validating target: $value")
            } finally {
                view?.showSearchLoading(false)
            }
        }
    }

    override fun setNewSourceAmount(amount: String) {
        inputAmount = amount

        val token = token ?: return
        calculateData(token)
    }

    override fun setNetworkDestination(networkType: NetworkType) {
        this.networkType = networkType
        view?.showNetworkDestination(networkType)
        calculateFee()
    }

    override fun send() {
        val token = token ?: throw IllegalStateException("Token cannot be null!")
        val address = target?.address ?: throw IllegalStateException("Target address cannot be null!")

        when (networkType) {
            NetworkType.SOLANA -> sendInSolana(token, address)
            NetworkType.BITCOIN -> sendInBitcoin(token, address)
        }
    }

    override fun loadTokensForSelection() {
        launch {
            val tokens = userInteractor.getUserTokens()
            view?.navigateToTokenSelection(tokens)
        }
    }

    override fun loadAvailableValue() {
        val token = token ?: return

        val totalAvailable = when (mode) {
            is CurrencyMode.Usd -> token.totalInUsd
            is CurrencyMode.Own -> token.total.scaleLong()
        } ?: return

        view?.showInputValue(totalAvailable)
    }

    override fun switchCurrency() {
        val token = token ?: return
        mode = when (mode) {
            is CurrencyMode.Own -> CurrencyMode.Usd
            is CurrencyMode.Usd -> CurrencyMode.Own(token.tokenSymbol)
        }

        calculateData(token)
    }

    private fun sendInBitcoin(token: Token.Active, address: String) {
        launch {
            try {
                view?.showLoading(true)
                val amount = tokenAmount.toLamports(token.decimals)
                val transactionId = burnBtcInteractor.submitBurnTransaction(address, amount)
                Timber.d("Bitcoin successfully burned and released! $transactionId")
                handleResult(TransactionResult.Success(transactionId))
            } catch (e: Throwable) {
                Timber.e(e, "Error sending token")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun sendInSolana(token: Token.Active, address: String) {
        launch {
            try {
                view?.showLoading(true)

                val result = if (token.isSOL) {
                    sendInteractor.sendNativeSolToken(
                        destinationAddress = address.toPublicKey(),
                        lamports = tokenAmount.toLamports(token.decimals)
                    )
                } else {
                    sendInteractor.sendSplToken(
                        destinationAddress = address.toPublicKey(),
                        token = token,
                        lamports = tokenAmount.toLamports(token.decimals)
                    )
                }

                handleResult(result)
            } catch (e: Throwable) {
                Timber.e(e, "Error sending token")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun handleResult(result: TransactionResult) {
        when (result) {
            is TransactionResult.Success -> {
                val info = TransactionInfo(
                    transactionId = result.transactionId,
                    status = R.string.main_send_success,
                    message = R.string.main_send_transaction_confirmed,
                    iconRes = R.drawable.ic_success,
                    amount = "-$tokenAmount",
                    usdAmount = "-${(token!!.usdRate ?: BigDecimal.ZERO).multiply(tokenAmount).scaleMedium()}",
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

    private fun calculateData(token: Token.Active) {
        if (calculationJob?.isActive == true) return

        calculationJob = launch {
            when (mode) {
                is CurrencyMode.Own -> {
                    tokenAmount = inputAmount.toBigDecimalOrZero()
                    usdAmount = tokenAmount.multiply(token.usdRateOrZero)

                    val usdAround = tokenAmount.times(token.usdRateOrZero).scaleMedium()
                    val total = token.total.scaleLong()
                    view?.showUsdAroundValue(usdAround)
                    view?.showAvailableValue(total, token.tokenSymbol)

                    updateButtonText(token)

                    setButtonEnabled(tokenAmount, total)
                }
                is CurrencyMode.Usd -> {
                    usdAmount = inputAmount.toBigDecimalOrZero()
                    tokenAmount = if (token.usdRateOrZero.isZero()) {
                        BigDecimal.ZERO
                    } else {
                        usdAmount.divide(token.usdRateOrZero, ROUNDING_VALUE, RoundingMode.HALF_EVEN)
                            .stripTrailingZeros()
                    }

                    val tokenAround = if (usdAmount.isZero() || token.usdRateOrZero.isZero()) {
                        BigDecimal.ZERO
                    } else {
                        usdAmount.divide(token.usdRateOrZero, ROUNDING_VALUE, RoundingMode.HALF_EVEN)
                            .stripTrailingZeros()
                    }
                    view?.showTokenAroundValue(tokenAround, token.tokenSymbol)
                    view?.showAvailableValue(token.totalInUsd ?: BigDecimal.ZERO, USD_SYMBOL)

                    updateButtonText(token)

                    setButtonEnabled(usdAmount, token.totalInUsd ?: BigDecimal.ZERO)
                }
            }
        }
    }

    private fun calculateFee() {
        if (networkType == NetworkType.SOLANA) {
            view?.showFee(null)
            return
        }

        if (feeJob?.isActive == true) return

        launch {
            val fee = burnBtcInteractor.getBurnFee()
            view?.showFee("$fee SOL")
        }
    }

    private suspend fun searchByUsername(username: String) {
        val usernames = searchInteractor.searchByName(username)
        if (usernames.isEmpty()) {
            view?.showWrongAddressTarget(username)
            return
        }

        val result = usernames.first()
        setTargetResult(result)
    }

    private suspend fun searchByAddress(address: String) {
        val validatedAddress = try {
            PublicKey(address)
        } catch (e: Throwable) {
            view?.showWrongAddressTarget(address.cutEnd())
            null
        } ?: return

        val results = searchInteractor.searchByAddress(validatedAddress.toBase58())
        if (results.isEmpty()) return

        val first = results.first()
        setTargetResult(first)
    }

    private fun updateButtonText(source: Token.Active) {
        val decimalAmount = inputAmount.toBigDecimalOrZero()
        val isMoreThanBalance = decimalAmount.isMoreThan(source.total)
        val address = target?.address

        when {
            isMoreThanBalance ->
                view?.showButtonText(R.string.swap_funds_not_enough)
            decimalAmount.isZero() ->
                view?.showButtonText(R.string.main_enter_the_amount)
            address.isNullOrBlank() ->
                view?.showButtonText(R.string.send_enter_address)
            else ->
                view?.showButtonText(R.string.send_now)
        }
    }

    private fun setButtonEnabled(amount: BigDecimal, total: BigDecimal) {
        val isMoreThanBalance = amount.isMoreThan(total)
        val isNotZero = !amount.isZero()
        val isValidAddress = isAddressValid(target?.address)

        val isEnabled = isNotZero && !isMoreThanBalance && isValidAddress

        val availableColor = if (isMoreThanBalance) R.attr.colorAccentWarning else R.attr.colorAccentPrimary
        view?.setAvailableTextColor(availableColor)
        view?.showButtonEnabled(isEnabled)
    }

    private fun isAddressValid(address: String?): Boolean =
        !address.isNullOrBlank() && address.trim().length >= VALID_ADDRESS_LENGTH
}