package org.p2p.wallet.newsend

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.CurrencyMode
import org.p2p.wallet.send.model.FeePayerState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendFee
import org.p2p.wallet.send.model.SendTotal
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.properties.Delegates

private const val ROUNDING_VALUE = 6

class NewSendPresenter(
    private val sendInteractor: SendInteractor,
    private val userInteractor: UserInteractor,
    private val browseAnalytics: BrowseAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val dispatchers: CoroutineDispatchers
) : BasePresenter<NewSendContract.View>(), NewSendContract.Presenter {

    private var token: Token.Active? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showTokenToSend(newValue)
    }
    private var searchResult: SearchResult? = null
    private var solToken: Token.Active? = null

    private var inputAmount: String = Constants.ZERO_AMOUNT
    private var currencyMode: CurrencyMode = CurrencyMode.Token(Constants.SOL_SYMBOL)
    private var tokenAmount: BigDecimal = BigDecimal.ZERO
    private var usdAmount: BigDecimal = BigDecimal.ZERO

    private var sendTotal: SendTotal? = null
    private var sendFee: SendFee? = null

    private var feePayerJob: Job? = null
    private var calculationJob: Job? = null

    init {
        launch {
            token = userInteractor.getUserTokens().first()
            solToken = userInteractor.getUserSolToken()
            // TODO try catch?
        }
    }

    override fun onTokenClicked() {
        loadTokensForSelection()
    }

    override fun setTokenToSend(newToken: Token.Active) {
        token = newToken
    }

    override fun setAmount(amount: String) {
        inputAmount = amount

        val token = token ?: return
        updateMaxButtonVisibility(token)
        calculateByMode(token)

        findValidFeePayer(
            sourceToken = token,
            feePayerToken = token,
            strategy = FeePayerSelectionStrategy.SELECT_FEE_PAYER
        )
    }

    private fun updateMaxButtonVisibility(token: Token.Active) {
        val totalAvailable = when (currencyMode) {
            is CurrencyMode.Usd -> token.totalInUsd
            is CurrencyMode.Token -> token.total.scaleLong()
        } ?: return
        view?.setMaxButtonVisibility(isVisible = inputAmount != totalAvailable.toString())
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
            usdAmount.divide(token.usdRateOrZero, ROUNDING_VALUE, RoundingMode.HALF_EVEN)
                .stripTrailingZeros()
        }

        val tokenAround = if (usdAmount.isZero() || token.usdRateOrZero.isZero()) {
            BigDecimal.ZERO
        } else {
            usdAmount.divide(token.usdRateOrZero, ROUNDING_VALUE, RoundingMode.HALF_EVEN)
                .stripTrailingZeros()
        }
        view?.showAroundValue(tokenAround, token.tokenSymbol)
    }

    private fun calculateByToken(token: Token.Active) {
        tokenAmount = inputAmount.toBigDecimalOrZero()
        usdAmount = tokenAmount.multiply(token.usdRateOrZero)

        val usdAround = tokenAmount.times(token.usdRateOrZero)
        view?.showAroundValue(usdAround, Constants.USD_READABLE_SYMBOL)
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

    private fun findValidFeePayer(
        sourceToken: Token.Active,
        feePayerToken: Token.Active?,
        strategy: FeePayerSelectionStrategy
    ) {
        val feePayer = feePayerToken ?: sendInteractor.getFeePayerToken()

        feePayerJob?.cancel()
        launch(dispatchers.ui) {
            try {
                view?.showFeeViewLoading(isLoading = true)
                val (feeInSol, feeInPayingToken) = calculateFeeRelayerFee(
                    sourceToken = sourceToken,
                    feePayerToken = feePayer,
                    result = searchResult
                ) ?: return@launch
                showFeeDetails(sourceToken, feeInSol, feeInPayingToken, feePayer, strategy)
            } catch (e: Throwable) {
                Timber.e(e, "Error during FeeRelayer fee calculation")
            } finally {
                view?.showFeeViewLoading(isLoading = false)
            }
        }.also { feePayerJob = it }
    }

    private suspend fun showFeeDetails(
        sourceToken: Token.Active,
        feeInSol: BigInteger,
        feeInPayingToken: BigInteger,
        feePayerToken: Token.Active,
        strategy: FeePayerSelectionStrategy
    ) {
        val fee = buildSolanaFee(feePayerToken, sourceToken, feeInSol, feeInPayingToken)

        if (strategy == FeePayerSelectionStrategy.NO_ACTION) {
            showFees(sourceToken, fee)
            calculateTotal(fee)
        } else {
            validateAndSelectFeePayer(sourceToken, fee, strategy)
        }
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
            feeInSol = feeInSol,
            feeInPayingToken = feeInPayingToken,
            solToken = solToken
        ).also { sendFee = it }
    }

    private fun showFees(source: Token.Active, fee: SendFee.SolanaFee) {
        val inputAmount = tokenAmount.toLamports(source.decimals)
        val isEnoughToCoverExpenses = fee.isEnoughToCoverExpenses(
            sourceTokenTotal = source.totalInLamports,
            inputAmount = inputAmount
        )

        if (isEnoughToCoverExpenses) {
            sendFee = fee
            // view?.showAccountFeeView(fee = fee)
        } else {
            view?.showInsufficientFundsView(source.tokenSymbol, fee.feeUsd)
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

    private suspend fun recalculate(sourceToken: Token.Active) {
        /*
         * Optimized recalculation and UI update
         * */
        val newFeePayer = sendInteractor.getFeePayerToken()
        val (feeInSol, feeInPayingToken) = calculateFeeRelayerFee(
            sourceToken = sourceToken,
            feePayerToken = newFeePayer,
            result = searchResult
        ) ?: return
        val fee = buildSolanaFee(newFeePayer, sourceToken, feeInSol, feeInPayingToken)
        showFees(sourceToken, fee)
        calculateTotal(fee)
    }

    private fun reduceInputAmount(maxAllowedAmount: BigInteger) {
        val token = token ?: return

        val newInputAmount = maxAllowedAmount.fromLamports(token.decimals).scaleLong()
        val totalInput = when (currencyMode) {
            is CurrencyMode.Usd -> newInputAmount.toUsd(token)
            is CurrencyMode.Token -> newInputAmount
        } ?: return

        view?.showInputValue(totalInput, forced = true)

        inputAmount = totalInput.toPlainString()

        updateMaxButtonVisibility(token)
        calculateByMode(token)
    }

    private suspend fun calculateFeeRelayerFee(
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        result: SearchResult?
    ): Pair<BigInteger, BigInteger>? {
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
            Timber.w("Fee calculation is cancelled")
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
        if (fees?.feeInPayingToken == null || sourceToken.isSOL) {
            sendFee = null
            calculateTotal(sendFee = null)
            // view?.hideAccountFeeView()
            return null
        }

        return fees.feeInSol to fees.feeInPayingToken
    }

    private fun handleError() {
        sendFee = null
        calculateTotal(sendFee = null)
        // view?.hideAccountFeeView() TODO ask about fee error
        // view?.showDetailsError(R.string.send_cannot_send_token)
        // view?.showButtonText(R.string.main_select_token)
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

        // TODO updateButton

        sendTotal = data
    }
}
