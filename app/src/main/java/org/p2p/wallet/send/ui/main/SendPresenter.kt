package org.p2p.wallet.send.ui.main

import android.content.res.Resources
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.utils.PublicKeyValidator
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransferType
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.renbtc.interactor.BurnBtcInteractor
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.model.AddressValidation
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.CurrencyMode
import org.p2p.wallet.send.model.FeePayerSelectionStrategy
import org.p2p.wallet.send.model.FeePayerSelectionStrategy.CORRECT_AMOUNT
import org.p2p.wallet.send.model.FeePayerSelectionStrategy.NO_ACTION
import org.p2p.wallet.send.model.FeePayerSelectionStrategy.SELECT_FEE_PAYER
import org.p2p.wallet.send.model.FeePayerState
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchAddress
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendButton
import org.p2p.wallet.send.model.SendConfirmData
import org.p2p.wallet.send.model.SendFee
import org.p2p.wallet.send.model.SendTotal
import org.p2p.wallet.send.model.SolanaAddress
import org.p2p.wallet.send.model.Target
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.transaction.TransactionManager
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.wallet.utils.cutEnd
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.formatToken
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.toBigDecimalOrZero
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toPublicKey
import org.p2p.wallet.utils.toUsd
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.Locale
import kotlin.properties.Delegates

