package com.p2p.wallet.main.ui.swap

import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.properties.Delegates

class SwapPresenter(
    private val userInteractor: UserInteractor
) : BasePresenter<SwapContract.View>(), SwapContract.Presenter {

    private var sourceToken: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    private var destinationToken: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showDestinationToken(newValue)
    }

    private var sourceAmount: BigDecimal = BigDecimal.ZERO

    private var slippage: Double = 0.1

    private var calculationJob: Job? = null

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            val tokens = userInteractor.getTokens()
            val source = tokens.firstOrNull() ?: return@launch
            sourceToken = source
            view?.showFullScreenLoading(false)
        }
    }

    override fun loadTokensForSourceSelection() {
        launch {
            val tokens = userInteractor.getTokens()
            view?.openSourceSelection(tokens)
        }
    }

    override fun loadTokensForDestinationSelection() {
        launch {
            val tokens = userInteractor.getTokens()
            view?.openDestinationSelection(tokens)
        }
    }

    override fun setNewSourceToken(newToken: Token) {
        sourceToken = newToken
    }

    override fun setNewDestinationToken(newToken: Token) {
        destinationToken = newToken

        calculateData(sourceToken!!, newToken)
    }

    override fun setSlippage(slippage: Double) {
        this.slippage = slippage
    }

    override fun setSourceAmount(amount: BigDecimal) {
        sourceAmount = amount
        val token = sourceToken!!
        val around = token.exchangeRate.times(sourceAmount)

        val isMoreThanBalance = sourceAmount > token.total
        val availableColor = if (isMoreThanBalance) R.color.colorRed else R.color.colorBlue

        view?.setAvailableTextColor(availableColor)
        view?.showAroundValue(around)

        val isEnabled = sourceAmount == BigDecimal.ZERO && !isMoreThanBalance && destinationToken != null
        view?.showButtonEnabled(isEnabled)

        if (destinationToken != null) calculateData(sourceToken!!, destinationToken!!)
    }

    private fun calculateData(source: Token, destination: Token) {
        calculationJob?.cancel()
        calculationJob = launch {
            val exchangeRate = userInteractor.getPriceByToken(source.tokenSymbol, destination.tokenSymbol)
            val price = exchangeRate * sourceAmount
            view?.showPrice(price, destination.tokenSymbol, source.tokenSymbol)

//            val data = CalculationsData(
//
//            )
//            view?.showCalculations()
        }
    }
}

data class CalculationsData(
    val minReceive: BigDecimal,
    val minReceiveSymbol: String,
    val fee: BigDecimal,
    val feeSymbol: String,
    val slippage: BigDecimal
)