package org.p2p.wallet.send.ui

import android.content.res.Resources
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date
import java.util.UUID
import kotlin.properties.Delegates.observable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.common.di.AppScope
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.model.CurrencyMode
import org.p2p.core.model.TitleValue
import org.p2p.core.token.Token
import org.p2p.core.token.findByMintAddress
import org.p2p.core.token.sortedWithPreferredStableCoins
import org.p2p.core.utils.asNegativeUsdTransaction
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.CORRECT_AMOUNT
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.NO_ACTION
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.SELECT_FEE_PAYER
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.send.SendFeeRelayerManager
import org.p2p.wallet.send.analytics.NewSendAnalytics
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.CalculationMode
import org.p2p.wallet.send.model.FeeLoadingState
import org.p2p.wallet.send.model.FeeRelayerState
import org.p2p.wallet.send.model.FeeRelayerStateError
import org.p2p.wallet.send.model.NewSendButtonState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendFatalError
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.send.model.getFee
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.model.progressstate.SendSwapProgressState
import org.p2p.wallet.updates.NetworkConnectionStateProvider
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.toPublicKey

private const val TAG = "NewSendPresenter"

class NewSendPresenter(
    private val recipientAddress: SearchResult,
    private val openedFrom: SendOpenedFrom,
    private val userInteractor: UserInteractor,
    private val userTokensInteractor: UserTokensInteractor,
    private val sendInteractor: SendInteractor,
    private val resources: Resources,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionManager: TransactionManager,
    private val connectionStateProvider: NetworkConnectionStateProvider,
    private val newSendAnalytics: NewSendAnalytics,
    private val alertErrorsLogger: AlarmErrorsLogger,
    private val appScope: AppScope,
    sendModeProvider: SendModeProvider,
    private val historyInteractor: HistoryInteractor,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val sendFeeRelayerManager: SendFeeRelayerManager
) : BasePresenter<NewSendContract.View>(), NewSendContract.Presenter {

    private val flow: NewSendAnalytics.AnalyticsSendFlow = if (openedFrom == SendOpenedFrom.SELL_FLOW) {
        NewSendAnalytics.AnalyticsSendFlow.SELL
    } else {
        NewSendAnalytics.AnalyticsSendFlow.SEND
    }

    private var token: Token.Active? by observable(null) { _, _, newToken ->
        if (newToken != null) {
            view?.showToken(newToken)
            calculationMode.updateToken(newToken)
        }
    }

    private val calculationMode = CalculationMode(
        sendModeProvider = sendModeProvider,
        lessThenMinString = resources.getString(R.string.common_less_than_minimum)
    )

    private var selectedToken: Token.Active? = null
    private var initialAmount: BigDecimal? = null

    private var feePayerJob: Job? = null

    override fun attach(view: NewSendContract.View) {
        super.attach(view)
        newSendAnalytics.logNewSendScreenOpened(flow)
        initialize(view)
        subscribeToSelectedTokenUpdates()
    }

    private fun subscribeToSelectedTokenUpdates() {
        userTokensInteractor.observeUserTokens()
            .map { it.findByMintAddress(token?.mintAddress ?: selectedToken?.mintAddress) }
            .filterNotNull()
            .onEach { token = it }
            .launchIn(this)
    }

    override fun setInitialData(selectedToken: Token.Active?, inputAmount: BigDecimal?) {
        this.selectedToken = selectedToken
        this.initialAmount = inputAmount
    }

    private fun initialize(view: NewSendContract.View) {
        view.disableSwitchAmounts()

        calculationMode.onCalculationCompleted = view::showAroundValue
        calculationMode.onInputFractionUpdated = view::updateInputFraction
        calculationMode.onLabelsUpdated = { switchSymbol, mainSymbol ->
            view.setSwitchLabel(switchSymbol)
            view.setMainAmountLabel(mainSymbol)
        }

        sendFeeRelayerManager.onStateUpdated = { newState ->
            handleFeeRelayerStateUpdate(newState, view)
        }
        sendFeeRelayerManager.onFeeLoading = { loadingState ->
            when (loadingState) {
                is FeeLoadingState.Instant -> view.showFeeViewLoading(isLoading = loadingState.isLoading)
                is FeeLoadingState.Delayed -> view.showDelayedFeeViewLoading(isLoading = loadingState.isLoading)
            }
        }

        if (token != null) {
            restoreSelectedToken(view, requireToken())
        } else {
            setupInitialToken(view)
        }
    }

    private fun restoreSelectedToken(view: NewSendContract.View, token: Token.Active) {
        launch {
            view.showToken(token)
            calculationMode.updateToken(token)
            checkTokenRatesAndSetSwitchAmountState(token)

            val userTokens = getNonZeroUserTokensOrSoul()
            val isTokenChangeEnabled = userTokens.size > 1 && selectedToken == null
            view.setTokenContainerEnabled(isEnabled = isTokenChangeEnabled)

            val currentState = sendFeeRelayerManager.getState()
            handleFeeRelayerStateUpdate(currentState, view)
        }
    }

    private fun setupInitialToken(view: NewSendContract.View) {
        launch {
            // We should find SOL anyway because SOL is needed for Selection Mechanism
            // todo: check this logic as user definitely may have empty account, we should not error by this reason
            val userNonZeroTokens = getNonZeroUserTokensOrSoul()
            if (userNonZeroTokens.isEmpty()) {
                Timber.tag(TAG).e(SendFatalError("User non-zero tokens can't be empty!"))
                // we cannot proceed if user tokens are not loaded
                view.showUiKitSnackBar(resources.getString(R.string.error_general_message))
                return@launch
            }

            val isTokenChangeEnabled = userNonZeroTokens.size > 1 && selectedToken == null
            view.setTokenContainerEnabled(isEnabled = isTokenChangeEnabled)

            val initialToken = if (selectedToken != null) selectedToken!! else userNonZeroTokens.first()
            token = initialToken

            checkTokenRatesAndSetSwitchAmountState(initialToken)

            val solToken = if (initialToken.isSOL) initialToken else tokenServiceCoordinator.getUserSolToken()
            if (solToken == null) {
                // we cannot proceed without SOL.
                view.showUiKitSnackBar(resources.getString(R.string.error_general_message))
                Timber.tag(TAG).e(SendFatalError("Couldn't find user's SOL account!"))
                return@launch
            }

            initializeFeeRelayer(view, initialToken, solToken)
            initialAmount?.let { inputAmount ->
                setupDefaultFields(inputAmount)
            }
        }
    }

    /**
     * Get user's soul if nothing is there
     */
    private suspend fun getNonZeroUserTokensOrSoul(): List<Token.Active> {
        return tokenServiceCoordinator.getUserTokens()
            .filterNot {
                if (it.isSOL) {
                    false
                } else {
                    it.isZero
                }
            }
            .sortedWithPreferredStableCoins()
    }

    private fun setupDefaultFields(inputAmount: BigDecimal) {
        view?.apply {
            if (calculationMode.getCurrencyMode() is CurrencyMode.Fiat.Usd) {
                switchCurrencyMode()
            }
            val newTextValue = inputAmount.scaleShort().toPlainString()
            updateInputValue(newTextValue, forced = true)
            calculationMode.updateInputAmount(newTextValue)
            disableInputs()
        }
    }

    private fun handleUpdateFee(
        feeRelayerState: FeeRelayerState.UpdateFee,
        view: NewSendContract.View
    ) {
        val sourceToken = requireToken()
        val total = sendFeeRelayerManager.buildTotalFee(
            sourceToken = sourceToken,
            calculationMode = calculationMode,
            tokenExtensions = feeRelayerState.tokenExtensions
        )

        val feesLabel = total.getFeesInToken(calculationMode.isCurrentInputEmpty()).format(resources)
        view.setFeeLabel(feesLabel)
        updateButton(sourceToken, feeRelayerState)

        // FIXME: only for debug needs, remove after release
        if (BuildConfig.DEBUG) buildDebugInfo(feeRelayerState.solanaFee)
    }

    private fun handleFeeRelayerStateUpdate(
        newState: FeeRelayerState,
        view: NewSendContract.View
    ) {
        when (newState) {
            is FeeRelayerState.UpdateFee -> {
                handleUpdateFee(newState, view)
            }

            is FeeRelayerState.ReduceAmount -> {
                val inputAmount = calculationMode.reduceAmount(newState.newInputAmount).toPlainString()
                view.updateInputValue(inputAmount, forced = true)
                view.showUiKitSnackBar(resources.getString(R.string.send_reduced_amount_calculation_message))
            }

            is FeeRelayerState.Failure -> {
                if (newState.isFeeCalculationError()) {
                    view.showFeeViewVisible(isVisible = false)

                    newState.errorStateError as FeeRelayerStateError.FeesCalculationError
                    val exception = newState.errorStateError.cause
                    if (exception is CancellationException) {
                        Timber.tag(TAG).i(newState)
                    } else {
                        logSendError(token, Throwable("Fee calculation error", exception))
                        Timber.tag(TAG).e(newState, "FeeRelayerState has calculation error")
                    }
                } else {
                    Timber.tag(TAG).e(newState, "FeeRelayerState has error")
                }
                updateButton(requireToken(), newState)
            }

            is FeeRelayerState.Idle -> Unit
        }
    }

    private suspend fun initializeFeeRelayer(
        view: NewSendContract.View,
        initialToken: Token.Active,
        solToken: Token.Active
    ) {
        view.setFeeLabel(resources.getString(R.string.send_fees))
        view.setBottomButtonText(TextContainer.Res(R.string.send_calculating_fees))

        sendFeeRelayerManager.initialize(initialToken, solToken, recipientAddress)
        executeSmartSelection(
            token = requireToken(),
            feePayerToken = requireToken(),
            strategy = SELECT_FEE_PAYER,
            useCache = false
        )

        updateButton(sourceToken = initialToken, feeRelayerState = sendFeeRelayerManager.getState())
    }

    override fun onTokenClicked() {
        newSendAnalytics.logTokenSelectionClicked(flow)
        view?.showTokenSelection(selectedToken = token)
    }

    override fun updateToken(newToken: Token.Active) {
        newSendAnalytics.logTokenChanged(newToken.tokenSymbol, flow)
        token = newToken
        checkTokenRatesAndSetSwitchAmountState(newToken)

        showMaxButtonIfNeeded()
        view?.showFeeViewVisible(isVisible = true)
        updateButton(requireToken(), sendFeeRelayerManager.getState())

        /*
         * Calculating if we can pay with current token instead of already selected fee payer token
         * */
        executeSmartSelection(
            token = requireToken(),
            feePayerToken = requireToken(),
            strategy = CORRECT_AMOUNT,
            useCache = false
        )
    }

    private fun checkTokenRatesAndSetSwitchAmountState(token: Token.Active) {
        /*
        if (token.rate == null || isStableCoinRateDiffAcceptable(token)) {
            if (calculationMode.getCurrencyMode() is CurrencyMode.Fiat.Usd) {
                switchCurrencyMode()
            }
            view?.disableSwitchAmounts()
        } else {
            view?.enableSwitchAmounts()
        }
         */
    }

    private fun isStableCoinRateDiffAcceptable(token: Token.Active): Boolean {
        return token.rate.orZero() == BigDecimal.ONE
    }

    override fun switchCurrencyMode() {
        val newMode = calculationMode.switchMode()
        newSendAnalytics.logSwitchCurrencyModeClicked(newMode, flow)
        view?.showFeeViewVisible(isVisible = true)
        /*
         * Trigger recalculation for USD input
         * */
        executeSmartSelection(
            token = requireToken(),
            feePayerToken = requireToken(),
            strategy = SELECT_FEE_PAYER
        )
    }

    override fun updateInputAmount(amount: String) {
        newSendAnalytics.logTokenAmountChanged(token?.tokenSymbol.orEmpty(), amount, flow)
        calculationMode.updateInputAmount(amount)
        view?.showFeeViewVisible(isVisible = true)
        showMaxButtonIfNeeded()
        updateButton(requireToken(), sendFeeRelayerManager.getState())

        newSendAnalytics.setMaxButtonClicked(isClicked = false)

        /*
         * Calculating if we can pay with current token instead of already selected fee payer token
         * */
        executeSmartSelection(
            token = requireToken(),
            feePayerToken = requireToken(),
            strategy = SELECT_FEE_PAYER
        )
    }

    override fun updateFeePayerToken(feePayerToken: Token.Active) {
        try {
            sendInteractor.setFeePayerToken(feePayerToken)
            executeSmartSelection(
                token = requireToken(),
                feePayerToken = feePayerToken,
                strategy = NO_ACTION
            )
        } catch (e: Throwable) {
            Timber.tag(TAG).e(SendFatalError(cause = e), "Error updating fee payer token")
        }
    }

    override fun onMaxButtonClicked() {
        val token = token ?: kotlin.run {
            Timber.tag(TAG).e(SendFatalError("Token can't be null"))
            return
        }
        val totalAvailable = calculationMode.getMaxAvailableAmount() ?: kotlin.run {
            Timber.tag(TAG).e(SendFatalError("totalAvailable is unavailable"))
            return
        }
        view?.updateInputValue(totalAvailable.toPlainString(), forced = true)
        view?.showFeeViewVisible(isVisible = true)

        showMaxButtonIfNeeded()

        newSendAnalytics.setMaxButtonClicked(isClicked = true)

        val message = resources.getString(R.string.send_using_max_amount, token.tokenSymbol)
        view?.showToast(TextContainer.Raw(message))

        /*
        * Calculating if we can pay with current token instead of already selected fee payer token
        * */
        executeSmartSelection(
            token = requireToken(),
            feePayerToken = requireToken(),
            strategy = CORRECT_AMOUNT
        )
    }

    override fun onFeeInfoClicked() {
        val currentState = sendFeeRelayerManager.getState()
        if (currentState !is FeeRelayerState.UpdateFee) return

        val solanaFee = currentState.solanaFee
        if (calculationMode.isCurrentInputEmpty() && solanaFee == null) {
            newSendAnalytics.logFreeTransactionsClicked(flow)
            view?.showFreeTransactionsInfo()
        } else {
            val total = sendFeeRelayerManager.buildTotalFee(
                sourceToken = requireToken(),
                calculationMode = calculationMode,
                tokenExtensions = currentState.tokenExtensions
            )
            view?.showTransactionDetails(total)
        }
    }

    override fun checkInternetConnection() {
        if (!isInternetConnectionEnabled()) {
            view?.showUiKitSnackBar(
                message = resources.getString(R.string.error_no_internet_message),
                actionButtonResId = R.string.common_hide
            )
            view?.restoreSlider()
            return
        }

        view?.showSliderCompleteAnimation()
    }

    override fun send() {
        val token = requireToken()

        val address = recipientAddress.address
        val currentAmount = calculationMode.getCurrentAmount()
        val currentAmountUsd = calculationMode.getCurrentAmountUsd()
        val lamports = calculationMode.getCurrentAmountLamports()

        logSendClicked(token, currentAmount.toPlainString(), currentAmountUsd.orZero().toPlainString())

        // the internal id for controlling the transaction state
        val internalTransactionId = UUID.randomUUID().toString()

        val total = sendFeeRelayerManager.buildTotalFee(
            sourceToken = requireToken(),
            calculationMode = calculationMode,
            tokenExtensions = emptyMap()
        )

        appScope.launch {
            val transactionDate = ZonedDateTime.now()
            try {
                val progressDetails = NewShowProgress(
                    date = transactionDate,
                    tokenUrl = token.iconUrl.orEmpty(),
                    amountTokens = "${currentAmount.toPlainString()} ${token.tokenSymbol}",
                    amountUsd = currentAmountUsd?.asNegativeUsdTransaction(),
                    totalFees = total.getFeesCombined(checkFeePayer = false)?.let { listOf(it) },
                    transactionDetails = listOf(
                        TitleValue(
                            title = resources.getString(R.string.transaction_send_to_title),
                            value = recipientAddress.nicknameOrAddress()
                        )
                    )
                )
                view?.showProgressDialog(internalTransactionId, progressDetails)

//                val result = sendInteractor.sendTransaction(address.toPublicKey(), token, lamports)
                val result = sendInteractor.sendTransactionV2(address.toPublicKey(), token, lamports)
                userInteractor.addRecipient(recipientAddress, Date(transactionDate.dateMilli()))

                tokenServiceCoordinator.refresh()

                val transaction = buildTransaction(result, token)
                val transactionState = SendSwapProgressState.Success(transaction, token.tokenSymbol)
                transactionManager.emitTransactionState(internalTransactionId, transactionState)
                historyInteractor.addPendingTransaction(
                    txSignature = result,
                    transaction = transaction,
                    mintAddress = token.mintAddress.toBase58Instance()
                )
            } catch (e: Throwable) {
                Timber.tag(TAG).e(e, "Failed sending transaction!")
                val message = e.getErrorMessage { res -> resources.getString(res) }
                transactionManager.emitTransactionState(internalTransactionId, SendSwapProgressState.Error(message))
                logSendError(token, e)
            }
        }
    }

    private fun SearchResult.nicknameOrAddress(): String {
        return if (this is SearchResult.UsernameFound) formattedUsername
        else address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)
    }

    /**
     * The smart selection of the Fee Payer token is being executed in the following cases:
     * 1. When the screen initializes. It checks if we need to create an account for the recipient
     * 2. When user is typing the amount. We are checking what token we can choose for fee payment
     * 3. When user updates the fee payer token manually. We don't do anything, only updating the info
     * 4. When user clicks on MAX button. We are verifying if we need to reduce the amount for valid transaction
     * 5. When user updated the source token. We are checking for valid fee payer and if the entered amount is not much
     *
     * @param useCache is responsible for checking the recipient account info.
     * We are checking if we need to create an account for a user when initially loaded
     * */
    private fun executeSmartSelection(
        token: Token.Active,
        feePayerToken: Token.Active?,
        strategy: FeePayerSelectionStrategy,
        useCache: Boolean = true
    ) {
        feePayerJob?.cancel()
        feePayerJob = launch {
            sendFeeRelayerManager.executeSmartSelection(
                sourceToken = token,
                feePayerToken = feePayerToken,
                strategy = strategy,
                tokenAmount = calculationMode.getCurrentAmount(),
                useCache = useCache
            )
        }
    }

    private fun showMaxButtonIfNeeded() {
        val isMaxButtonVisible = calculationMode.isMaxButtonVisible(sendFeeRelayerManager.getMinRentExemption())
        view?.setMaxButtonVisible(isVisible = isMaxButtonVisible)
    }

    private fun buildTransaction(transactionId: String, token: Token.Active): HistoryTransaction =
        RpcHistoryTransaction.Transfer(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = -1,
            type = RpcHistoryTransactionType.SEND,
            senderAddress = tokenKeyProvider.publicKey,
            amount = RpcHistoryAmount(calculationMode.getCurrentAmount(), calculationMode.getCurrentAmountUsd()),
            destination = recipientAddress.address,
            counterPartyUsername = recipientAddress.nicknameOrAddress(),
            fees = null,
            status = HistoryTransactionStatus.PENDING,
            iconUrl = token.iconUrl,
            symbol = token.tokenSymbol
        )

    private fun updateButton(sourceToken: Token.Active, feeRelayerState: FeeRelayerState) {
        val sendButton = NewSendButtonState(
            sourceToken = sourceToken,
            searchResult = recipientAddress,
            calculationMode = calculationMode,
            feeRelayerState = feeRelayerState,
            minRentExemption = sendFeeRelayerManager.getMinRentExemption(),
            resources = resources
        )

        when (val state = sendButton.currentState) {
            is NewSendButtonState.State.Disabled -> {
                view?.setBottomButtonText(state.textContainer)
                view?.setSliderText(null)
                view?.setInputColor(state.totalAmountTextColor)
            }

            is NewSendButtonState.State.Enabled -> {
                view?.setSliderText(resources.getString(state.textResId, state.value))
                view?.setBottomButtonText(null)
                view?.setInputColor(state.totalAmountTextColor)
            }
        }
    }

    private fun buildDebugInfo(solanaFee: SendSolanaFee?) {
        launch {
            val debugInfo = sendFeeRelayerManager.buildDebugInfo(solanaFee)
            view?.showDebugInfo(debugInfo)
        }
    }

    private fun isInternetConnectionEnabled(): Boolean =
        connectionStateProvider.hasConnection()

    private fun requireToken(): Token.Active =
        token ?: throw SendFatalError("Token can't be null")

    private fun logSendClicked(token: Token.Active, amountInToken: String, amountInUsd: String) {
        val solanaFee = (sendFeeRelayerManager.getState() as? FeeRelayerState.UpdateFee)?.solanaFee
        newSendAnalytics.logSendConfirmButtonClicked(
            tokenName = token.tokenName,
            amountInToken = amountInToken,
            amountInUsd = amountInUsd,
            isFeeFree = solanaFee?.isTransactionFree ?: false,
            mode = calculationMode.getCurrencyMode(),
            flow = flow
        )
    }

    private fun logSendError(
        token: Token.Active?,
        error: Throwable
    ) {
        if (token == null) return

        launch {
            val fee = sendFeeRelayerManager.getState().getFee()
            val accountCreationFee = fee?.accountCreationFeeDecimals?.toPlainString()
            val transactionFee = fee?.transactionDecimals?.toPlainString()
            alertErrorsLogger.triggerSendAlarm(
                token = token,
                currencyMode = calculationMode.getCurrencyMode(),
                amount = calculationMode.getCurrentAmount().toPlainString(),
                feePayerToken = sendInteractor.getFeePayerToken(),
                accountCreationFee = accountCreationFee,
                transactionFee = transactionFee,
                relayAccount = sendInteractor.getUserRelayAccount(),
                recipientAddress = recipientAddress,
                error = error
            )
        }
    }
}
