package org.p2p.wallet.bridge.send.ui

import android.content.res.Resources
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date
import java.util.UUID
import kotlin.properties.Delegates
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.core.utils.asNegativeUsdTransaction
import org.p2p.core.utils.scaleShort
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.newsend.SendFeeRelayerManager
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.FeeLoadingState
import org.p2p.wallet.newsend.model.FeeRelayerState
import org.p2p.wallet.newsend.model.NewSendButtonState
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.model.SendSolanaFee
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.updates.ConnectionStateProvider
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.toPublicKey

class BridgeSendV2Presenter(
    private val recipientAddress: SearchResult,
    private val userInteractor: UserInteractor,
    private val sendInteractor: SendInteractor,
    private val resources: Resources,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionManager: TransactionManager,
    private val connectionStateProvider: ConnectionStateProvider,
    private val newSendAnalytics: NewSendAnalytics,
    private val appScope: AppScope,
    sendModeProvider: SendModeProvider
) : BasePresenter<BridgeSendContract.View>(), BridgeSendContract.Presenter {

    private var token: Token.Active? by Delegates.observable(null) { _, _, newToken ->
        if (newToken != null) {
            view?.showToken(newToken)
            calculationMode.updateToken(newToken)
        }
    }

    private val calculationMode = CalculationMode(
        sendModeProvider = sendModeProvider,
        lessThenMinString = resources.getString(R.string.common_less_than_minimum)
    )
    private val feeRelayerManager = SendFeeRelayerManager(sendInteractor, userInteractor)

    private var selectedToken: Token.Active? = null
    private var initialAmount: BigDecimal? = null

    private var feePayerJob: Job? = null

    override fun attach(view: BridgeSendContract.View) {
        super.attach(view)
        newSendAnalytics.logNewSendScreenOpened()

        initialize(view)
    }

    override fun setInitialData(selectedToken: Token.Active?, inputAmount: BigDecimal?) {
        this.selectedToken = selectedToken
        this.initialAmount = inputAmount
    }

    private fun initialize(view: BridgeSendContract.View) {
        calculationMode.onCalculationCompleted = { view.showAroundValue(it) }
        calculationMode.onInputFractionUpdated = { view.updateInputFraction(it) }
        calculationMode.onLabelsUpdated = { switchSymbol, mainSymbol ->
            view.setSwitchLabel(switchSymbol)
            view.setMainAmountLabel(mainSymbol)
        }

        feeRelayerManager.onStateUpdated = { newState ->
            handleFeeRelayerStateUpdate(newState, view)
        }
        feeRelayerManager.onFeeLoading = { loadingState ->
            when (loadingState) {
                is FeeLoadingState.Instant -> view.showFeeViewLoading(isLoading = loadingState.isLoading)
                is FeeLoadingState.Delayed -> view.showDelayedFeeViewLoading(isLoading = loadingState.isLoading)
            }
        }

        if (token != null) {
            restoreSelectedToken(view, token!!)
        } else {
            setupInitialToken(view)
        }
    }

    private fun restoreSelectedToken(view: BridgeSendContract.View, token: Token.Active) {
        launch {
            view.showToken(token)
            calculationMode.updateToken(token)

            val userTokens = userInteractor.getNonZeroUserTokens()
            val isTokenChangeEnabled = userTokens.size > 1 && selectedToken == null
            view.setTokenContainerEnabled(isEnabled = isTokenChangeEnabled)

            val currentState = feeRelayerManager.getState()
            handleFeeRelayerStateUpdate(currentState, view)
        }
    }

    private fun setupInitialToken(view: BridgeSendContract.View) {
        launch {
            // We should find SOL anyway because SOL is needed for Selection Mechanism
            val userTokens = userInteractor.getNonZeroUserTokens()
            if (userTokens.isEmpty()) {
                // we cannot proceed if user tokens are not loaded
                view.showUiKitSnackBar(resources.getString(R.string.error_general_message))
                return@launch
            }

            val isTokenChangeEnabled = userTokens.size > 1 && selectedToken == null
            view.setTokenContainerEnabled(isEnabled = isTokenChangeEnabled)

            val initialToken = if (selectedToken != null) selectedToken!! else userTokens.first()
            token = initialToken

            val solToken = if (initialToken.isSOL) initialToken else userInteractor.getUserSolToken()
            if (solToken == null) {
                // we cannot proceed without SOL.
                view.showUiKitSnackBar(resources.getString(R.string.error_general_message))
                Timber.e(IllegalStateException("Couldn't find user's SOL account!"))
                return@launch
            }

            initializeFeeRelayer(view, initialToken, solToken)
            initialAmount?.let { inputAmount ->
                setupDefaultFields(inputAmount)
            }
        }
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
        view: BridgeSendContract.View
    ) {
        val sourceToken = requireToken()
        val total = feeRelayerManager.buildTotalFee(
            sourceToken = sourceToken,
            calculationMode = calculationMode
        )

        val feesLabel = total.getFeesInToken(calculationMode.isCurrentInputEmpty()).format(resources)
        view.setFeeLabel(feesLabel)
        updateButton(sourceToken, feeRelayerState)

        // FIXME: only for debug needs, remove after release
        if (BuildConfig.DEBUG) buildDebugInfo(feeRelayerState.solanaFee)
    }

    private fun handleFeeRelayerStateUpdate(
        newState: FeeRelayerState,
        view: BridgeSendContract.View
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
                if (newState.isFeeCalculationError()) view.showFeeViewVisible(isVisible = false)
                updateButton(requireToken(), newState)
            }
            is FeeRelayerState.Idle -> Unit
        }
    }

    private suspend fun initializeFeeRelayer(
        view: BridgeSendContract.View,
        initialToken: Token.Active,
        solToken: Token.Active
    ) {
        view.setFeeLabel(resources.getString(R.string.send_fees))
        view.setBottomButtonText(TextContainer.Res(R.string.send_calculating_fees))

        feeRelayerManager.initialize(initialToken, solToken, recipientAddress)
        executeSmartSelection(
            token = requireToken(),
            feePayerToken = requireToken(),
            strategy = FeePayerSelectionStrategy.SELECT_FEE_PAYER,
            useCache = false
        )

        updateButton(sourceToken = initialToken, feeRelayerState = feeRelayerManager.getState())
    }

    override fun onTokenClicked() {
        newSendAnalytics.logTokenSelectionClicked()
        launch {
            val supportedTokensMints = ERC20Tokens.values().map { it.mintAddress }
            val tokens = userInteractor.getUserTokens().filter { it.mintAddress in supportedTokensMints }
            val result = tokens.filterNot(Token.Active::isZero)
            view?.showTokenSelection(tokens = result, selectedToken = token)
        }
    }

    override fun updateToken(newToken: Token.Active) {
        token = newToken
        showMaxButtonIfNeeded()
        view?.showFeeViewVisible(isVisible = true)
        updateButton(requireToken(), feeRelayerManager.getState())

        /*
         * Calculating if we can pay with current token instead of already selected fee payer token
         * */
        executeSmartSelection(
            token = requireToken(),
            feePayerToken = requireToken(),
            strategy = FeePayerSelectionStrategy.CORRECT_AMOUNT,
            useCache = false
        )
    }

    override fun switchCurrencyMode() {
        val newMode = calculationMode.switchMode()
        newSendAnalytics.logSwitchCurrencyModeClicked(newMode)
        view?.showFeeViewVisible(isVisible = true)
        /*
         * Trigger recalculation for USD input
         * */
        executeSmartSelection(
            token = requireToken(),
            feePayerToken = requireToken(),
            strategy = FeePayerSelectionStrategy.SELECT_FEE_PAYER
        )
    }

    override fun updateInputAmount(amount: String) {
        calculationMode.updateInputAmount(amount)
        view?.showFeeViewVisible(isVisible = true)
        showMaxButtonIfNeeded()
        updateButton(requireToken(), feeRelayerManager.getState())

        newSendAnalytics.setMaxButtonClicked(isClicked = false)

        /*
         * Calculating if we can pay with current token instead of already selected fee payer token
         * */
        executeSmartSelection(
            token = requireToken(),
            feePayerToken = requireToken(),
            strategy = FeePayerSelectionStrategy.SELECT_FEE_PAYER
        )
    }

    override fun updateFeePayerToken(feePayerToken: Token.Active) {
        try {
            sendInteractor.setFeePayerToken(feePayerToken)
            executeSmartSelection(
                token = requireToken(),
                feePayerToken = feePayerToken,
                strategy = FeePayerSelectionStrategy.NO_ACTION
            )
        } catch (e: Throwable) {
            Timber.e(e, "Error updating fee payer token")
        }
    }

    override fun onMaxButtonClicked() {
        val token = token ?: return
        val totalAvailable = calculationMode.getMaxAvailableAmount() ?: return
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
            strategy = FeePayerSelectionStrategy.CORRECT_AMOUNT
        )
    }

    override fun onFeeInfoClicked() {
        val currentState = feeRelayerManager.getState()
        if (currentState !is FeeRelayerState.UpdateFee) return

        val solanaFee = currentState.solanaFee
        if (calculationMode.isCurrentInputEmpty() && solanaFee == null) {
            newSendAnalytics.logFreeTransactionsClicked()
            view?.showFreeTransactionsInfo()
        } else {
            val total = feeRelayerManager.buildTotalFee(
                sourceToken = requireToken(),
                calculationMode = calculationMode
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
        val token = token ?: error("Token cannot be null!")

        val address = recipientAddress.addressState.address
        val currentAmount = calculationMode.getCurrentAmount()
        val currentAmountUsd = calculationMode.getCurrentAmountUsd()
        val lamports = calculationMode.getCurrentAmountLamports()

        logSendClicked(token, currentAmount.toPlainString(), currentAmountUsd.toPlainString())

        // the internal id for controlling the transaction state
        val internalTransactionId = UUID.randomUUID().toString()

        val total = feeRelayerManager.buildTotalFee(
            sourceToken = requireToken(),
            calculationMode = calculationMode
        )

        appScope.launch {
            val transactionDate = Date()
            try {
                val progressDetails = NewShowProgress(
                    date = transactionDate,
                    tokenUrl = token.iconUrl.orEmpty(),
                    amountTokens = "${currentAmount.toPlainString()} ${token.tokenSymbol}",
                    amountUsd = currentAmountUsd.asNegativeUsdTransaction(),
                    recipient = recipientAddress.nicknameOrAddress(),
                    totalFees = total.getFeesCombined(checkFeePayer = false)?.let { listOf(it) }
                )

                view?.showProgressDialog(internalTransactionId, progressDetails)

                val result = sendInteractor.sendTransaction(address.toPublicKey(), token, lamports)
                userInteractor.addRecipient(recipientAddress, transactionDate)
                val transactionState = TransactionState.SendSuccess(buildTransaction(result), token.tokenSymbol)
                transactionManager.emitTransactionState(internalTransactionId, transactionState)
            } catch (e: Throwable) {
                Timber.e(e)
                val message = e.getErrorMessage { res -> resources.getString(res) }
                transactionManager.emitTransactionState(internalTransactionId, TransactionState.Error(message))
            }
        }
    }

    private fun SearchResult.nicknameOrAddress(): String {
        return if (this is SearchResult.UsernameFound) getFormattedUsername()
        else addressState.address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)
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
            feeRelayerManager.executeSmartSelection(
                sourceToken = token,
                feePayerToken = feePayerToken,
                strategy = strategy,
                tokenAmount = calculationMode.getCurrentAmount(),
                useCache = useCache
            )
        }
    }

    private fun showMaxButtonIfNeeded() {
        val isMaxButtonVisible = calculationMode.isMaxButtonVisible(feeRelayerManager.getMinRentExemption())
        view?.setMaxButtonVisible(isVisible = isMaxButtonVisible)
    }

    private fun buildTransaction(transactionId: String): HistoryTransaction =
        RpcHistoryTransaction.Transfer(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = -1,
            type = RpcHistoryTransactionType.SEND,
            senderAddress = tokenKeyProvider.publicKey,
            amount = RpcHistoryAmount(calculationMode.getCurrentAmount(), calculationMode.getCurrentAmountUsd()),
            destination = recipientAddress.addressState.address,
            counterPartyUsername = recipientAddress.nicknameOrAddress(),
            fees = null,
            status = HistoryTransactionStatus.PENDING,
            iconUrl = emptyString(),
            symbol = emptyString()
        )

    private fun updateButton(sourceToken: Token.Active, feeRelayerState: FeeRelayerState) {
        val sendButton = NewSendButtonState(
            sourceToken = sourceToken,
            searchResult = recipientAddress,
            calculationMode = calculationMode,
            feeRelayerState = feeRelayerState,
            minRentExemption = feeRelayerManager.getMinRentExemption(),
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
            val debugInfo = feeRelayerManager.buildDebugInfo(solanaFee)
            view?.showDebugInfo(debugInfo)
        }
    }

    private fun isInternetConnectionEnabled(): Boolean =
        connectionStateProvider.hasConnection()

    private fun requireToken(): Token.Active =
        token ?: error("Source token cannot be empty!")

    private fun logSendClicked(token: Token.Active, amountInToken: String, amountInUsd: String) {
        val solanaFee = (feeRelayerManager.getState() as? FeeRelayerState.UpdateFee)?.solanaFee
        newSendAnalytics.logSendConfirmButtonClicked(
            tokenName = token.tokenName,
            amountInToken = amountInToken,
            amountInUsd = amountInUsd,
            isFeeFree = solanaFee?.isTransactionFree ?: false,
            mode = calculationMode.getCurrencyMode()
        )
    }
}
