package com.p2p.wallet.swap.ui

import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.swap.interactor.SwapInteractor
import com.p2p.wallet.swap.model.Slippage
import com.p2p.wallet.swap.model.SwapRequest
import com.p2p.wallet.swap.model.SwapResult
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.fromLamports
import com.p2p.wallet.utils.isMoreThan
import com.p2p.wallet.utils.isZero
import com.p2p.wallet.utils.scaleLong
import com.p2p.wallet.utils.scaleMedium
import com.p2p.wallet.utils.toBigDecimalOrZero
import com.p2p.wallet.utils.toLamports
import com.p2p.wallet.utils.toPowerValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.types.TokenAccountBalance
import timber.log.Timber
import java.math.BigDecimal
import kotlin.properties.Delegates

class SwapPresenter(
    private val initialToken: Token?,
    private val userInteractor: UserInteractor,
    private val swapInteractor: SwapInteractor
) : BasePresenter<SwapContract.View>(), SwapContract.Presenter {

    private var sourceToken: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    private var destinationToken: Token? by Delegates.observable(null) { _, _, newValue ->
        view?.showDestinationToken(newValue)
    }

    private var currentPool: Pool.PoolInfo? = null
    private var sourceBalance: TokenAccountBalance? = null
    private var destinationBalance: TokenAccountBalance? = null

    private var isReverse: Boolean = false
    private var reverseJob: Job? = null

    private var sourceAmount: String = "0"
    private var destinationAmount: String = "0"

    private var aroundValue: BigDecimal = BigDecimal.ZERO
    private var slippage: Double = 0.1

    private var searchPoolJob: Job? = null
    private var calculationJob: Job? = null

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            val source = initialToken ?: userInteractor.getTokens().firstOrNull() ?: return@launch
            sourceToken = source

            swapInteractor.loadAllPools()

            view?.showSlippage(slippage)
            view?.showFullScreenLoading(false)
        }
    }

    override fun loadTokensForSourceSelection() {
        launch {
            val tokens = userInteractor.getTokens()
            val pools = swapInteractor.getAllPools()
            val result = tokens.filter { token ->

                if (destinationToken == null) return@filter true

                val pool = pools.firstOrNull {
                    it.swapData.mintA.toBase58() == token.mintAddress ||
                        it.swapData.mintB.toBase58() == token.mintAddress
                }
                pool != null
            }
            view?.openSourceSelection(result)
        }
    }

    override fun loadTokensForDestinationSelection() {
        launch {
            val tokens = userInteractor.getTokens()
            val pools = swapInteractor.getAllPools()

            val result = tokens.filter { token ->
                val pool = pools.firstOrNull {
                    val pool = pools.firstOrNull {
                        it.swapData.mintB.toBase58() == token.mintAddress &&
                            sourceToken!!.mintAddress == it.swapData.mintA.toBase58() ||

                            it.swapData.mintA.toBase58() == token.mintAddress &&
                            it.swapData.mintB.toBase58() == sourceToken!!.mintAddress
                    }
                    pool != null && token.publicKey != sourceToken?.publicKey
                }
                pool != null
            }
            view?.openDestinationSelection(result)
        }
    }

    override fun setNewSourceToken(newToken: Token) {
        if (sourceToken == newToken) return
        sourceToken = newToken
        destinationToken = null
        view?.hidePrice()
        destinationAmount = "0"

        view?.hideCalculations()
        view?.showButtonText(R.string.main_select_token)

        updateButtonText(newToken)
        setButtonEnabled()
    }

    override fun setNewDestinationToken(newToken: Token) {
        destinationToken = newToken
        searchPool()
        updateButtonText(sourceToken!!)
        setButtonEnabled()
    }

    override fun setSlippage(slippage: Double) {
        this.slippage = slippage
        view?.showSlippage(slippage)
    }

    override fun loadDataForSwapSettings() {
        view?.openSwapSettings(Slippage.parse(slippage))
    }

    override fun loadSlippage() {
        view?.openSlippageDialog(Slippage.parse(slippage))
    }

    override fun feedAvailableValue() {
        view?.updateInputValue(requireSourceToken().total.scaleLong())
    }

    override fun loadPrice(toggle: Boolean) {
        if (toggle) isReverse = !isReverse

        reverseJob?.cancel()
        reverseJob = launch {
            try {
                val source = requireSourceToken()
                val destination = requireDestinationToken()
                val sourceSymbol = if (isReverse) destination.tokenSymbol else source.tokenSymbol
                val destinationSymbol = if (isReverse) source.tokenSymbol else destination.tokenSymbol
                val priceInSingleValue = userInteractor.getPriceByToken(sourceSymbol, destinationSymbol)
                view?.showPrice(priceInSingleValue, destinationSymbol, sourceSymbol)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading price")
            }
        }
    }

    override fun setSourceAmount(amount: String) {
        sourceAmount = amount
        val token = sourceToken ?: return

        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = token.usdRate.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(token.total)
        val availableColor = if (isMoreThanBalance) R.attr.colorAccentWarning else R.attr.colorAccentPrimary

        view?.setAvailableTextColor(availableColor)
        view?.showAroundValue(aroundValue)

        calculateData(requireSourceToken(), destinationToken)

        setButtonEnabled()
    }

    override fun reverseTokens() {
        val source = sourceToken
        sourceToken = destinationToken
        destinationToken = source

        searchPool()
        setButtonEnabled()
    }

    override fun swap() {
        launch {
            try {
                view?.showLoading(true)
                val finalAmount = sourceAmount.toBigDecimalOrZero()
                    .multiply(requireSourceToken().decimals.toPowerValue())
                    .toBigInteger()

                val request = SwapRequest(
                    pool = requirePool(),
                    slippage = slippage,
                    amount = finalAmount,
                    balanceA = sourceBalance!!,
                    balanceB = destinationBalance!!
                )
                val data = swapInteractor.swap(
                    request = request,
                    receivedAmount = destinationAmount.toBigDecimalOrZero(),
                    usdReceivedAmount = aroundValue,
                    tokenSymbol = requireDestinationToken().tokenSymbol
                )
                handleResult(data)
            } catch (e: Throwable) {
                Timber.e(e, "Error swapping tokens")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun calculateData(source: Token, destination: Token?) {
        if (destination == null) return
        calculationJob?.cancel()
        calculationJob = launch {
            val pool = requirePool()
            val balanceA =
                swapInteractor.loadTokenBalance(pool.tokenAccountA).also { sourceBalance = it }
            val balanceB =
                swapInteractor.loadTokenBalance(pool.tokenAccountB).also { destinationBalance = it }

            val calculatedAmount = swapInteractor.calculateAmountInOtherToken(
                pool = pool,
                inputAmount = sourceAmount.toBigDecimalOrZero().toLamports(source.decimals),
                withFee = false,
                tokenABalance = balanceA,
                tokenBBalance = balanceB
            ).fromLamports(destination.decimals).scaleMedium()

            destinationAmount = calculatedAmount.toString()

            val fee = swapInteractor.calculateFee(
                pool = pool,
                inputAmount = sourceAmount.toBigDecimalOrZero().toLamports(source.decimals),
                tokenABalance = balanceA,
                tokenBBalance = balanceB
            )
            val minReceive = swapInteractor.calculateMinReceive(
                balanceA = balanceA,
                balanceB = balanceB,
                amount = sourceAmount.toBigDecimalOrZero().toLamports(source.decimals),
                slippage = slippage
            )

            val data = CalculationsData(
                destinationAmount = destinationAmount,
                minReceive = minReceive.fromLamports(destination.decimals).scaleMedium(),
                minReceiveSymbol = destination.tokenSymbol,
                fee = "${fee.fromLamports(destination.decimals).scaleMedium()} ${destination.tokenSymbol}",
                liquidityProviderFee = "0.0000075 BTC",
                feeSymbol = destination.tokenSymbol
            )

            updateButtonText(source)
            view?.showCalculations(data)
            view?.showSlippage(slippage)
        }
    }

    private fun searchPool() {
        val source = sourceToken ?: return
        val destination = destinationToken ?: return

        loadPrice(false)

        searchPoolJob?.cancel()
        searchPoolJob = launch {
            val sourceMint = source.mintAddress
            val destinationMint = destination.mintAddress
            val pool = swapInteractor.findPool(sourceMint, destinationMint)

            if (pool != null) {
                currentPool = pool
                calculateData(source, destination)
            }
        }
    }

    private fun setButtonEnabled() {
        val isMoreThanBalance = sourceAmount.toBigDecimalOrZero() > sourceToken?.total ?: BigDecimal.ZERO
        val isEnabled = sourceAmount.toBigDecimalOrZero().compareTo(BigDecimal.ZERO) != 0 &&
            !isMoreThanBalance && destinationToken != null
        view?.showButtonEnabled(isEnabled)
    }

    private fun handleResult(result: SwapResult) {
        when (result) {
            is SwapResult.Success -> {
                val info = TransactionInfo(
                    transactionId = result.transactionId,
                    status = R.string.main_send_success,
                    message = R.string.main_send_transaction_confirmed,
                    iconRes = R.drawable.ic_success,
                    amount = result.receivedAmount,
                    usdAmount = result.usdReceivedAmount,
                    tokenSymbol = result.tokenSymbol
                )
                view?.showSwapSuccess(info)
            }
            is SwapResult.Error -> {
                view?.showErrorMessage(result.messageRes)
            }
        }
    }

    private fun updateButtonText(source: Token) {
        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = source.usdRate.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(source.total)

        when {
            isMoreThanBalance -> view?.showButtonText(R.string.swap_funds_not_enough)
            decimalAmount.isZero() -> view?.showButtonText(R.string.main_enter_the_amount)
            destinationToken == null -> view?.showButtonText(R.string.main_select_token)
            else -> view?.showButtonText(R.string.main_swap_now)
        }
    }

    private fun requirePool(): Pool.PoolInfo =
        currentPool ?: throw IllegalStateException("Pool is null")

    private fun requireSourceToken(): Token =
        sourceToken ?: throw IllegalStateException("Source token is null")

    private fun requireDestinationToken(): Token =
        destinationToken ?: throw IllegalStateException("Destination token is null")
}

data class CalculationsData(
    val destinationAmount: String,
    val minReceive: BigDecimal,
    val minReceiveSymbol: String,
    val fee: String,
    val liquidityProviderFee: String,
    val feeSymbol: String
)