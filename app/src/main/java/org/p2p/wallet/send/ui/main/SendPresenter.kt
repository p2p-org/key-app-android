package org.p2p.wallet.send.ui.main

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.utils.PublicKeyValidator
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.CORRECT_AMOUNT
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.NO_ACTION
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.SELECT_FEE_PAYER
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransferType
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.renbtc.interactor.BurnBtcInteractor
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.model.AddressValidation
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.FeePayerState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SearchTarget
import org.p2p.wallet.send.model.SendButton
import org.p2p.wallet.send.model.SendConfirmData
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendPresenterState
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.send.model.SolanaAddress
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.cutEnd
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.toBase58Instance
import org.p2p.wallet.utils.toPublicKey
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.Locale
import java.util.UUID
import kotlin.properties.Delegates

@Deprecated("Will be removed, old design flow")
class SendPresenter(
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
    private val resourcesProvider: ResourcesProvider,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle,
    private val dispatchers: CoroutineDispatchers
) : BasePresenter<SendContract.View>(), SendContract.Presenter {

    companion object {
        private const val ROUNDING_VALUE = 6
    }

    private var token: Token.Active? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    private var state = SendPresenterState()

    private var calculationJob: Job? = null
    private var checkAddressJob: Job? = null
    private var feePayerJob: Job? = null
    private lateinit var transactionId: String

    private var availableTokensToSwitch: List<Token.Active> = emptyList()

    override fun setInitialToken(initialToken: Token.Active) {
        state.updateInitialToken(initialToken)
    }

    override fun loadInitialData() {
        launch(dispatchers.ui) {
            try {
                view?.showFullScreenLoading(true)

                // We should find SOL anyway because SOL is needed for Selection Mechanism
                state.solToken = userInteractor.getUserSolToken()
                val initialToken = state.initialToken ?: state.solToken ?: error("SOL account is not found")

                sendInteractor.initialize(initialToken).also { token = initialToken }
                state.minRentExemption = sendInteractor.getMinRelayRentExemption()
                availableTokensToSwitch = userInteractor.getNonZeroUserTokens()

                calculateTotal(sendFeeRelayerFee = null)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading initial data")
            } finally {
                view?.showFullScreenLoading(false)
            }
        }
    }

    override fun setSourceToken(newToken: Token.Active) {
        token = newToken
        view?.showDetailsError(null)
        val validatedResult = validateResultByNetwork(result = state.searchResult)
        handleTargetResult(validatedResult)

        calculateByMode(newToken)
        checkAddress(state.searchResult?.addressState?.address)
        updateMaxButtonVisibility(newToken)
        sendAnalytics.logSendChangingToken(newToken.tokenSymbol)

        findValidFeePayer(sourceToken = newToken, feePayerToken = newToken, strategy = CORRECT_AMOUNT)
    }

    override fun setTargetResult(result: SearchResult?) {
        if (result is SearchResult.UsernameFound && result.username.isNotBlank()) {
            sendAnalytics.isSendTargetUsername = true
        }

        val validatedResult = validateResultByNetwork(result = result)

        state.searchResult = validatedResult

        handleTargetResult(validatedResult)

        val sourceToken = token ?: return
        calculateByMode(sourceToken)
    }

    private fun handleTargetResult(validatedResult: SearchResult?) {
        when (validatedResult) {
            is SearchResult.UsernameFound -> handleFullResult(validatedResult)
            is SearchResult.AddressFound -> handleAddressOnlyResult(validatedResult)
            else -> handleIdleTarget()
        }
    }

    private fun validateResultByNetwork(result: SearchResult?): SearchResult? {
        val addressState = result?.addressState ?: return null

        val address = addressState.address

        val isValid = PublicKeyValidator.isValid(address)
        return if (isValid) {
            result
        } else {
            null
        }
    }

    override fun validateTargetAddress(value: String) {
        launch {
            try {
                sendAnalytics.logFillingAddress()
                view?.showIndeterminateLoading(true)

                val target = SearchTarget(value, usernameDomainFeatureToggle.value)

                when (target.validation) {
                    SearchTarget.Validation.USERNAME -> {
                        searchByUsername(target.trimmedUsername)
                    }
                    SearchTarget.Validation.BTC_ADDRESS -> {
                        setBitcoinTargetResult(target.value)
                    }
                    SearchTarget.Validation.SOLANA_TYPE_ADDRESS -> {
                        searchBySolAddress(target.value.toBase58Instance())
                    }
                    SearchTarget.Validation.EMPTY -> {
                        view?.showIdleTarget()
                        sendAnalytics.isSendTargetUsername = false
                    }
                    SearchTarget.Validation.INVALID -> {
                        view?.showWrongAddressTarget(target.value)
                        sendAnalytics.isSendTargetUsername = false
                    }
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error validating target: $value")
            } finally {
                view?.showIndeterminateLoading(false)
            }
        }
    }

    override fun setNewSourceAmount(amount: String) {
        state.inputAmount = amount

        val token = token ?: return
        updateMaxButtonVisibility(token)
        calculateByMode(token)

        /*
        * Calculating if we can pay with current token instead of already selected fee payer token
        * */
        findValidFeePayer(sourceToken = token, feePayerToken = token, strategy = SELECT_FEE_PAYER)
    }

    override fun send() {
        val token = token ?: error("Token cannot be null!")
        val address = state.searchResult?.addressState?.address ?: error("Target address cannot be null!")

        sendAnalytics.logSendStarted(
            sendAmount = state.tokenAmount,
            sendToken = token,
            fee = state.sendFeeRelayerFee,
            usdAmount = state.usdAmount
        )

        sendAnalytics.logConfirmButtonPressed(
            sendCurrency = token.tokenSymbol,
            sendSum = state.inputAmount,
            sendSumInUsd = state.usdAmount.toPlainString(),
            isSendFree = state.sendFeeRelayerFee == null,
            accountFeeTokenSymbol = state.sendFeeRelayerFee?.feePayerSymbol
        )

        sendAnalytics.logIsSendByUsername()
        sendInSolana(token, address)
    }

    override fun sendOrConfirm() {
        val token = token ?: error("Token cannot be null!")
        val address = state.searchResult?.addressState?.address ?: error("Target address cannot be null!")

        sendAnalytics.logUserConfirmedSend(
            sendAmount = state.tokenAmount,
            sendToken = token,
            fee = state.sendFeeRelayerFee,
            usdAmount = state.usdAmount
        )

        val isConfirmationRequired = settingsInteractor.isBiometricsConfirmationEnabled()
        if (isConfirmationRequired) {
            val data = SendConfirmData(
                token = token,
                amount = state.tokenAmount.toPlainString(),
                amountUsd = state.usdAmount.toPlainString(),
                destination = address
            )

            // TODO resolve [sendMax, sendFree,sendUsername]
            sendAnalytics.logSendReviewing(
                sendCurrency = token.tokenSymbol,
                sendSum = state.tokenAmount,
                sendUSD = state.usdAmount,
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

            view?.navigateToTokenSelection(result, token)
        }
    }

    override fun setMaxSourceAmountValue() {
        val token = token ?: return

        val totalAvailable = when (state.mode) {
            is CurrencyMode.Fiat -> token.totalInUsd
            is CurrencyMode.Token -> token.total.scaleLong()
        } ?: return

        view?.showInputValue(totalAvailable, forced = false)

        val message = resourcesProvider.getString(R.string.send_using_max_amount, token.tokenSymbol)
        view?.showUiKitSnackBar(message)
        sendAnalytics.isSendMaxButtonClickedOnce = true

        state.inputAmount = totalAvailable.toString()

        updateMaxButtonVisibility(token)
        calculateByMode(token)

        /*
        * Calculating if we can pay with current token instead of already selected fee payer token
        * */
        findValidFeePayer(sourceToken = token, feePayerToken = token, strategy = CORRECT_AMOUNT)
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
                token?.let { findValidFeePayer(sourceToken = it, feePayerToken = feePayerToken, NO_ACTION) }
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
        state.mode = when (state.mode) {
            is CurrencyMode.Token -> {
                sendCurrency = USD_READABLE_SYMBOL
                CurrencyMode.Fiat.Usd
            }
            is CurrencyMode.Fiat -> {
                sendCurrency = token.tokenSymbol.uppercase(Locale.getDefault())
                CurrencyMode.Token(token)
            }
        }
        sendAnalytics.logSendChangingCurrency(sendCurrency)
        updateMaxButtonVisibility(token)
        calculateByMode(token)
    }

    internal fun updateMaxButtonVisibility(token: Token.Active) {
        val totalAvailable = when (state.mode) {
            is CurrencyMode.Fiat -> token.totalInUsd
            is CurrencyMode.Token -> token.total.scaleLong()
        } ?: return
        view?.setMaxButtonVisibility(isVisible = state.inputAmount != totalAvailable.toString())
    }

    private fun handleFullResult(result: SearchResult.UsernameFound) {
        val isKeyAppUsername = result.username.endsWith(usernameDomainFeatureToggle.value)
        view?.showUsernameTarget(result.addressState.address, result.username, isKeyAppUsername)
        checkAddress(result.addressState.address)
    }

    private fun handleAddressOnlyResult(result: SearchResult.AddressFound) {
        view?.showAddressOnlyTarget(result.addressState.address)
        checkAddress(result.addressState.address)
    }

//    private fun handleWrongResult(result: SearchResult.InvalidAddress) {
//        view?.showWrongAddressTarget(result.addressState.address.cutEnd())
//        view?.hideAccountFeeView()
//    }

    private fun handleIdleTarget() {
        view?.showIdleTarget()
        view?.showTotal(data = null)
        view?.hideAccountFeeView()

        state.sendFeeRelayerFee = null
        calculateTotal(sendFeeRelayerFee = null)
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
                token?.let { findValidFeePayer(sourceToken = it, feePayerToken = it, strategy = CORRECT_AMOUNT) }
            is SolanaAddress.AccountExists,
            is SolanaAddress.InvalidAddress -> {
                calculateTotal(sendFeeRelayerFee = null)
                view?.hideAccountFeeView()
            }
        }
    }

    private fun sendInSolana(token: Token.Active, address: String) {
        launch {
            try {
                transactionId = UUID.randomUUID().toString()
                val destinationAddress = address.toPublicKey()
                val lamports = state.tokenAmount.toLamports(token.decimals)

                when (val validation = addressInteractor.validateAddress(destinationAddress, token.mintAddress)) {
                    is AddressValidation.WrongWallet -> {
                        view?.showWrongWalletError()
                        view?.showProgressDialog(transactionId, null)
                    }
                    is AddressValidation.Error -> {
                        view?.showErrorMessage(validation.messageRes)
                        view?.showProgressDialog(transactionId, null)
                    }
                    is AddressValidation.Valid -> {
                        handleValidAddress(token, destinationAddress, lamports)
                    }
                }
            } catch (e: CancellationException) {
                Timber.e(e, "Sending was cancelled")
            } catch (serverError: ServerException) {
                val state = TransactionState.Error(
                    serverError.getErrorMessage(resourcesProvider.resources).orEmpty()
                )
                transactionManager.emitTransactionState(transactionId, state)
            } catch (e: Throwable) {
                Timber.e(e, "Error sending token")
                view?.showProgressDialog(transactionId, null)
                view?.showErrorMessage(e)
            }
        }
    }

    private suspend fun handleValidAddress(
        token: Token.Active,
        destinationAddress: PublicKey,
        lamports: BigInteger
    ) {
        val destinationAddressShort = destinationAddress.toBase58().cutMiddle()
        val data = ShowProgress(
            title = R.string.send_transaction_being_processed,
            subTitle = "${state.tokenAmount.toPlainString()} ${token.tokenSymbol} → $destinationAddressShort",
            transactionId = emptyString()
        )
        view?.showProgressDialog(transactionId, data)

        val result = sendInteractor.sendTransaction(
            destinationAddress = destinationAddress,
            token = token,
            lamports = lamports
        )
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Send.SUCCESS)
        val transaction = buildTransaction(result)
        val transactionState = TransactionState.SendSuccess(transaction, token.tokenSymbol)
        transactionManager.emitTransactionState(transactionId, transactionState)
        sendAnalytics.logSendCompleted(
            state.tokenAmount,
            token,
            state.sendFeeRelayerFee,
            state.usdAmount
        )
    }

    private fun buildTransaction(transactionId: String): HistoryTransaction =
        HistoryTransaction.Transfer(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = null,
            type = TransferType.SEND,
            senderAddress = tokenKeyProvider.publicKey,
            tokenData = TokenConverter.toTokenData(token!!),
            totalInUsd = state.usdAmount,
            total = state.tokenAmount,
            destination = state.searchResult!!.addressState.address,
            fee = BigInteger.ZERO,
            status = TransactionStatus.PENDING
        )

    internal fun calculateByMode(token: Token.Active) {
        if (calculationJob?.isActive == true) return

        launch(dispatchers.ui) {
            when (state.mode) {
                is CurrencyMode.Token -> calculateByToken(token)
                is CurrencyMode.Fiat -> calculateByUsd(token)
            }
        }.also { calculationJob = it }
    }

    private fun calculateByUsd(token: Token.Active) {
        state.usdAmount = state.inputAmount.toBigDecimalOrZero()
        state.tokenAmount = if (token.usdRateOrZero.isZero()) {
            BigDecimal.ZERO
        } else {
            state.usdAmount.divide(token.usdRateOrZero, ROUNDING_VALUE, RoundingMode.HALF_EVEN)
                .stripTrailingZeros()
        }

        val tokenAround = if (state.usdAmount.isZero() || token.usdRateOrZero.isZero()) {
            BigDecimal.ZERO
        } else {
            state.usdAmount.divide(token.usdRateOrZero, ROUNDING_VALUE, RoundingMode.HALF_EVEN)
                .stripTrailingZeros()
        }
        view?.showTokenAroundValue(tokenAround, token.tokenSymbol)
        view?.showAvailableValue(token.totalInUsd ?: BigDecimal.ZERO, USD_READABLE_SYMBOL)
    }

    private fun calculateByToken(token: Token.Active) {
        state.tokenAmount = state.inputAmount.toBigDecimalOrZero()
        state.usdAmount = state.tokenAmount.multiply(token.usdRateOrZero)

        val usdAround = state.tokenAmount.times(token.usdRateOrZero)
        val total = token.total.scaleLong()
        view?.showUsdAroundValue(usdAround)
        view?.showAvailableValue(total, token.tokenSymbol)
    }

    internal fun calculateTotal(sendFeeRelayerFee: SendSolanaFee?) {
        val sourceToken = token ?: return

        val data = SendFeeTotal(
            currentAmount = state.tokenAmount,
            currentAmountUsd = state.usdAmount,
            receive = "${state.tokenAmount.formatToken()} ${sourceToken.tokenSymbol}",
            receiveUsd = state.tokenAmount.toUsd(sourceToken),
            sendFee = sendFeeRelayerFee,
            sourceSymbol = sourceToken.tokenSymbol,
            // FIXME: these two fields are not used in the old send. remove when new send is released
            feeLimit = FreeTransactionFeeLimit(0, 0, BigInteger.ZERO, BigInteger.ZERO),
            recipientAddress = emptyString()
        )

        updateButton(sourceToken, data.sendFee)

        view?.showTotal(data)
    }

    /**
     * Launches the auto-selection mechanism
     * Selects automatically the fee payer token if there is enough balance
     * */
    internal fun findValidFeePayer(
        sourceToken: Token.Active,
        feePayerToken: Token.Active?,
        strategy: FeePayerSelectionStrategy
    ) {
        val feePayer = feePayerToken ?: sendInteractor.getFeePayerToken()

        feePayerJob?.cancel()
        launch(dispatchers.ui) {
            try {
                view?.showAccountFeeViewLoading(isLoading = true)
                val fee = calculateFeeRelayerFee(
                    sourceToken = sourceToken,
                    feePayerToken = feePayer,
                    result = state.searchResult
                ) ?: return@launch
                showFeeDetails(sourceToken, fee, feePayer, strategy)
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
    internal suspend fun calculateFeeRelayerFee(
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        result: SearchResult?
    ): FeeRelayerFee? {
        val recipient = result?.addressState?.address ?: return null

        val fees = try {
            sendInteractor.calculateFeesForFeeRelayer(
                feePayerToken = feePayerToken,
                token = sourceToken,
                recipient = recipient
            )
        } catch (noPoolsException: IllegalStateException) {
            val sol = userInteractor.getUserSolToken()
            if (sol != null) {
                sendInteractor.calculateFeesForFeeRelayer(
                    feePayerToken = sol,
                    token = sourceToken,
                    recipient = recipient
                )
            } else {
                handleError()
                return null
            }
        } catch (e: CancellationException) {
            Timber.i("Fee calculation is cancelled")
            return null
        } catch (e: Throwable) {
            Timber.e(e, "Error calculating fees")
            handleError()
            return null
        }

        /*
         * Checking if fee or feeInPayingToken are null
         * feeInPayingToken can be null only for renBTC network
         * */
        if (fees?.totalInSpl == null || sourceToken.isSOL) {
            state.sendFeeRelayerFee = null
            calculateTotal(sendFeeRelayerFee = null)
            view?.hideAccountFeeView()
            return null
        }

        return fees
    }

    private fun handleError() {
        state.sendFeeRelayerFee = null
        calculateTotal(sendFeeRelayerFee = null)
        view?.hideAccountFeeView()
        view?.showDetailsError(R.string.send_cannot_send_token)
        view?.showButtonText(R.string.main_select_token)
    }

    internal suspend fun showFeeDetails(
        sourceToken: Token.Active,
        feeRelayerFee: FeeRelayerFee,
        feePayerToken: Token.Active,
        strategy: FeePayerSelectionStrategy
    ) {
        val fee = buildSolanaFee(feePayerToken, sourceToken, feeRelayerFee)

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
        val feeRelayerFee = calculateFeeRelayerFee(
            sourceToken = sourceToken,
            feePayerToken = newFeePayer,
            result = state.searchResult
        ) ?: return
        val fee = buildSolanaFee(newFeePayer, sourceToken, feeRelayerFee)
        showFees(sourceToken, fee)
        calculateTotal(fee)
    }

    private fun reduceInputAmount(maxAllowedAmount: BigInteger) {
        val token = token ?: return

        val newInputAmount = maxAllowedAmount.fromLamports(token.decimals).scaleLong()
        val totalInput = when (state.mode) {
            is CurrencyMode.Fiat -> newInputAmount.toUsd(token)
            is CurrencyMode.Token -> newInputAmount
        } ?: return

        view?.showInputValue(totalInput, forced = true)

        state.inputAmount = totalInput.toPlainString()

        updateMaxButtonVisibility(token)
        calculateByMode(token)
    }

    internal fun buildSolanaFee(
        newFeePayer: Token.Active,
        source: Token.Active,
        feeRelayerFee: FeeRelayerFee
    ): SendSolanaFee {

        return SendSolanaFee(
            feePayerToken = newFeePayer,
            sourceToken = source,
            feeRelayerFee = feeRelayerFee,
            solToken = state.solToken,
            alternativeFeePayerTokens = availableTokensToSwitch
        ).also { state.sendFeeRelayerFee = it }
    }

    private fun showFees(source: Token.Active, fee: SendSolanaFee) {
        val inputAmount = state.tokenAmount.toLamports(source.decimals)
        val isEnoughToCoverExpenses = fee.isEnoughToCoverExpenses(
            sourceTokenTotal = source.totalInLamports,
            inputAmount = inputAmount
        )

        if (isEnoughToCoverExpenses) {
            view?.showAccountFeeView(fee = fee)
        } else {
            view?.showInsufficientFundsView(source.tokenSymbol, fee.feeUsd)
        }
    }

    private suspend fun validateAndSelectFeePayer(
        sourceToken: Token.Active,
        fee: SendSolanaFee,
        strategy: FeePayerSelectionStrategy
    ) {

        // Assuming token is not SOL
        val inputAmount = state.tokenAmount.toLamports(sourceToken.decimals)
        val tokenTotal = sourceToken.total.toLamports(sourceToken.decimals)

        /*
         * Checking if fee payer is SOL, otherwise fee payer is already correctly set up
         * - if there is enough SPL balance to cover fee, setting the default fee payer as SPL token
         * - if there is not enough SPL/SOL balance to cover fee, trying to reduce input amount
         * - In other cases, switching to SOL
         * */
        when (val state = fee.calculateFeePayerState(strategy, tokenTotal, inputAmount)) {
            is FeePayerState.SwitchToSpl -> {
                val tokenToSwitch = state.tokenToSwitch
                sendInteractor.setFeePayerToken(tokenToSwitch)
                recalculate(tokenToSwitch)
            }
            is FeePayerState.SwitchToSol -> {
                sendInteractor.switchFeePayerToSol(this.state.solToken)
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
        sendAnalytics.isSendTargetUsername = true
    }

    private fun setBitcoinTargetResult(address: String) {
        if (token.isRenBTC) {
            val addressState = AddressState(address)
            val result = SearchResult.AddressFound(addressState)
            setTargetResult(result)
        } else {
            view?.showWrongAddressTarget(address)
        }
        sendAnalytics.isSendTargetUsername = false
    }

    private suspend fun searchBySolAddress(address: Base58String) {
        if (!PublicKeyValidator.isValid(address.base58Value)) {
            view?.showWrongAddressTarget(address.base58Value.cutEnd())
            return
        }

        val result = searchInteractor.searchByAddress(address)
        setTargetResult(result)
        sendAnalytics.isSendTargetUsername = false
    }

    private fun updateButton(sourceToken: Token.Active, sendFee: SendSolanaFee?) {
        val sendButton = SendButton(
            sourceToken = sourceToken,
            searchResult = state.searchResult,
            tokenAmount = state.tokenAmount,
            sendFee = sendFee,
            minRentExemption = state.minRentExemption
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