class SendPresenter(
    private val initialToken: Token.Active?,
    private val sendInteractor: SendInteractor,
    private val addressInteractor: TransactionAddressInteractor,
    private val userInteractor: UserInteractor,
    private val searchInteractor: SearchInteractor,
    private val burnBtcInteractor: BurnBtcInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val browseAnalytics: BrowseAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val sendAnalytics: SendAnalytics,
    private val transactionManager: TransactionManager,
    private val resources: Resources
) : BasePresenter<SendContract.View>(), SendContract.Presenter {

    companion object {
        private const val ROUNDING_VALUE = 6
    }

    private var token: Token.Active? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    private var solToken: Token.Active? = null

    private var inputAmount: String = "0"

    private var tokenAmount: BigDecimal = BigDecimal.ZERO
    private var usdAmount: BigDecimal = BigDecimal.ZERO

    private var minRentExemption: BigInteger = BigInteger.ZERO

    private var mode: CurrencyMode = CurrencyMode.Token(initialToken?.tokenSymbol ?: SOL_SYMBOL)

    private var networkType: NetworkType = NetworkType.SOLANA

    private var searchResult: SearchResult? = null

    private var sendFee: SendFee? = null

    private var calculationJob: Job? = null
    private var checkAddressJob: Job? = null

    private var feeJob: Job? = null
    private var feePayerJob: Job? = null

    override fun loadInitialData() {
        launch {
            try {
                view?.showFullScreenLoading(true)
                view?.showNetworkSelectionView(initialToken.isRenBTC)

                val userTokens = userInteractor.getUserTokens()
                val userPublicKey = tokenKeyProvider.publicKey
                solToken = userTokens.find { it.isSOL && it.publicKey == userPublicKey }
                    ?: throw IllegalStateException("No SOL account found")
                token = initialToken ?: solToken

                calculateTotal(sendFee = null)
                view?.showNetworkDestination(networkType)

                sendInteractor.initialize(solToken!!)
                minRentExemption = sendInteractor.getMinRelayRentExemption()
            } catch (e: Throwable) {
                Timber.e(e, "Error loading initial data")
            } finally {
                view?.showFullScreenLoading(false)
            }
        }
    }

    override fun setSourceToken(newToken: Token.Active) {
        token = newToken
        if (newToken.isRenBTC) {
            view?.showNetworkSelectionView(true)
        } else {
            /* reset to default network and updating UI if source is not renBTC */
            networkType = NetworkType.SOLANA
            view?.showNetworkDestination(networkType)
            view?.showNetworkSelectionView(false)
        }

        calculateRenBtcFeeIfNeeded(hideTotal = true)
        calculateByMode(newToken)
        updateMaxButtonVisibility(newToken)
        sendAnalytics.logSendChangingToken(newToken.tokenSymbol)

        findValidFeePayer(newToken, CORRECT_AMOUNT)
    }

    override fun setTargetResult(result: SearchResult?) {
        searchResult = result

        result?.let {
            selectNetworkType(result.searchAddress.networkType)
            if (networkType != result.searchAddress.networkType) {
                view?.showWrongAddressTarget(result.searchAddress.address)
                return
            }
        }

        when (result) {
            is SearchResult.Full -> handleFullResult(result)
            is SearchResult.AddressOnly -> handleAddressOnlyResult(result)
            is SearchResult.EmptyBalance -> handleEmptyBalanceResult(result)
            is SearchResult.Wrong -> handleWrongResult(result)
            else -> handleIdleTarget()
        }

        val sourceToken = token ?: return
        calculateByMode(sourceToken)
    }

    private fun selectNetworkType(networkType: NetworkType) {
        if (!token.isRenBTC) return

        this.networkType = networkType
        calculateRenBtcFeeIfNeeded()

        view?.showNetworkDestination(networkType)
    }

    override fun validateTarget(value: String) {
        launch {
            try {
                view?.showIndeterminateLoading(true)

                val target = Target(value)
                selectNetworkType(target.networkType)

                when (target.validation) {
                    Target.Validation.USERNAME -> searchByUsername(target.trimmedUsername)
                    Target.Validation.BTC_ADDRESS -> setBitcoinTargetResult(target.value)
                    Target.Validation.SOL_ADDRESS -> searchBySolAddress(target.value)
                    Target.Validation.EMPTY -> view?.showIdleTarget()
                    Target.Validation.INVALID -> view?.showWrongAddressTarget(target.value)
                }

                sendAnalytics.logSendPasting()
            } catch (e: Throwable) {
                Timber.e(e, "Error validating target: $value")
            } finally {
                view?.showIndeterminateLoading(false)
            }
        }
    }

    override fun setNewSourceAmount(amount: String) {
        inputAmount = amount

        val token = token ?: return
        updateMaxButtonVisibility(token)
        calculateRenBtcFeeIfNeeded()
        calculateByMode(token)

        /*
        * Calculating if we can pay with current token instead of already selected fee payer token
        * */
        findValidFeePayer(token, SELECT_FEE_PAYER)
    }

    override fun setNetworkDestination(networkType: NetworkType) {
        this.networkType = networkType
        view?.showNetworkDestination(networkType)
        validateSelectedNetwork(networkType)
        calculateRenBtcFeeIfNeeded(hideTotal = true)
    }

    override fun send() {
        val token = token ?: error("Token cannot be null!")
        val address = searchResult?.searchAddress?.address ?: error("Target address cannot be null!")

        sendAnalytics.logSendStarted(networkType, tokenAmount, token, sendFee, usdAmount)

        when (networkType) {
            NetworkType.SOLANA -> sendInSolana(token, address)
            NetworkType.BITCOIN -> sendInBitcoin(token, address)
        }
    }

    override fun sendOrConfirm() {
        val token = token ?: error("Token cannot be null!")
        val address = searchResult?.searchAddress?.address ?: error("Target address cannot be null!")

        sendAnalytics.logUserConfirmedSend(networkType, tokenAmount, token, sendFee, usdAmount)

        val isConfirmationRequired = settingsInteractor.isBiometricsConfirmationEnabled()
        if (isConfirmationRequired) {
            val data = SendConfirmData(
                token = token,
                amount = tokenAmount.toString(),
                amountUsd = usdAmount.toString(),
                destination = address
            )

            // TODO resolve [sendMax, sendFree,sendUsername]
            sendAnalytics.logSendReviewing(
                sendNetwork = networkType,
                sendCurrency = token.tokenSymbol,
                sendSum = tokenAmount,
                sendUSD = usdAmount,
                sendMax = false,
                sendFree = false,
                sendUsername = false
            )
            view?.showBiometricConfirmationPrompt(data)
        } else {
            send()
        }
    }

    override fun loadTokensForSelection() {
        launch {
            val tokens = userInteractor.getUserTokens()
            val result = tokens.filter { token -> !token.isZero }
            browseAnalytics.logTokenListViewed(
                lastScreenName = analyticsInteractor.getPreviousScreenName(),
                tokenListLocation = BrowseAnalytics.TokenListLocation.SEND
            )
            view?.navigateToTokenSelection(result)
        }
    }

    override fun setMaxSourceAmountValue() {
        val token = token ?: return

        val totalAvailable = when (mode) {
            is CurrencyMode.Usd -> token.totalInUsd
            is CurrencyMode.Token -> token.total.scaleLong()
        } ?: return

        view?.showInputValue(totalAvailable, forced = false)

        val message = resources.getString(R.string.send_using_max_amount, token.tokenSymbol)
        view?.showSuccessSnackBar(message)

        inputAmount = totalAvailable.toString()

        updateMaxButtonVisibility(token)
        calculateRenBtcFeeIfNeeded()
        calculateByMode(token)

        /*
        * Calculating if we can pay with current token instead of already selected fee payer token
        * */
        findValidFeePayer(token, CORRECT_AMOUNT)
    }

    override fun loadCurrentNetwork() {
        view?.navigateToNetworkSelection(networkType)
    }

    override fun loadFeePayerTokens() {
        val token = token ?: return
        launch {
            val feePayerTokens = sendInteractor.getFeeTokenAccounts(token.publicKey)
            view?.showFeePayerTokenSelector(feePayerTokens)
        }
    }

    override fun setFeePayerToken(feePayerToken: Token.Active) {
        launch {
            try {
                sendInteractor.setFeePayerToken(feePayerToken)
                findValidFeePayer(feePayerToken, NO_ACTION)
            } catch (e: Throwable) {
                Timber.e(e, "Error updating fee payer token")
            }
        }
    }

    override fun onScanClicked() {
        sendAnalytics.logSendQrScanning()
        view?.showScanner()
    }

    override fun onFeeClicked() {
        launch {
            try {
                sendAnalytics.logSendShowDetailsPressed()
                val freeTransactionsInfo = sendInteractor.getFreeTransactionsInfo()
                view?.showFeeLimitsDialog(freeTransactionsInfo.maxUsage, freeTransactionsInfo.remaining)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading free transactions info")
            }
        }
    }

    override fun switchCurrency() {
        val token = token ?: return
        val sendCurrency: String
        mode = when (mode) {
            is CurrencyMode.Token -> {
                sendCurrency = USD_READABLE_SYMBOL
                CurrencyMode.Usd
            }
            is CurrencyMode.Usd -> {
                sendCurrency = token.tokenSymbol.uppercase(Locale.getDefault())
                CurrencyMode.Token(token.tokenSymbol)
            }
        }
        sendAnalytics.logSendChangingCurrency(sendCurrency)
        updateMaxButtonVisibility(token)
        calculateByMode(token)
    }

    private fun updateMaxButtonVisibility(token: Token.Active) {
        val totalAvailable = when (mode) {
            is CurrencyMode.Usd -> token.totalInUsd
            is CurrencyMode.Token -> token.total.scaleLong()
        } ?: return
        view?.setMaxButtonVisibility(isVisible = inputAmount != totalAvailable.toString())
    }

    private fun validateSelectedNetwork(networkType: NetworkType) {
        val target = searchResult ?: return
        if (target.searchAddress.networkType != networkType) {
            view?.showWrongAddressTarget(target.searchAddress.address)
        } else {
            view?.showAddressOnlyTarget(target.searchAddress.address)
        }
    }

    private fun handleFullResult(result: SearchResult.Full) {
        view?.showFullTarget(result.searchAddress.address, result.username)
        checkAddress(result.searchAddress.address)
    }

    private fun handleAddressOnlyResult(result: SearchResult.AddressOnly) {
        view?.showAddressOnlyTarget(result.searchAddress.address)
        checkAddress(result.searchAddress.address)
    }

    private fun handleEmptyBalanceResult(result: SearchResult.EmptyBalance) {
        view?.showEmptyBalanceTarget(result.searchAddress.address.cutEnd())
        checkAddress(result.searchAddress.address)
    }

    private fun handleWrongResult(result: SearchResult.Wrong) {
        view?.showWrongAddressTarget(result.searchAddress.address.cutEnd())
        view?.hideAccountFeeView()
    }

    private fun handleIdleTarget() {
        view?.showIdleTarget()
        view?.showTotal(data = null)
        view?.hideAccountFeeView()

        sendFee = null
        calculateTotal(sendFee = null)
    }

    private fun checkAddress(address: String?) {
        if (address.isNullOrEmpty()) return
        val token = token ?: return

        checkAddressJob?.cancel()
        checkAddressJob = launch {
            try {
                view?.showIndeterminateLoading(true)
                val checkAddressResult = sendInteractor.checkAddress(address.toPublicKey(), token)
                handleCheckAddressResult(checkAddressResult)
            } catch (e: CancellationException) {
                Timber.w("Cancelled check address request")
            } catch (e: Throwable) {
                Timber.e(e, "Error checking address")
            } finally {
                view?.showIndeterminateLoading(false)
            }
        }
    }

    private fun handleCheckAddressResult(checkAddressResult: SolanaAddress) {
        when (checkAddressResult) {
            is SolanaAddress.NewAccountNeeded ->
                findValidFeePayer(feePayerToken = null, strategy = CORRECT_AMOUNT)
            is SolanaAddress.AccountExists,
            is SolanaAddress.InvalidAddress -> {
                calculateTotal(sendFee = null)
                view?.hideAccountFeeView()
            }
        }
    }

    private fun sendInBitcoin(token: Token.Active, address: String) {
        launch {
            try {
                view?.showLoading(true)
                val amountLamports = tokenAmount.toLamports(token.decimals)
                val transactionId = burnBtcInteractor.submitBurnTransaction(address, amountLamports)

                Timber.d("Bitcoin successfully burned and released! $transactionId")
                sendAnalytics.logSendCompleted(networkType, tokenAmount, token, sendFee, usdAmount)

                val transaction = buildTransaction(transactionId)
                view?.showTransactionDetails(transaction)
                view?.showTransactionStatusMessage(tokenAmount, token.tokenSymbol, isSuccess = true)
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
                val destinationAddress = address.toPublicKey()
                val lamports = tokenAmount.toLamports(token.decimals)

                when (val validation = addressInteractor.validateAddress(destinationAddress, token.mintAddress)) {
                    is AddressValidation.WrongWallet -> {
                        view?.showWrongWalletError()
                        view?.showProgressDialog(null)
                    }
                    is AddressValidation.Error -> {
                        view?.showErrorMessage(validation.messageRes)
                        view?.showProgressDialog(null)
                    }
                    is AddressValidation.Valid -> {
                        handleValidAddress(token, destinationAddress, lamports)
                    }
                }
            } catch (e: CancellationException) {
                Timber.e(e, "Sending was cancelled")
            } catch (serverError: ServerException) {
                val state = TransactionState.Error(
                    serverError.getErrorMessage(resources).orEmpty()
                )
                transactionManager.emitTransactionState(state)
            } catch (e: Throwable) {
                Timber.e(e, "Error sending token")
                view?.showProgressDialog(null)
                view?.showErrorMessage(e)
            }
        }
    }

    private suspend fun handleValidAddress(
        token: Token.Active,
        destinationAddress: PublicKey,
        lamports: BigInteger
    ) {
        val data = ShowProgress(
            title = R.string.send_transaction_being_processed,
            subTitle = "$tokenAmount ${token.tokenSymbol} → ${destinationAddress.toBase58().cutMiddle()}",
            transactionId = emptyString()
        )
        view?.showProgressDialog(data)

        val result = sendInteractor.sendTransaction(
            destinationAddress = destinationAddress,
            token = token,
            lamports = lamports
        )
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Send.SUCCESS)
        val transaction = buildTransaction(result)
        val state = TransactionState.SendSuccess(transaction, token.tokenSymbol)
        transactionManager.emitTransactionState(state)
        sendAnalytics.logSendCompleted(networkType, tokenAmount, token, sendFee, usdAmount)
    }

    private fun buildTransaction(transactionId: String): HistoryTransaction =
        HistoryTransaction.Transfer(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = null,
            type = TransferType.SEND,
            senderAddress = tokenKeyProvider.publicKey,
            tokenData = TokenConverter.toTokenData(token!!),
            totalInUsd = usdAmount,
            total = tokenAmount,
            destination = searchResult!!.searchAddress.address,
            fee = BigInteger.ZERO,
            status = TransactionStatus.PENDING
        )

    private fun calculateByMode(token: Token.Active) {
        if (calculationJob?.isActive == true) return

        launch {
            when (mode) {
                is CurrencyMode.Token -> calculateByToken(token)
                is CurrencyMode.Usd -> calculateByUsd(token)
            }
        }.also { calculationJob = it }
    }

    private fun calculateByUsd(token: Token.Active) {
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
        view?.showAvailableValue(token.totalInUsd ?: BigDecimal.ZERO, USD_READABLE_SYMBOL)
    }

    private fun calculateByToken(token: Token.Active) {
        tokenAmount = inputAmount.toBigDecimalOrZero()
        usdAmount = tokenAmount.multiply(token.usdRateOrZero)

        val usdAround = tokenAmount.times(token.usdRateOrZero)
        val total = token.total.scaleLong()
        view?.showUsdAroundValue(usdAround)
        view?.showAvailableValue(total, token.tokenSymbol)
    }

    private fun calculateTotal(sendFee: SendFee?) {
        val sourceToken = token ?: return

        val data = SendTotal(
            total = tokenAmount,
            totalUsd = usdAmount,
            receive = "${tokenAmount.formatToken()} ${sourceToken.tokenSymbol}",
            receiveUsd = tokenAmount.toUsd(sourceToken),
            fee = sendFee,
            sourceSymbol = sourceToken.tokenSymbol
        )

        updateButton(sourceToken, data.fee)

        view?.showTotal(data)
    }

    private fun calculateRenBtcFeeIfNeeded(hideTotal: Boolean = false) {
        if (networkType == NetworkType.SOLANA) {
            if (hideTotal) view?.showTotal(null)
            return
        }

        if (feeJob?.isActive == true) return
        val sourceToken = token ?: return
        view?.hideAccountFeeView()

        launch {
            val fee = burnBtcInteractor.getBurnFee()
            calculateTotal(SendFee.RenBtcFee(sourceToken, fee))
        }
    }

    /**
     * Launches the auto-selection mechanism
     * Selects automatically the fee payer token if there is enough balance
     * */
    private fun findValidFeePayer(feePayerToken: Token.Active?, strategy: FeePayerSelectionStrategy) {
        val feePayer = feePayerToken ?: sendInteractor.getFeePayerToken()
        val token = token ?: return

        feePayerJob?.cancel()
        launch {
            try {
                view?.showAccountFeeViewLoading(isLoading = true)
                val (feeInSol, feeInPayingToken) = calculateFeeRelayerFee(token, feePayer) ?: return@launch
                showFeeDetails(token, feeInSol, feeInPayingToken, feePayer, strategy)
            } catch (e: Throwable) {
                Timber.e(e, "Error during FeeRelayer fee calculation")
            } finally {
                view?.showAccountFeeViewLoading(isLoading = false)
            }
        }.also { feePayerJob = it }
    }

    /*
    * Assume this to be called only if associated account address creation needed
    * */
    private suspend fun calculateFeeRelayerFee(
        sourceToken: Token.Active,
        feePayerToken: Token.Active
    ): Pair<BigInteger, BigInteger>? {
        val receiver = searchResult?.searchAddress?.address ?: return null

        val fees = sendInteractor.calculateFeesForFeeRelayer(
            feePayerToken = feePayerToken,
            token = sourceToken,
            receiver = receiver
        )

        /*
         * Checking if fee or feeInPayingToken are null
         * feeInPayingToken can be null only for renBTC network
         * */
        if (fees?.feeInPayingToken == null || sourceToken.isSOL) {
            sendFee = null
            calculateTotal(sendFee = null)
            view?.hideAccountFeeView()
            return null
        }

        return fees.feeInSol to fees.feeInPayingToken
    }

    private suspend fun showFeeDetails(
        sourceToken: Token.Active,
        feeInSol: BigInteger,
        feeInPayingToken: BigInteger,
        feePayerToken: Token.Active,
        strategy: FeePayerSelectionStrategy
    ) {
        val fee = buildSolanaFee(feePayerToken, sourceToken, feeInSol, feeInPayingToken)

        if (strategy == NO_ACTION) {
            showFees(sourceToken, fee)
            calculateTotal(fee)
        } else {
            validateAndSelectFeePayer(sourceToken, fee, strategy)
        }
    }

    private suspend fun recalculate(sourceToken: Token.Active) {
        /*
         * Optimized recalculation and UI update
         * */
        val newFeePayer = sendInteractor.getFeePayerToken()
        val (feeInSol, feeInPayingToken) = calculateFeeRelayerFee(sourceToken, newFeePayer) ?: return
        val fee = buildSolanaFee(newFeePayer, sourceToken, feeInSol, feeInPayingToken)
        showFees(sourceToken, fee)
        calculateTotal(fee)
    }

    private fun reduceInputAmount(maxAllowedAmount: BigInteger) {
        val token = token ?: return

        val newInputAmount = maxAllowedAmount.fromLamports(token.decimals).scaleLong()
        val totalInput = when (mode) {
            is CurrencyMode.Usd -> newInputAmount.toUsd(token)
            is CurrencyMode.Token -> newInputAmount
        } ?: return

        view?.showInputValue(totalInput, forced = true)

        inputAmount = totalInput.toPlainString()

        updateMaxButtonVisibility(token)
        calculateByMode(token)
    }

    private fun buildSolanaFee(
        newFeePayer: Token.Active,
        source: Token.Active,
        feeInSol: BigInteger,
        feeInPayingToken: BigInteger,
    ): SendFee.SolanaFee {

        return SendFee.SolanaFee(
            feePayerToken = newFeePayer,
            sourceTokenSymbol = source.tokenSymbol,
            feeLamports = feeInSol,
            feeInPayingToken = feeInPayingToken
        ).also { sendFee = it }
    }

    private fun showFees(source: Token.Active, fee: SendFee.SolanaFee) {
        val inputAmount = tokenAmount.toLamports(source.decimals)
        val isEnoughToCoverExpenses = fee.isEnoughToCoverExpenses(
            sourceTokenTotal = source.totalInLamports,
            inputAmount = inputAmount
        )

        if (isEnoughToCoverExpenses) {
            view?.showAccountFeeView(fee = fee)
        } else {
            view?.showInsufficientFundsView(source.tokenSymbol, fee.feeUsd?.toPlainString())
        }
    }

    private suspend fun validateAndSelectFeePayer(
        sourceToken: Token.Active,
        fee: SendFee.SolanaFee,
        strategy: FeePayerSelectionStrategy
    ) {

        // Assuming token is not SOL
        val inputAmount = tokenAmount.toLamports(sourceToken.decimals)
        val tokenTotal = sourceToken.total.toLamports(sourceToken.decimals)

        /*
         * Checking if fee payer is SOL, otherwise fee payer is already correctly set up
         * - if there is enough SPL balance to cover fee, setting the default fee payer as SPL token
         * - if there is not enough SPL/SOL balance to cover fee, trying to reduce input amount
         * - In other cases, switching to SOL
         * */
        when (val state = fee.calculateFeePayerState(strategy, tokenTotal, inputAmount)) {
            is FeePayerState.UpdateFeePayer -> {
                sendInteractor.setFeePayerToken(sourceToken)
                recalculate(sourceToken)
            }
            is FeePayerState.SwitchToSol -> {
                sendInteractor.switchFeePayerToSol(solToken)
                recalculate(sourceToken)
            }
            is FeePayerState.ReduceInputAmount -> {
                sendInteractor.setFeePayerToken(sourceToken)
                reduceInputAmount(state.maxAllowedAmount)
                recalculate(sourceToken)
            }
        }
    }

    private suspend fun searchByUsername(username: String) {
        val usernames = searchInteractor.searchByName(username)
        if (usernames.isEmpty()) {
            view?.showWrongAddressTarget(username)
            return
        }

        if (usernames.size > 1) {
            view?.showSearchScreen(usernames)
            return
        }

        val result = usernames.first()
        setTargetResult(result)
    }

    private fun setBitcoinTargetResult(address: String) {
        if (!token.isRenBTC) {
            view?.showWrongAddressTarget(address)
        } else {
            setTargetResult(SearchResult.AddressOnly(SearchAddress(address, NetworkType.BITCOIN)))
        }
    }

    private suspend fun searchBySolAddress(address: String) {
        if (!PublicKeyValidator.isValid(address)) {
            view?.showWrongAddressTarget(address.cutEnd())
            return
        }

        val results = searchInteractor.searchByAddress(address)
        if (results.isEmpty()) return

        val first = results.first()
        setTargetResult(first)
    }

    private fun updateButton(sourceToken: Token.Active, sendFee: SendFee?) {
        val sendButton = SendButton(
            sourceToken = sourceToken,
            searchResult = searchResult,
            tokenAmount = tokenAmount,
            sendFee = sendFee,
            minRentExemption = minRentExemption
        )

        when (val state = sendButton.state) {
            is SendButton.State.Disabled -> {
                view?.showButtonText(state.textResId)
                view?.showButtonEnabled(isEnabled = false)
                view?.setTotalAmountTextColor(textColor = state.totalAmountTextColor)
                view?.showWarning(state.warningTextResId)
            }
            is SendButton.State.Enabled -> {
                view?.showButtonText(state.textResId, state.iconRes, value = state.value)
                view?.showButtonEnabled(isEnabled = true)
                view?.setTotalAmountTextColor(textColor = state.totalAmountTextColor)
            }
        }
    }

    private val Token.Active?.isRenBTC: Boolean
        get() = this?.isRenBTC == true
}
