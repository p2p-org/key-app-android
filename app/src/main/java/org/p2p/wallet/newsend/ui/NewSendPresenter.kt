package org.p2p.wallet.newsend.ui

import android.content.res.Resources
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.asNegativeUsdTransaction
import org.p2p.core.utils.emptyString
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.CORRECT_AMOUNT
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.NO_ACTION
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.SELECT_FEE_PAYER
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransferType
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.newsend.SendFeeRelayerManager
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.FeeRelayerState
import org.p2p.wallet.newsend.model.NewSendButtonValidator
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.updates.ConnectionStateProvider
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.toPublicKey
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Date
import java.util.UUID
import kotlin.properties.Delegates
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NewSendPresenter(
    private val recipientAddress: SearchResult,
    private val userInteractor: UserInteractor,
    private val sendInteractor: SendInteractor,
    private val resources: Resources,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionManager: TransactionManager,
    private val connectionStateProvider: ConnectionStateProvider,
    private val appScope: AppScope
) : BasePresenter<NewSendContract.View>(), NewSendContract.Presenter {

    private var token: Token.Active? by Delegates.observable(null) { _, _, newToken ->
        if (newToken != null) {
            view?.showToken(newToken)
            calculationMode.updateToken(newToken)
        }
    }

    private var inputAmount: String by Delegates.observable(emptyString()) { _, _, newInput ->
        calculationMode.updateInputAmount(newInput)
    }

    private val calculationMode = CalculationMode()
    private val feeRelayerManager = SendFeeRelayerManager(sendInteractor)

    private var selectedToken: Token.Active? = null
    private var initialAmount: BigDecimal? = null

    private var feePayerJob: Job? = null

    override fun attach(view: NewSendContract.View) {
        super.attach(view)
        initialize(view)
    }

    override fun setInitialData(selectedToken: Token.Active?, inputAmount: BigDecimal?) {
        this.selectedToken = selectedToken
        this.initialAmount = inputAmount
    }

    private fun initialize(view: NewSendContract.View) {
        calculationMode.onCalculationCompleted = { view.showAroundValue(it) }
        calculationMode.onInputFractionUpdated = { view.updateInputFraction(it) }
        calculationMode.onLabelsUpdated = { switchSymbol, mainSymbol ->
            view.setSwitchLabel(switchSymbol)
            view.setMainAmountLabel(mainSymbol)
        }

        feeRelayerManager.onStateUpdated = { newState -> handleFeeRelayerStateUpdate(newState, view) }
        feeRelayerManager.onFeeLoading = { isLoading -> view.showFeeViewLoading(isLoading = isLoading) }

        if (token != null) {
            restoreSelectedToken(view, token!!)
        } else {
            setupInitialToken(view)
        }
    }

    private fun restoreSelectedToken(view: NewSendContract.View, token: Token.Active) {
        launch {
            view.showToken(token)
            calculationMode.updateToken(token)

            val userTokens = userInteractor.getNonZeroUserTokens()
            val isTokenChangeEnabled = userTokens.size > 1 && selectedToken == null
            view.setTokenContainerEnabled(isEnabled = isTokenChangeEnabled)

            val solToken = userInteractor.getUserSolToken()
            if (solToken == null) {
                view.showUiKitSnackBar(resources.getString(R.string.error_general_message))
                return@launch
            }

            val currentState = feeRelayerManager.getState()
            handleFeeRelayerStateUpdate(currentState, view)
        }
    }

    private fun setupInitialToken(view: NewSendContract.View) {
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
            val solToken = if (initialToken.isSOL) initialToken else userTokens.find { it.isSOL }
            if (solToken == null) {
                // we cannot proceed without SOL.
                view.showUiKitSnackBar(resources.getString(R.string.error_general_message))
                return@launch
            }

            initializeFeeRelayer(view, initialToken, solToken)
        }
    }

    private fun handleUpdateFee(
        feeRelayerState: FeeRelayerState.UpdateFee,
        view: NewSendContract.View
    ) {
        val sourceToken = requireToken()
        val total = feeRelayerManager.buildTotalFee(
            sourceToken = sourceToken,
            calculationMode = calculationMode
        )

        val feesLabel = total.getFeesInToken(inputAmount.isEmpty()).format(resources)
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
                inputAmount = calculationMode.reduceAmount(newState.newInputAmount).toPlainString()
                view.updateInputValue(inputAmount, forced = true)
                view.showUiKitSnackBar(resources.getString(R.string.send_reduced_amount_calculation_message))
            }
            is FeeRelayerState.Failure -> {
                view.setFeeLabel(text = null)
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
        view.showFeeViewLoading(isLoading = true)
        view.setBottomButtonText(TextContainer.Res(R.string.send_calculating_fees))

        feeRelayerManager.initialize(initialToken, solToken, recipientAddress)
        executeSmartSelection(
            token = requireToken(),
            feePayerToken = requireToken(),
            strategy = SELECT_FEE_PAYER
        )

        view.showFeeViewLoading(isLoading = false)
        updateButton(initialToken, feeRelayerManager.getState())
    }

    override fun onTokenClicked() {
        launch {
            val tokens = userInteractor.getUserTokens()
            val result = tokens.filter { token -> !token.isZero }
            view?.showTokenSelection(result, token)
        }
    }

    override fun updateToken(newToken: Token.Active) {
        token = newToken
        showMaxButtonIfNeeded()
        updateButton(requireToken(), feeRelayerManager.getState())

        /*
         * Calculating if we can pay with current token instead of already selected fee payer token
         * */
        executeSmartSelection(
            token = requireToken(),
            feePayerToken = requireToken(),
            strategy = CORRECT_AMOUNT
        )
    }

    override fun switchCurrencyMode() {
        calculationMode.switchMode()
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
        inputAmount = amount
        showMaxButtonIfNeeded()
        updateButton(requireToken(), feeRelayerManager.getState())

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
            Timber.e(e, "Error updating fee payer token")
        }
    }

    override fun onMaxButtonClicked() {
        val token = token ?: return
        val totalAvailable = calculationMode.getTotalAvailable() ?: return
        view?.updateInputValue(totalAvailable.toPlainString(), forced = true)
        inputAmount = totalAvailable.toString()

        showMaxButtonIfNeeded()

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
        val currentState = feeRelayerManager.getState()
        if (currentState !is FeeRelayerState.UpdateFee) return

        val solanaFee = currentState.solanaFee
        if (inputAmount.isEmpty() && solanaFee == null) {
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
                    totalFee = total
                )

                view?.showProgressDialog(internalTransactionId, progressDetails)

                val result = sendInteractor.sendTransaction(address.toPublicKey(), token, lamports)
                userInteractor.addRecipient(recipientAddress, transactionDate)
                val transactionState = TransactionState.SendSuccess(buildTransaction(result), token.tokenSymbol)
                transactionManager.emitTransactionState(internalTransactionId, transactionState)
            } catch (e: Throwable) {
                val message = e.getErrorMessage { res -> resources.getString(res) }
                transactionManager.emitTransactionState(internalTransactionId, TransactionState.Error(message))
            }
        }
    }

    private fun SearchResult.nicknameOrAddress(): String {
        return if (this is SearchResult.UsernameFound) username
        else addressState.address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)
    }

    /**
     * The smart selection of the Fee Payer token is being executed in four cases:
     * 1. When the screen initializes. It checks if we need to create an account for the recipient
     * 2. When user is typing the amount. We are checking what token we can choose for fee payment
     * 3. When user updates the fee payer token manually. We don't do anything, only updating the info
     * 4. When user clicks on MAX button. We are verifying if we need to reduce the amount for valid transaction
     * 5. When user updated the source token. We are checking for valid fee payer and if the entered amount is not much
     * */
    private fun executeSmartSelection(
        token: Token.Active,
        feePayerToken: Token.Active?,
        strategy: FeePayerSelectionStrategy
    ) {
        feePayerJob?.cancel()
        feePayerJob = launch {
            feeRelayerManager.executeSmartSelection(
                sourceToken = token,
                feePayerToken = feePayerToken,
                strategy = strategy,
                tokenAmount = calculationMode.getCurrentAmount()
            )
        }
    }

    private fun showMaxButtonIfNeeded() {
        val isMaxButtonVisible = calculationMode.isMaxButtonVisible(feeRelayerManager.getMinRentExemption())
        view?.setMaxButtonVisible(isVisible = isMaxButtonVisible)
    }

    private fun buildTransaction(transactionId: String): HistoryTransaction =
        HistoryTransaction.Transfer(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = null,
            type = TransferType.SEND,
            senderAddress = tokenKeyProvider.publicKey,
            tokenData = TokenConverter.toTokenData(token!!),
            totalInUsd = calculationMode.getCurrentAmountUsd(),
            total = calculationMode.getCurrentAmount(),
            destination = recipientAddress.addressState.address,
            fee = BigInteger.ZERO,
            status = TransactionStatus.PENDING
        )

    private fun updateButton(sourceToken: Token.Active, feeRelayerState: FeeRelayerState) {
        val sendButton = NewSendButtonValidator(
            sourceToken = sourceToken,
            searchResult = recipientAddress,
            calculationMode = calculationMode,
            feeRelayerState = feeRelayerState,
            minRentExemption = feeRelayerManager.getMinRentExemption(),
            resources = resources
        )

        when (val state = sendButton.state) {
            is NewSendButtonValidator.State.Disabled -> {
                view?.setBottomButtonText(state.textContainer)
                view?.setSliderText(null)
                view?.setInputColor(state.totalAmountTextColor)
            }
            is NewSendButtonValidator.State.Enabled -> {
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
}
