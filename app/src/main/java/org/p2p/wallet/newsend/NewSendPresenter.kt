package org.p2p.wallet.newsend

import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.CORRECT_AMOUNT
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.NO_ACTION
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.SELECT_FEE_PAYER
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransferType
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.FeeRelayerState
import org.p2p.wallet.newsend.model.NewSendButton
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.toPublicKey
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.util.UUID
import kotlin.properties.Delegates
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NewSendPresenter(
    private val recipientAddress: SearchResult,
    private val userInteractor: UserInteractor,
    private val sendInteractor: SendInteractor,
    private val resources: ResourcesProvider,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionManager: TransactionManager,
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

    private var feePayerJob: Job? = null

    override fun attach(view: NewSendContract.View) {
        super.attach(view)
        initialize(view)
    }

    private fun initialize(view: NewSendContract.View) {
        calculationMode.onCalculationCompleted = { view.showAroundValue(it) }
        calculationMode.onLabelsUpdated = { switchSymbol, mainSymbol ->
            view.setSwitchLabel(switchSymbol)
            view.setMainAmountLabel(mainSymbol)
        }

        feeRelayerManager.onStateUpdated = { newState ->
            when (newState) {
                is FeeRelayerState.UpdateFee -> {
                    handleUpdateFee(newState, view)
                }
                is FeeRelayerState.ReduceAmount -> {
                    inputAmount = newState.newInputAmount.fromLamports(requireToken().decimals).toPlainString()
                    view.updateInputValue(inputAmount, forced = true)
                }
                is FeeRelayerState.Failure -> {
                    view.setFeeLabel(text = null)
                    updateButton(requireToken(), newState)
                }
                is FeeRelayerState.Idle -> Unit
            }
        }

        launch {
            // We should find SOL anyway because SOL is needed for Selection Mechanism
            val userTokens = userInteractor.getUserTokens()
            if (userTokens.isEmpty()) {
                // we cannot proceed if user tokens are not loaded
                view.showUiKitSnackBar(resources.getString(R.string.error_general_message))
                return@launch
            }

            val initialToken = userTokens.find { it.isUSDC && !it.isZero } ?: userTokens.maxBy { it.totalInLamports }

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
        val currentAmount = calculationMode.getCurrentAmount()
        val sendFee = feeRelayerState.solanaFee
        val total = buildTotalFee(currentAmount, sourceToken, sendFee, feeRelayerState.feeLimitInfo)

        val text = total.getTotalFee { resources.getString(it) }
        view.setFeeLabel(text)

        updateButton(sourceToken, feeRelayerState)
    }

    private fun buildTotalFee(
        currentAmount: BigDecimal,
        sourceToken: Token.Active,
        sendFee: SendSolanaFee?,
        feeLimitInfo: FreeTransactionFeeLimit
    ) = SendFeeTotal(
        total = currentAmount,
        totalUsd = calculationMode.getCurrentAmountUsd(),
        receive = "${currentAmount.formatToken()} ${sourceToken.tokenSymbol}",
        receiveUsd = currentAmount.toUsd(sourceToken),
        sourceSymbol = sourceToken.tokenSymbol,
        sendFee = sendFee,
        recipientAddress = recipientAddress.addressState.address,
        feeLimit = feeLimitInfo
    )

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
        recountAccordingToSelectedData()
    }

    override fun switchCurrencyMode() {
        calculationMode.switchMode()
        recountAccordingToSelectedData()
    }

    override fun updateInputAmount(amount: String) {
        inputAmount = amount
        recountAccordingToSelectedData()
    }

    private fun recountAccordingToSelectedData() {
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

    override fun setMaxAmountValue() {
        val totalAvailable = calculationMode.getTotalAvailable() ?: return
        view?.updateInputValue(totalAvailable.toPlainString(), forced = true)
        inputAmount = totalAvailable.toString()

        showMaxButtonIfNeeded()

        val token = token ?: return
        val message = resources.getString(R.string.send_using_max_amount, token.tokenSymbol)
        view?.showUiKitSnackBar(message)

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
        if (solanaFee == null) {
            view?.showFreeTransactionsInfo()
        } else {
            val sourceToken = requireToken()
            val currentAmount = calculationMode.getCurrentAmount()
            val total = buildTotalFee(currentAmount, sourceToken, solanaFee, feeRelayerManager.getFeeLimitInfo())
            view?.showTransactionDetails(total)
        }
    }

    override fun onAccountCreationFeeClicked(fee: SendSolanaFee) {
        launch {
            val userTokens = userInteractor.getUserTokens()
            val hasAlternativeFeePayerTokens = sendInteractor.hasAlternativeFeePayerTokens(userTokens, fee)
            view?.showAccountCreationFeeInfo(
                tokenSymbol = fee.feePayerSymbol,
                amountInUsd = fee.approxAccountCreationFeeUsd.orEmpty(),
                hasAlternativeToken = hasAlternativeFeePayerTokens
            )
        }
    }

    override fun onChangeFeePayerClicked(approximateFeeUsd: String) {
        launch {
            val feePayerToken = sendInteractor.getFeePayerToken()
            val userTokens = userInteractor.getUserTokens()
            view?.showFeePayerTokenSelection(
                tokens = userTokens,
                currentFeePayerToken = feePayerToken,
                approximateFeeUsd = approximateFeeUsd
            )
        }
    }

    override fun send() {
        val token = token ?: error("Token cannot be null!")
        val address = recipientAddress.addressState.address
        val currentAmount = calculationMode.getCurrentAmount()
        val lamports = calculationMode.getCurrentAmountLamports()

        // the internal id for controlling the transaction state
        val internalTransactionId = UUID.randomUUID().toString()

        appScope.launch {
            try {
                val destinationAddressShort = address.cutMiddle()
                val data = ShowProgress(
                    title = R.string.send_transaction_being_processed,
                    subTitle = "${currentAmount.toPlainString()} ${token.tokenSymbol} → $destinationAddressShort",
                    transactionId = emptyString()
                )

                view?.showProgressDialog(internalTransactionId, data)

                val result = sendInteractor.sendTransaction(address.toPublicKey(), token, lamports)
                val transactionState = TransactionState.SendSuccess(buildTransaction(result), token.tokenSymbol)
                transactionManager.emitTransactionState(internalTransactionId, transactionState)
            } catch (e: Throwable) {
                val message = e.getErrorMessage { res -> resources.getString(res) }
                transactionManager.emitTransactionState(internalTransactionId, TransactionState.Error(message))
            }
        }
    }

    /**
     * The smart selection of the Fee Payer token is being executed in four cases:
     * 1. When the screen initializes. It checks if we need to create an account for the recipient
     * 2. When user is typing the amount. We are checking what token we can choose for fee payment
     * 3. When user updates the fee payer token manually. We don't do anything, only updating the info
     * 4. When user clicks on MAX button. We are verifying if we need to reduce the amount for valid transaction
     * */
    private fun executeSmartSelection(
        token: Token.Active,
        feePayerToken: Token.Active?,
        strategy: FeePayerSelectionStrategy
    ) {
        feePayerJob?.cancel()

        launch {
            feeRelayerManager.executeSmartSelection(
                sourceToken = token,
                feePayerToken = feePayerToken,
                strategy = strategy,
                tokenAmount = calculationMode.getCurrentAmount()
            )
        }
    }

    private fun showMaxButtonIfNeeded() {
        view?.setMaxButtonVisible(isVisible = inputAmount.isEmpty())
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
        val sendButton = NewSendButton(
            sourceToken = sourceToken,
            searchResult = recipientAddress,
            tokenAmount = calculationMode.getCurrentAmount(),
            feeRelayerState = feeRelayerState,
            minRentExemption = feeRelayerManager.getMinRentExemption(),
            resources = resources
        )

        when (val state = sendButton.state) {
            is NewSendButton.State.Disabled -> {
                view?.setBottomButtonText(state.textContainer)
                view?.setSliderText(null)
                view?.setInputColor(state.totalAmountTextColor)
            }
            is NewSendButton.State.Enabled -> {
                view?.setSliderText(resources.getString(state.textResId, state.value))
                view?.setBottomButtonText(null)
                view?.setInputColor(state.totalAmountTextColor)
            }
        }
    }

    private fun requireToken(): Token.Active =
        token ?: error("Source token cannot be empty!")
}
