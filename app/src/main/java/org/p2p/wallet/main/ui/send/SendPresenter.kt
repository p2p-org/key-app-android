package org.p2p.wallet.main.ui.send

import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.main.interactor.SendInteractor
import org.p2p.wallet.main.model.CurrencyMode
import org.p2p.wallet.main.model.NetworkType
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.Token.Companion.USD_SYMBOL
import org.p2p.wallet.main.model.TransactionResult
import org.p2p.wallet.main.ui.transaction.TransactionInfo
import org.p2p.wallet.renbtc.interactor.BurnBtcInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.isMoreThan
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.toBigDecimalOrZero
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toPublicKey
import retrofit2.HttpException
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.properties.Delegates

class SendPresenter(
    private val initialToken: Token.Active?,
    private val sendInteractor: SendInteractor,
    private val userInteractor: UserInteractor,
    private val burnBtcInteractor: BurnBtcInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val context: Context,
) : BasePresenter<SendContract.View>(), SendContract.Presenter {

    companion object {
        private const val VALID_ADDRESS_LENGTH = 24
        private val VALID_USER_NAME_RANGE = 1..15
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

    private var mode: CurrencyMode by Delegates.observable(CurrencyMode.Own("")) { _, _, newValue ->
        view?.showCurrencyMode(newValue)
    }

    private var networkType: NetworkType = NetworkType.SOLANA

    private var destinationAddress: String = ""
    private var username: String = ""

    private var shouldAskConfirmation: Boolean = false

    private var calculationJob: Job? = null
    private var checkBalanceJob: Job? = null
    private var feeJob: Job? = null

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            val source = initialToken ?: userInteractor.getUserTokens().firstOrNull() ?: return@launch
            val exchangeRate = userInteractor.getPriceByToken(source.tokenSymbol, DESTINATION_USD)
            token = source.copy(usdRate = exchangeRate?.price)
            mode = CurrencyMode.Own(source.tokenSymbol)

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

        when (networkType) {
            NetworkType.SOLANA -> sendInSolana(token)
            NetworkType.BITCOIN -> sendInBitcoin(token)
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

    override fun setNewTargetAddress(address: String) {
        this.destinationAddress = address
        val addressOrName = address.replace(context.getString(R.string.auth_p2p_sol), "")
        when {
            addressOrName.length in VALID_USER_NAME_RANGE -> {
                launch {
                    try {
                        val checkUsername = usernameInteractor.checkUsername(addressOrName)
                        username = addressOrName
                        destinationAddress = checkUsername.owner
                        view?.showBufferUsernameResolvedOk(checkUsername.owner)
                        calculateData(token!!)
                    } catch (e: HttpException) {
                        view?.showBufferNoAddress()
                        Timber.e(e, "Error checking username")
                    }
                }
            }

            addressOrName.length >= VALID_ADDRESS_LENGTH -> {
                if (!isAddressValid(address)) {
                    view?.showButtonText(R.string.send_enter_address)
                    view?.showButtonEnabled(false)
                    return
                }

                /* Checking destination balance only for Solana network transfers */
                if (networkType == NetworkType.SOLANA) {
                    checkDestinationBalance(address)
                } else {
                    view?.hideAddressConfirmation()
                }

                calculateData(token!!)
            }

            else -> {
                if (address.isNotEmpty()) {
                    view?.showBufferNoAddress()
                    view?.showButtonText(R.string.send_enter_address)
                    view?.showButtonEnabled(false)
                }
            }
        }
    }

    override fun switchCurrency() {
        val token = token ?: return
        mode = when (mode) {
            is CurrencyMode.Own -> CurrencyMode.Usd
            is CurrencyMode.Usd -> CurrencyMode.Own(token.tokenSymbol)
        }

        calculateData(token)
    }

    override fun setShouldAskConfirmation(shouldAsk: Boolean) {
        shouldAskConfirmation = shouldAsk
        updateButtonText(token!!)

        if (mode is CurrencyMode.Own) {
            tokenAmount = inputAmount.toBigDecimalOrZero()
            setButtonEnabled(tokenAmount, token!!.total)
        } else {
            usdAmount = inputAmount.toBigDecimalOrZero()
            setButtonEnabled(usdAmount, token!!.totalInUsd ?: BigDecimal.ZERO)
        }
    }

    private fun sendInBitcoin(token: Token.Active) {
        launch {
            try {
                view?.showLoading(true)
                val amount = tokenAmount.toLamports(token.decimals)
                val transactionId = burnBtcInteractor.submitBurnTransaction(destinationAddress, amount)
                Timber.d("Bitcoin successfully burned and released! $transactionId")
                handleResult(TransactionResult.Success(transactionId))
            } catch (e: Throwable) {
                Timber.e(e, "Error sending token")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun sendInSolana(token: Token.Active) {
        Timber.i("efef " + destinationAddress)
        launch {
            try {
                view?.showLoading(true)

                val result = if (token.isSOL) {
                    sendInteractor.sendNativeSolToken(
                        destinationAddress = destinationAddress.toPublicKey(),
                        lamports = tokenAmount.toLamports(token.decimals)
                    )
                } else {
                    sendInteractor.sendSplToken(
                        destinationAddress = destinationAddress.toPublicKey(),
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

    private fun checkDestinationBalance(address: String) {
        if (checkBalanceJob?.isActive == true)
            return

        checkBalanceJob = launch {
            try {
                val balance = userInteractor.getBalance(address.trim())
                shouldAskConfirmation = if (balance == 0L) {
                    view?.showAddressConfirmation()
                    true
                } else {
                    view?.hideAddressConfirmation()
                    false
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error loading destination balance")
                view?.showAddressConfirmation()
            }
        }
    }

    private fun updateButtonText(source: Token.Active) {
        val decimalAmount = inputAmount.toBigDecimalOrZero()
        val isMoreThanBalance = decimalAmount.isMoreThan(source.total)

        when {
            isMoreThanBalance ->
                view?.showButtonText(R.string.swap_funds_not_enough)
            decimalAmount.isZero() ->
                view?.showButtonText(R.string.main_enter_the_amount)
            destinationAddress.isBlank() ->
                view?.showButtonText(R.string.send_enter_address)
            shouldAskConfirmation ->
                view?.showButtonText(R.string.send_make_sure_correct_address)
            else ->
                view?.showButtonText(R.string.send_now)
        }
    }

    private fun setButtonEnabled(amount: BigDecimal, total: BigDecimal) {
        val isMoreThanBalance = amount.isMoreThan(total)
        val isNotZero = !amount.isZero()
        val isEnabled = isNotZero && !isMoreThanBalance

        val isValidAddress = isAddressValid(destinationAddress)
        val isUserNameValid = isUsernameValid(username)
        val availableColor = if (isMoreThanBalance) R.attr.colorAccentWarning else R.attr.colorAccentPrimary
        view?.setAvailableTextColor(availableColor)
        view?.showButtonEnabled(
            isEnabled && (isValidAddress || isUserNameValid) && !shouldAskConfirmation
        )
    }

    private fun isAddressValid(address: String): Boolean =
        address.trim().length >= VALID_ADDRESS_LENGTH

    private fun isUsernameValid(userName: String): Boolean =
        userName.length in VALID_USER_NAME_RANGE
}