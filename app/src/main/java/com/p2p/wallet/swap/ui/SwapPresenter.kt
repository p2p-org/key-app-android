package com.p2p.wallet.swap.ui

import com.p2p.wallet.R
import com.p2p.wallet.amount.toPowerValue
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
import java.math.BigInteger
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

    private var sourceAmount: BigInteger = BigInteger.ZERO

    private var slippage: Double = 0.1

    private var searchPoolJob: Job? = null
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
        searchPool()
        setButtonEnabled()
    }

    override fun setNewDestinationToken(newToken: Token) {
        destinationToken = newToken
        searchPool()
        setButtonEnabled()
    }

    override fun setSlippage(slippage: Double) {
        this.slippage = slippage
    }

    override fun loadSlippageForSelection() {
        view?.openSlippageSelection(Slippage.parse(slippage))
    }

    override fun feedAvailableValue() {
        view?.updateInputValue(requireSourceToken().total.toBigInteger())
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
        sourceAmount = amount.toBigIntegerOrNull() ?: BigInteger.ZERO
        val token = sourceToken ?: return
        val around = token.exchangeRate.toBigDecimal().times(sourceAmount.toBigDecimal())

        val isMoreThanBalance = sourceAmount > token.total.toBigInteger()
        val availableColor = if (isMoreThanBalance) R.color.colorRed else R.color.colorBlue

        view?.setAvailableTextColor(availableColor)
        view?.showAroundValue(around)

        setButtonEnabled()

        if (destinationToken != null) calculateData(requireSourceToken(), requireDestinationToken())
    }

    override fun swap() {
        launch {
            try {
                view?.showLoading(true)
                val finalAmount = sourceAmount.divide(requireSourceToken().decimals.toPowerValue())

                val request = SwapRequest(
                    requirePool(),
                    slippage,
                    finalAmount,
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
            val pool = requirePool()
            val finalAmount = sourceAmount.multiply(source.decimals.toPowerValue())

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
                pool = pool,
                inputAmount = finalAmount,
                withFee = true,
                tokenABalance = sourceBalance,
                tokenBBalance = destinationBalance
            )

            val data = CalculationsData(
                destinationAmount,
                minReceive,
                destination.tokenSymbol,
                fee,
                source.tokenSymbol,
                slippage
            )
            view?.showCalculations(data)
        }
    }

    private fun searchPool() {
        val source = sourceToken ?: return
        val destination = destinationToken ?: return

        loadPrice(false)

        searchPoolJob?.cancel()
        searchPoolJob = launch {
            val sourceMint = source.getFormattedMintAddress()
            val destinationMint = destination.getFormattedMintAddress()
            val pool = swapInteractor.findPool(sourceMint, destinationMint)

            if (pool != null) {
                currentPool = pool
                calculateData(source, destination)
            } else {
                view?.showNoPoolFound()
            }
        }
    }

    private fun setButtonEnabled() {
//        val isMoreThanBalance = sourceAmount > sourceToken?.total
//        val isEnabled = sourceAmount == BigDecimal.ZERO && !isMoreThanBalance && destinationToken != null
//        view?.showButtonEnabled(isEnabled)
    }

    private fun requirePool(): Pool.PoolInfo =
        currentPool ?: throw IllegalStateException("Pool is null")

    private fun requireSourceToken(): Token =
        sourceToken ?: throw IllegalStateException("Source token is null")

    private fun requireDestinationToken(): Token =
        destinationToken ?: throw IllegalStateException("Destination token is null")
}

data class CalculationsData(
    val destinationAmount: BigInteger,
    val minReceive: BigInteger,
    val minReceiveSymbol: String,
    val fee: BigInteger,
    val feeSymbol: String,
    val slippage: Double
)