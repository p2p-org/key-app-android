package org.p2p.wallet.svl.ui.send

import android.content.res.Resources
import timber.log.Timber
import kotlin.properties.Delegates.observable
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.newsend.SendFeeRelayerManager
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.interactor.SendViaLinkInteractor
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.FeeRelayerState
import org.p2p.wallet.newsend.model.NewSendButtonState
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.newsend.model.toSearchResult
import org.p2p.wallet.svl.model.SendLinkGenerator
import org.p2p.wallet.updates.ConnectionStateProvider
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.unsafeLazy

class SendViaLinkPresenter(
    private val userInteractor: UserInteractor,
    sendInteractor: SendInteractor,
    private val sendViaLinkInteractor: SendViaLinkInteractor,
    private val resources: Resources,
    private val connectionStateProvider: ConnectionStateProvider,
    private val newSendAnalytics: NewSendAnalytics,
    sendModeProvider: SendModeProvider
) : BasePresenter<SendViaLinkContract.View>(), SendViaLinkContract.Presenter {

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
    private val feeRelayerManager = SendFeeRelayerManager(sendInteractor, userInteractor)

    private val recipient: TemporaryAccount by unsafeLazy {
        SendLinkGenerator.createTemporaryAccount()
    }

    private var selectedToken: Token.Active? = null

    override fun attach(view: SendViaLinkContract.View) {
        super.attach(view)
        newSendAnalytics.logNewSendScreenOpened()

        initialize(view)
    }

    override fun setInitialData(selectedToken: Token.Active?) {
        this.selectedToken = selectedToken
    }

    private fun initialize(view: SendViaLinkContract.View) {
        calculationMode.onCalculationCompleted = { view.showAroundValue(it) }
        calculationMode.onInputFractionUpdated = { view.updateInputFraction(it) }
        calculationMode.onLabelsUpdated = { switchSymbol, mainSymbol ->
            view.setSwitchLabel(switchSymbol)
            view.setMainAmountLabel(mainSymbol)
        }

        if (token != null) {
            restoreSelectedToken(view, token!!)
        } else {
            setupInitialToken(view)
        }

        launch {
            try {
                sendViaLinkInteractor.initialize()
            } catch (e: Throwable) {
                Timber.e(e, "Error initializing send via link")
                view.showUiKitSnackBar(resources.getString(R.string.error_general_message))
            }
        }
    }

    private fun restoreSelectedToken(view: SendViaLinkContract.View, token: Token.Active) {
        launch {
            view.showToken(token)
            calculationMode.updateToken(token)

            val userTokens = userInteractor.getNonZeroUserTokens()
            val isTokenChangeEnabled = userTokens.size > 1 && selectedToken == null
            view.setTokenContainerEnabled(isEnabled = isTokenChangeEnabled)

            view.setFeeLabel(resources.getString(R.string.send_fees_free))
            view.showFeeViewLoading(isLoading = false)
        }
    }

    private fun setupInitialToken(view: SendViaLinkContract.View) {
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

            view.setFeeLabel(resources.getString(R.string.send_fees_free))
            view.showFeeViewLoading(isLoading = false)
        }
    }

    override fun onTokenClicked() {
        newSendAnalytics.logTokenSelectionClicked()
        launch {
            val tokens = userInteractor.getUserTokens()
            val result = tokens.filterNot(Token.Active::isZero)
            view?.showTokenSelection(tokens = result, selectedToken = token)
        }
    }

    override fun updateToken(newToken: Token.Active) {
        token = newToken
        showMaxButtonIfNeeded()
        view?.showFeeViewVisible(isVisible = true)
        updateButton(requireToken())
    }

    override fun switchCurrencyMode() {
        val newMode = calculationMode.switchMode()
        newSendAnalytics.logSwitchCurrencyModeClicked(newMode)
        view?.showFeeViewVisible(isVisible = true)
    }

    override fun updateInputAmount(amount: String) {
        calculationMode.updateInputAmount(amount)
        view?.showFeeViewVisible(isVisible = true)
        showMaxButtonIfNeeded()
        updateButton(requireToken())

        newSendAnalytics.setMaxButtonClicked(isClicked = false)
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
    }

    override fun onFeeInfoClicked() {
        newSendAnalytics.logFreeTransactionsClicked()
        view?.showFreeTransactionsInfo()
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

    override fun generateLink() {
        val token = token ?: error("Token cannot be null!")

        val currentAmount = calculationMode.getCurrentAmount()
        val currentAmountUsd = calculationMode.getCurrentAmountUsd()
        val lamports = calculationMode.getCurrentAmountLamports()

        logSendClicked(token, currentAmount.toPlainString(), currentAmountUsd.toPlainString())

        view?.navigateToLinkGeneration(recipient, token, lamports)
    }

    private fun showMaxButtonIfNeeded() {
        val isMaxButtonVisible = calculationMode.isMaxButtonVisible(feeRelayerManager.getMinRentExemption())
        view?.setMaxButtonVisible(isVisible = isMaxButtonVisible)
    }

    private fun updateButton(sourceToken: Token.Active) {
        val sendButton = NewSendButtonState(
            sourceToken = sourceToken,
            searchResult = recipient.toSearchResult(),
            calculationMode = calculationMode,
            feeRelayerState = FeeRelayerState.Idle,
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
                view?.setSliderText(resources.getString(R.string.send_via_link_action_text))
                view?.setBottomButtonText(null)
                view?.setInputColor(state.totalAmountTextColor)
            }
        }
    }

    private fun isInternetConnectionEnabled(): Boolean =
        connectionStateProvider.hasConnection()

    private fun requireToken(): Token.Active =
        token ?: error("Source token cannot be empty!")

    private fun logSendClicked(token: Token.Active, amountInToken: String, amountInUsd: String) {
        newSendAnalytics.logSendConfirmButtonClicked(
            tokenName = token.tokenName,
            amountInToken = amountInToken,
            amountInUsd = amountInUsd,
            isFeeFree = true,
            mode = calculationMode.getCurrencyMode()
        )
    }
}
