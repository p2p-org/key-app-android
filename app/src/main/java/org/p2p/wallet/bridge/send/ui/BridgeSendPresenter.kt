package org.p2p.wallet.bridge.send.ui

import android.content.res.Resources
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import java.util.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.core.utils.asNegativeUsdTransaction
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isConnectionError
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.R
import org.p2p.wallet.bridge.send.interactor.BridgeSendInteractor
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendFeatureException
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.SendStateMachine
import org.p2p.wallet.bridge.send.statemachine.bridgeFee
import org.p2p.wallet.bridge.send.statemachine.bridgeToken
import org.p2p.wallet.bridge.send.statemachine.lastStaticState
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendInitialData
import org.p2p.wallet.bridge.send.statemachine.model.SendToken
import org.p2p.wallet.bridge.send.ui.mapper.BridgeSendUiMapper
import org.p2p.wallet.bridge.send.ui.model.BridgeFeeDetails
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.updates.ConnectionStateProvider
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.getErrorMessage

class BridgeSendPresenter(
    private val recipientAddress: SearchResult,
    private val userInteractor: UserInteractor,
    private val bridgeInteractor: BridgeSendInteractor,
    private val resources: Resources,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionManager: TransactionManager,
    private val connectionStateProvider: ConnectionStateProvider,
    private val newSendAnalytics: NewSendAnalytics,
    private val appScope: AppScope,
    sendModeProvider: SendModeProvider,
    private val initialData: SendInitialData.Bridge,
    private val stateMachine: SendStateMachine,
    private val bridgeSendUiMapper: BridgeSendUiMapper,
) : BasePresenter<BridgeSendContract.View>(), BridgeSendContract.Presenter {

    private var currentState: SendState = SendState.Static.Empty

    private val calculationMode = CalculationMode(
        sendModeProvider = sendModeProvider,
        lessThenMinString = resources.getString(R.string.common_less_than_minimum)
    )

    override fun attach(view: BridgeSendContract.View) {
        super.attach(view)
        newSendAnalytics.logNewSendScreenOpened()
        initialize(view)
        launch {
            val isTokenChangeEnabled = bridgeInteractor.supportedSendTokens().size > 1
            view.setTokenContainerEnabled(isEnabled = isTokenChangeEnabled)
        }
        stateMachine.observe()
            .onEach {
                handleState(it)
                this.currentState = it
            }
            .launchIn(this)
    }

    private fun handleState(state: SendState) {
        when (state) {
            is SendState.Exception -> handleErrorState(state)
            is SendState.Loading -> handleLoadingState(state)
            is SendState.Static -> handleStaticState(state)
        }
    }

    private fun handleStaticState(state: SendState.Static) {
        when (state) {
            SendState.Static.Empty -> Unit
            is SendState.Static.ReadyToSend -> view?.apply {
                val bridgeToken = state.bridgeToken ?: return
                val token = bridgeToken.token
                updateTokenAndInput(bridgeToken, state.amount)
                handleUpdateFee(sendFee = state.fee, isInputEmpty = false)

                val textResId = R.string.send_format
                val value = "${state.amount.formatToken(token.decimals)} ${token.tokenSymbol}"
                updateButtons(
                    errorButton = null,
                    sliderButton = resources.getString(textResId, value)
                )
            }
            is SendState.Static.TokenNotZero -> view?.apply {
                val bridgeToken = state.bridgeToken ?: return
                updateTokenAndInput(bridgeToken, state.amount)
                handleUpdateFee(sendFee = state.fee, isInputEmpty = false)
                updateButtons(
                    errorButton = TextContainer.Res(R.string.main_enter_the_amount),
                    sliderButton = null
                )
            }
            is SendState.Static.TokenZero -> view?.apply {
                val bridgeToken = state.bridgeToken ?: return
                updateTokenAndInput(bridgeToken, BigDecimal.ZERO)
                handleUpdateFee(sendFee = state.fee, isInputEmpty = true)
                updateButtons(
                    errorButton = TextContainer.Res(R.string.main_enter_the_amount),
                    sliderButton = null
                )
            }
        }
        view?.apply {
            showFeeViewVisible(isVisible = true)
            showFeeViewLoading(isLoading = false)
            setInputColor(R.color.text_night)
        }
    }

    private fun handleLoadingState(state: SendState.Loading) {
        handleStaticState(state.lastStaticState)
        view?.apply {
            when (state) {
                is SendState.Loading.Fee -> {
                    showFeeViewVisible(isVisible = true)
                    setFeeLabel(resources.getString(R.string.send_fees))
                    showFeeViewLoading(isLoading = true)
                    updateButtons(
                        errorButton = TextContainer.Res(R.string.send_calculating_fees),
                        sliderButton = null
                    )
                }
            }
        }
    }

    private fun handleErrorState(state: SendState.Exception) {
        handleStaticState(state.lastStaticState)
        view?.apply {
            when (state) {
                is SendState.Exception.Feature -> {
                    showFeeViewLoading(isLoading = false)
                    when (val featureException = state.featureException) {
                        is SendFeatureException.FeeLoadingError -> {
                            setFeeLabel(resources.getString(R.string.send_fees))
                            showFeeViewVisible(false)
                            updateButtons(
                                errorButton = TextContainer.Res(R.string.send_cant_calculate_fees_error),
                                sliderButton = null
                            )
                        }
                        is SendFeatureException.NotEnoughAmount -> {
                            setInputColor(R.color.text_rose)

                            val bridgeToken = state.lastStaticState.bridgeToken ?: return
                            updateTokenAndInput(bridgeToken, featureException.invalidAmount)
                            updateButtons(
                                errorButton = TextContainer.ResParams(
                                    R.string.send_max_warning_text_format,
                                    listOf(bridgeToken.tokenAmount, bridgeToken.token.tokenSymbol)
                                ),
                                sliderButton = null
                            )
                        }
                        is SendFeatureException.InsufficientFunds -> {
                            setInputColor(R.color.text_rose)
                            updateButtons(
                                errorButton = TextContainer.Res(R.string.error_insufficient_funds),
                                sliderButton = null
                            )
                        }
                    }
                }
                is SendState.Exception.Other -> if (state.exception.isConnectionError()) {
                    view?.showUiKitSnackBar(
                        message = resources.getString(R.string.error_no_internet_message),
                        actionButtonResId = R.string.common_hide
                    )
                } else {
                    view?.showUiKitSnackBar(message = state.exception.message)
                }
            }
        }
    }

    // di inject initialData
    override fun setInitialData(selectedToken: Token.Active?, inputAmount: BigDecimal?) = Unit

    private fun initialize(view: BridgeSendContract.View) {
        calculationMode.onCalculationCompleted = { view.showAroundValue(it) }
        calculationMode.onInputFractionUpdated = { view.updateInputFraction(it) }
        calculationMode.onLabelsUpdated = { switchSymbol, mainSymbol ->
            view.setSwitchLabel(switchSymbol)
            view.setMainAmountLabel(mainSymbol)
        }
        val token = initialData.initialToken?.token
        token?.let { calculationMode.updateToken(token) }

        initialData.initialAmount?.let { inputAmount ->
            setupDefaultFields(inputAmount)
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

    private fun BridgeSendContract.View.updateButtons(errorButton: TextContainer?, sliderButton: String?) {
        setSliderText(sliderButton)
        setBottomButtonText(errorButton)
    }

    private fun BridgeSendContract.View.updateTokenAndInput(token: SendToken.Bridge, amount: BigDecimal) {
        showToken(token.token)
        calculationMode.updateToken(token.token)
        val oldInputAmount = calculationMode.formatInputAmount
        calculationMode.updateTokenAmount(amount)
        val inputAmount = calculationMode.formatInputAmount
        if (oldInputAmount != inputAmount && calculationMode.getCurrencyMode() !is CurrencyMode.Fiat) {
            updateInputValue(inputAmount, true)
        }
        view?.setMaxButtonVisible(calculationMode.inputAmountDecimal.isZero())
    }

    private fun BridgeSendContract.View.handleUpdateFee(sendFee: SendFee?, isInputEmpty: Boolean) {
        val fees = bridgeSendUiMapper.getFeesFormatted(
            bridgeFee = sendFee as? SendFee.Bridge,
            isInputEmpty = isInputEmpty
        )
        setFeeLabel(fees)
        // FIXME: only for debug needs, remove after release
        // if (BuildConfig.DEBUG) buildDebugInfo(feeRelayerState.solanaFee)
    }

    override fun onTokenClicked() {
        newSendAnalytics.logTokenSelectionClicked()
        launch {
            val token = currentState.lastStaticState.bridgeToken?.token
            val tokens = bridgeInteractor.supportedSendTokens()
            view?.showTokenSelection(tokens = tokens, selectedToken = token)
        }
    }

    override fun updateToken(newToken: Token.Active) {
        stateMachine.newAction(SendFeatureAction.NewToken(SendToken.Bridge(newToken)))
    }

    override fun switchCurrencyMode() {
        view?.updateInputValue(calculationMode.switchAndUpdateInputAmount(), true)
    }

    override fun updateInputAmount(amount: String) {
        calculationMode.updateInputAmount(amount)
        val currentAmount = calculationMode.getCurrentAmount()
        if (currentAmount.isZero()) {
            stateMachine.newAction(SendFeatureAction.ZeroAmount)
        } else {
            stateMachine.newAction(SendFeatureAction.AmountChange(currentAmount))
        }

        newSendAnalytics.setMaxButtonClicked(isClicked = false)
    }

    override fun onMaxButtonClicked() {
        val token = currentState.lastStaticState.bridgeToken?.token ?: return
        stateMachine.newAction(SendFeatureAction.MaxAmount)
        calculationMode.updateTokenAmount(token.total)
        view?.updateInputValue(calculationMode.formatInputAmount, true)
        newSendAnalytics.setMaxButtonClicked(isClicked = true)
        val message = resources.getString(R.string.send_using_max_amount, token.tokenSymbol)
        view?.showToast(TextContainer.Raw(message))
    }

    override fun onFeeInfoClicked() {
        val token = currentState.lastStaticState.bridgeToken?.token ?: error("Token cannot be null!")
        val fees = currentState.lastStaticState.bridgeFee?.fee
        if (calculationMode.isCurrentInputEmpty() && fees == null) {
            // TODO check free state works correct
            newSendAnalytics.logFreeTransactionsClicked()
            view?.showFreeTransactionsInfo()
        } else {
            val feeDetails = getFeeDetails(token)
            view?.showTransactionDetails(feeDetails)
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

    override fun finishFeature() {
        stateMachine.finishWork()
    }

    override fun send() {
        val token = currentState.lastStaticState.bridgeToken?.token ?: error("Token cannot be null!")

        val address = initialData.recipient
        val currentAmount = calculationMode.getCurrentAmount()
        val currentAmountUsd = calculationMode.getCurrentAmountUsd()
        val lamports = calculationMode.getCurrentAmountLamports()

        logSendClicked(token, currentAmount.toPlainString(), currentAmountUsd.orZero().toPlainString())

        // the internal id for controlling the transaction state
        val internalTransactionId = UUID.randomUUID().toString()

        appScope.launch {
            val transactionDate = Date()
            try {
                val feeDetails = getFeeDetails(token)
                val progressDetails = bridgeSendUiMapper.prepareShowProgress(
                    tokenToSend = token,
                    amountTokens = "${currentAmount.toPlainString()} ${token.tokenSymbol}",
                    amountUsd = currentAmountUsd?.asNegativeUsdTransaction(),
                    recipient = recipientAddress.nicknameOrAddress(),
                    feeDetails = feeDetails
                )

                view?.showProgressDialog(internalTransactionId, progressDetails)

                val bridgeFee = currentState.lastStaticState.bridgeFee

                val result = bridgeInteractor.sendTransaction(
                    recipient = address,
                    token = token,
                    feePayerToken = bridgeFee?.tokenToPayFee,
                    feeRelayerFee = bridgeFee?.feeRelayerFee,
                    amountInLamports = lamports
                )
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

    private fun getFeeDetails(tokenToSend: Token.Active): BridgeFeeDetails {
        val fees = currentState.lastStaticState.bridgeFee?.fee
        return bridgeSendUiMapper.makeBridgeFeeDetails(
            recipientAddress = recipientAddress.addressState.address,
            tokenToSend = tokenToSend,
            calculationMode = calculationMode,
            fees = fees
        )
    }

    private fun SearchResult.nicknameOrAddress(): String {
        return if (this is SearchResult.UsernameFound) getFormattedUsername()
        else addressState.address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)
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

    private fun isInternetConnectionEnabled(): Boolean =
        connectionStateProvider.hasConnection()

    private fun logSendClicked(token: Token.Active, amountInToken: String, amountInUsd: String) {
        val solanaFee = true // todo
        newSendAnalytics.logSendConfirmButtonClicked(
            tokenName = token.tokenName,
            amountInToken = amountInToken,
            amountInUsd = amountInUsd,
            isFeeFree = solanaFee,
            mode = calculationMode.getCurrencyMode()
        )
    }
}
