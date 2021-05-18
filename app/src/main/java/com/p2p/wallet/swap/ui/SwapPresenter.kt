package com.p2p.wallet.swap.ui

import com.p2p.wallet.R
import com.p2p.wallet.amount.toDecimalValue
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.swap.interactor.SwapInteractor
import com.p2p.wallet.swap.model.Slippage
import com.p2p.wallet.swap.model.SwapRequest
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.rpc.types.TokenAccountBalance
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.properties.Delegates

class SwapPresenter(
    private val userInteractor: UserInteractor,
    private val swapInteractor: SwapInteractor
) : BasePresenter<SwapContract.View>(), SwapContract.Presenter {

    /**
     * 1. Load all pools, cache it
     * 2. find pool by mint a and mint b
     * 3. Get balance of tokenA and tokenB
     * 4. Calculate amount, fee, min receive
     * 5. Swap
     * */

    private var sourceToken: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    private var destinationToken: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showDestinationToken(newValue)
    }

    private var currentPool: Pool.PoolInfo? = null
    private var sourceBalance: TokenAccountBalance? = null
    private var destinationBalance: TokenAccountBalance? = null

    private var isReverse: Boolean = false
    private var reverseJob: Job? = null

    private var sourceAmount: BigDecimal = BigDecimal.ZERO

    private var slippage: Double = 0.1

    private var calculationJob: Job? = null

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            val tokens = userInteractor.getTokens()
            val source = tokens.firstOrNull() ?: return@launch
            sourceToken = source

            swapInteractor.loadAllPools()

            view?.showFullScreenLoading(false)
        }
    }

    override fun loadTokensForSourceSelection() {
        launch {
            val tokens = userInteractor.getTokens()
            val pools = swapInteractor.getAllPools()
            val result = tokens.filter { token ->
                val pool = pools.firstOrNull { it.swapData.mintA.toBase58() == token.getFormattedMintAddress() }
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
                val pool = pools.firstOrNull { it.swapData.mintB.toBase58() == token.getFormattedMintAddress() }
                pool != null
            }
            view?.openDestinationSelection(result)
        }
    }

    override fun setNewSourceToken(newToken: Token) {
        sourceToken = newToken
        setButtonEnabled()
    }

    override fun setNewDestinationToken(newToken: Token) {
        destinationToken = newToken

        calculateData(sourceToken!!, newToken)
        setButtonEnabled()
    }

    override fun setSlippage(slippage: Double) {
        this.slippage = slippage
        view?.showSlippage(slippage)
    }

    override fun loadSlippageForSelection() {
        view?.openSlippageSelection(Slippage.parse(slippage))
    }

    override fun loadAvailableAmount() {
        view?.updateInputValue(sourceToken!!.total)
    }

    override fun togglePrice() {
        reverseJob?.cancel()
        reverseJob = launch {
            try {
                val sourceSymbol = if (isReverse) destinationToken!!.tokenSymbol else sourceToken!!.tokenSymbol
                val destinationSymbol = if (isReverse) sourceToken!!.tokenSymbol else destinationToken!!.tokenSymbol
                val priceInSingleValue = userInteractor.getPriceByToken(sourceSymbol, destinationSymbol)
                view?.showPrice(priceInSingleValue, destinationSymbol, sourceSymbol)

                isReverse = !isReverse
            } catch (e: Throwable) {
                Timber.e(e, "Error loading price")
            }
        }
    }

    override fun setSourceAmount(amount: BigDecimal) {
        sourceAmount = amount
        val token = sourceToken ?: return
        val around = token.exchangeRate.times(amount)

        val isMoreThanBalance = amount > token.total
        val availableColor = if (isMoreThanBalance) R.color.colorRed else R.color.colorBlue

        view?.setAvailableTextColor(availableColor)
        view?.showAroundValue(around.setScale(6, RoundingMode.HALF_EVEN))

        setButtonEnabled()

        if (destinationToken != null) calculateData(sourceToken!!, destinationToken!!)
    }

    override fun swap() {
        launch {
            try {
                view?.showLoading(true)
                val pool = currentPool!!
                val request = SwapRequest(
                    pool,
                    slippage,
                    sourceAmount.toDecimalValue().toBigInteger(),
                    sourceBalance!!,
                    destinationBalance!!
                )
                val data = swapInteractor.swap(request)
                view?.showSwapSuccess()
            } catch (e: Throwable) {
                Timber.e(e, "Error swapping tokens")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun calculateData(source: Token, destination: Token) {
        calculationJob?.cancel()
        calculationJob = launch {
            togglePrice()

            val pool = swapInteractor.findPool(source.getFormattedMintAddress(), destination.getFormattedMintAddress())
            if (pool == null) {
                view?.showNoPoolFound()
                return@launch
            }
            currentPool = pool

            val finalAmount = sourceAmount.multiply(source.decimals.toDecimalValue()).toBigInteger()

            val sourceBalance =
                swapInteractor.loadTokenBalance(pool.tokenAccountA).also { sourceBalance = it }
            val destinationBalance =
                swapInteractor.loadTokenBalance(pool.tokenAccountB).also { destinationBalance = it }

            val fee = swapInteractor.calculateFee(
                pool,
                finalAmount,
                sourceBalance,
                destinationBalance
            )
            val minReceive = swapInteractor.calculateMinReceive(
                sourceBalance,
                destinationBalance,
                finalAmount,
                slippage
            )

            Timber.d("### fee $fee minReceive $minReceive final $finalAmount")

            val destinationAmount = swapInteractor.calculateAmountInOtherToken(
                pool, finalAmount, true, sourceBalance, destinationBalance
            )

            val data = CalculationsData(
                destinationAmount,
                minReceive,
                destination.tokenSymbol,
                fee,
                source.tokenSymbol
            )
            view?.showCalculations(data)
        }
    }

    private fun setButtonEnabled() {
//        val isMoreThanBalance = sourceAmount > sourceToken?.total
//        val isEnabled = sourceAmount == BigDecimal.ZERO && !isMoreThanBalance && destinationToken != null
//        view?.showButtonEnabled(isEnabled)
    }
}

data class CalculationsData(
    val destinationAmount: BigInteger,
    val minReceive: BigInteger,
    val minReceiveSymbol: String,
    val fee: BigInteger,
    val feeSymbol: String
)