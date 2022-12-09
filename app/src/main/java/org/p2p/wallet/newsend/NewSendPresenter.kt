package org.p2p.wallet.newsend

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.formatUsd
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.send.model.CurrencyMode
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.user.interactor.UserInteractor
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.properties.Delegates

private const val ROUNDING_VALUE = 6

class NewSendPresenter(
    private val userInteractor: UserInteractor,
    private val browseAnalytics: BrowseAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val resourcesProvider: ResourcesProvider,
    private val dispatchers: CoroutineDispatchers
) : BasePresenter<NewSendContract.View>(), NewSendContract.Presenter {

    private var token: Token.Active? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showTokenToSend(newValue)
    }
    private var searchResult: SearchResult? = null

    private var inputAmount: String = Constants.ZERO_AMOUNT
    private var currencyMode: CurrencyMode = CurrencyMode.Usd
    private var tokenAmount: BigDecimal = BigDecimal.ZERO
    private var usdAmount: BigDecimal = BigDecimal.ZERO

    private var calculationJob: Job? = null

    init {
        launch(dispatchers.ui) {
            token = userInteractor.getUserTokens().first()
            updateValues()
        }
    }

    override fun attach(view: NewSendContract.View) {
        super.attach(view)
        updateValues()
    }

    private fun updateValues() {
        val token = token ?: return
        val switchSymbol: String
        val mainSymbol: String
        when (currencyMode) {
            is CurrencyMode.Token -> {
                switchSymbol = Constants.USD_READABLE_SYMBOL
                mainSymbol = token.tokenSymbol
            }
            is CurrencyMode.Usd -> {
                switchSymbol = token.tokenSymbol
                mainSymbol = Constants.USD_READABLE_SYMBOL
            }
        }
        updateMaxButtonVisibility(token)
        validateAmounts(token)
        calculateByMode(token)
        view?.setSwitchLabel(switchSymbol)
        view?.setMainAmountLabel(mainSymbol)
    }

    override fun onTokenClicked() {
        loadTokensForSelection()
    }

    override fun setTokenToSend(newToken: Token.Active) {
        token = newToken
        updateValues()
    }

    override fun switchCurrencyMode() {
        val token = token ?: return
        currencyMode = when (currencyMode) {
            is CurrencyMode.Token -> {
                CurrencyMode.Usd
            }
            is CurrencyMode.Usd -> {
                CurrencyMode.Token(token.tokenSymbol)
            }
        }
        updateValues()
    }

    override fun setAmount(amount: String) {
        inputAmount = amount

        val token = token ?: return
        updateMaxButtonVisibility(token)
        validateAmounts(token)
        calculateByMode(token)
    }

    private fun updateMaxButtonVisibility(token: Token.Active) {
        val totalAvailable = when (currencyMode) {
            is CurrencyMode.Usd -> token.totalInUsd
            is CurrencyMode.Token -> token.total.scaleLong()
        } ?: return
        // TODO PWN-6092 check on max sum - min sum for creation account - 0.00089088 SOL
        view?.setMaxButtonVisibility(isVisible = inputAmount == emptyString())
    }

    private fun validateAmounts(token: Token.Active) {
        // TODO PWN-6092 check min and max amount for SOL
        val minAmount: BigDecimal
        val currencySymbol: String
        val maxAmount: BigDecimal
        val inputValue = inputAmount.toBigDecimalOrZero()
        when (currencyMode) {
            is CurrencyMode.Token -> {
                currencySymbol = token.tokenSymbol
                minAmount = if (token.isSOL) {
                    0.0000002.toBigDecimal() // TODO update PWN-6092
                } else {
                    BigDecimal.ZERO
                }
                maxAmount = token.total
            }
            is CurrencyMode.Usd -> {
                currencySymbol = Constants.USD_READABLE_SYMBOL
                minAmount = BigDecimal.ZERO
                maxAmount = token.totalInUsd.orZero()
            }
        }

        var hasIssue = false
        var issueText: String = emptyString()
        var validText: String = emptyString()
        when {
            inputValue == BigDecimal.ZERO -> {
                hasIssue = true
                issueText = resourcesProvider.getString(R.string.send_enter_amount)
            }
            inputValue < minAmount -> {
                hasIssue = true
                issueText = resourcesProvider.getString(
                    R.string.send_min_warning_text_format,
                    minAmount.scaleLong().toPlainString(),
                    currencySymbol
                )
            }
            inputValue > maxAmount -> {
                hasIssue = true
                issueText = resourcesProvider.getString(
                    R.string.send_max_warning_text_format,
                    maxAmount.scaleLong().toPlainString(),
                    currencySymbol
                )
            }
            else -> {
                validText = resourcesProvider.getString(
                    R.string.send_slider_text_format,
                    inputAmount,
                    currencySymbol
                )
            }
        }
        view?.setBottomButtonVisibility(isVisible = hasIssue)
        view?.setBottomButtonText(issueText)
        view?.setSliderText(validText)
    }

    private fun calculateByMode(token: Token.Active) {
        if (calculationJob?.isActive == true) return

        launch(dispatchers.ui) {
            when (currencyMode) {
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
            usdAmount.divide(token.usdRateOrZero, ROUNDING_VALUE, RoundingMode.HALF_EVEN).stripTrailingZeros()
        }

        val tokenAround = if (usdAmount.isZero() || token.usdRateOrZero.isZero()) {
            BigDecimal.ZERO
        } else {
            usdAmount.divide(token.usdRateOrZero, ROUNDING_VALUE, RoundingMode.HALF_EVEN)
                .stripTrailingZeros()
        }

        view?.showAroundValue("${tokenAround.formatToken()} ${token.tokenSymbol}")
    }

    private fun calculateByToken(token: Token.Active) {
        tokenAmount = inputAmount.toBigDecimalOrZero()
        usdAmount = tokenAmount.multiply(token.usdRateOrZero)

        val usdAround = tokenAmount.times(token.usdRateOrZero)
        view?.showAroundValue("${usdAround.formatUsd()} ${Constants.USD_READABLE_SYMBOL}")
    }

    override fun setMaxAmountValue() {
        val token = token ?: return

        val totalAvailable = when (currencyMode) {
            is CurrencyMode.Usd -> token.totalInUsd
            is CurrencyMode.Token -> token.total.scaleLong()
        } ?: return

        view?.showInputValue(totalAvailable, forced = false)

        val message = resourcesProvider.getString(R.string.send_using_max_amount, token.tokenSymbol)
        view?.showUiKitSnackBar(message)

        inputAmount = totalAvailable.toString()

        updateMaxButtonVisibility(token)

        calculateByMode(token)
    }

    private fun loadTokensForSelection() {
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
}
