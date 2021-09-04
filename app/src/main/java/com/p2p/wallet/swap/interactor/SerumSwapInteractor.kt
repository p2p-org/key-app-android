package com.p2p.wallet.swap.interactor

import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.swap.repository.SerumSwapRepository
import com.p2p.wallet.utils.isUsdx
import com.p2p.wallet.utils.toLamports
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.TransactionInstruction
import org.p2p.solanaj.serumswap.Market
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions.dexPID
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions.usdcMint
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions.usdtMint
import org.p2p.solanaj.serumswap.model.Bbo
import org.p2p.solanaj.serumswap.model.ExchangeRate
import org.p2p.solanaj.serumswap.model.OrderbookPair
import org.p2p.solanaj.serumswap.model.Side
import org.p2p.solanaj.serumswap.model.SignersAndInstructions
import org.p2p.solanaj.serumswap.model.SwapParams
import java.math.BigInteger
import kotlin.math.pow

class SerumSwapInteractor(
    private val swapInteractor2: SwapInteractor2,
    private val serumSwapRepository: SerumSwapRepository,
    private val openOrdersInteractor: OpenOrdersInteractor,
    private val swapMarketInteractor: SwapMarketInteractor,
    private val serializationInteractor: SerializationInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    companion object {
        // Close account feature flag.
        //
        // TODO: enable once the DEX supports closing open orders accounts.
        private const val CLOSE_ENABLED = false

        // Initialize open orders feature flag.
        //
        // TODO: enable once the DEX supports initializing open orders accounts.
        private const val OPEN_ENABLED = false

        const val BASE_TAKER_FEE_BPS = 0.0022
        const val FEE_MULTIPLIER = 1.0 - BASE_TAKER_FEE_BPS
    }

    private val orderBooksCache = mutableMapOf<PublicKey, OrderbookPair>()
    private val marketsCache = mutableMapOf<PublicKey, Market>()

    suspend fun loadFair(
        fromMint: PublicKey,
        toMint: PublicKey,
        markets: List<Market>? = null
    ): Double {
        val marketsResult = if (markets.isNullOrEmpty()) {
            loadMarkets(fromMint, toMint)
        } else {
            markets
        }

        val pairs = marketsResult.map { loadOrderbook(it) }
        if (pairs.isEmpty()) throw IllegalStateException("Could not retrieve exchange rate")

        // direct
        if (pairs.size == 1) {
            val pair = pairs.first()
            val bbo = loadBbo(pair) ?: throw IllegalStateException("Could not retrieve exchange rate")

            val market = pair.asks.market // the same market as bids

            if (market.baseMintAddress == fromMint ||
                (market.baseMintAddress.toBase58() == Token.WRAPPED_SOL_MINT &&
                    fromMint.toBase58() == Token.SOL_MINT)
            ) {
                val bestBids = bbo.bestBids
                if (bestBids != null && bestBids != 0.0) {
                    return 1 / bestBids
                }
            } else {
                if (bbo.bestOffer != null) return bbo.bestOffer!!
            }

            throw IllegalStateException("Could not retrieve exchange rate")
        }

        // transitive
        val fromBbo = loadBbo(pairs[0])
        val toBbo = loadBbo(pairs[1])

        if (fromBbo?.bestBids == null || toBbo?.bestOffer == null) throw IllegalStateException("Could not retrieve exchange rate")

        if (fromBbo.bestBids == 0.0) throw IllegalStateException("Could not retrieve exchange rate")

        return toBbo.bestOffer!!.div(fromBbo.bestBids!!)
    }

    /// Load price of current markets
    suspend fun loadFair(
        fromMint: String,
        toMint: String,
        markets: List<Market>? = null
    ): Double {

        val fromMintPublicKey = try {
            PublicKey(fromMint)
        } catch (e: Throwable) {
            throw IllegalStateException("Some public keys are not valid", e)
        }
        val toMintPublicKey = try {
            PublicKey(toMint)
        } catch (e: Throwable) {
            throw IllegalStateException("Some public keys are not valid", e)
        }

        return loadFair(fromMintPublicKey, toMintPublicKey, markets)
    }

    /// Calculate minExchangeRate needed for swap
    /// - Parameters:
    ///   - fair: fair which is gotten from loadFair(fromMint:toMint)
    ///   - slippage: user input slippage
    ///   - fromDecimals: from token decimal
    ///   - toDecimal: to token decimal
    ///   - strict: strict
    /// - Returns: ExchangeRate
    fun calculateExchangeRate(
        fair: Double,
        slippage: Double,
        fromDecimals: Int,
        toDecimal: Int,
        strict: Boolean
    ): ExchangeRate {
        var number = (10.0.pow(toDecimal.toDouble()) * FEE_MULTIPLIER) / fair
        number *= (100 - slippage)
        number /= 100
        return ExchangeRate(
            rate = BigInteger.valueOf(number.toLong()),
            fromDecimals = fromDecimals,
            quoteDecimals = toDecimal,
            strict = strict
        )
    }

    /// Executes a swap against the Serum DEX.
    /// - Returns: transaction id
    suspend fun swap(
        fromWallet: Token,
        toWallet: Token,
        amount: Double,
        slippage: Double,
        isSimulation: Boolean = false
    ): String {
        val owner = tokenKeyProvider.publicKey.toPublicKey()

        val markets = loadMarkets(
            fromMint = fromWallet.mintAddress,
            toMint = toWallet.mintAddress
        )

        var toDecimal = toWallet.decimals
        // For a direct swap, toDecimal should be zero.
        // https://github.com/project-serum/swap/blob/master/programs/swap/src/lib.rs#L696
        if (markets.size == 1) {
            toDecimal = 0
        }

        val fair = loadFair(fromWallet.mintAddress, toWallet.mintAddress, markets)
        val exchangeRate = calculateExchangeRate(
            fair = fair,
            slippage = slippage,
            fromDecimals = fromWallet.decimals,
            toDecimal = toDecimal,
            strict = false
        )

        val openOrders = markets.mapNotNull {
            openOrdersInteractor.findForMarketAndOwner(
                marketAddress = it.address,
                ownerAddress = owner
            ).firstOrNull()
        }

        val fromMint: PublicKey
        val toMint: PublicKey
        val fromWalletPubkey: PublicKey

        try {
            fromMint = PublicKey(fromWallet.mintAddress)
            toMint = PublicKey(toWallet.mintAddress)
            fromWalletPubkey = PublicKey(fromWallet.publicKey)
        } catch (e: Throwable) {
            throw IllegalStateException("Some public keys are not valid")
        }

        val fromMarket = markets.firstOrNull() ?: throw IllegalStateException("Market is not available")

        val toWalletPubkey = PublicKey(toWallet.publicKey)
        val toMarket = markets.getOrNull(1)

        val params = SwapParams(
            fromMint = fromMint,
            toMint = toMint,
            amount = amount.toBigDecimal().toLamports(fromWallet.decimals),
            minExchangeRate = exchangeRate,
            referral = null,
            fromWallet = fromWalletPubkey,
            toWallet = toWalletPubkey,
            quoteWallet = null,
            fromMarket = fromMarket,
            toMarket = toMarket,
            fromOpenOrders = openOrders.firstOrNull()?.address,
            toOpenOrders = openOrders.getOrNull(1)?.address,
            close = true
        )

        val signersAndInstructions = swap(params)

        val instructions = signersAndInstructions.map { it.instructions }.flatten()
        var signers = signersAndInstructions.map { it.signers }.flatten().toMutableList()

        // TODO: If fee relayer is available, remove account as signer
        signers.add(0, Account(owner.toByteArray()))

        // serialize transaction
        val serializedTransaction = serializationInteractor.serializeTransaction(
            instructions = instructions,
            recentBlockhash = null,
            signers = signers,
            feePayer = null // TODO: modify for fee relayer
        )

        // todo: add simulation possibility
//        if (isSimulation) {
//            swapInteractor2.sendTransaction(serializedTransaction)
//        }

        return swapInteractor2.sendTransaction(serializedTransaction)
    }

    /// Executes a swap against the Serum DEX.
    /// - Parameter params: SwapParams
    /// - Returns: Signers and instructions for creating multiple transactions
    suspend fun swap(params: SwapParams): List<SignersAndInstructions> {
        val data = swapTxs(params)
        if (!params.additionalTransactions.isNullOrEmpty()) {
            return listOf(data) + params.additionalTransactions!!
        }

        return listOf(data)
    }

    suspend fun swapTxs(params: SwapParams): SignersAndInstructions {
        // check if fromMint and toMint are equal
        if (params.fromMint.toBase58() == params.toMint.toBase58()) {
            throw IllegalStateException("Can not swap ${params.fromMint} to itself")
        }

        // If swapping to/from a USD(x) token, then swap directly on the market.
        if (params.fromMint.isUsdx()) {
            var coinWallet = params.toWallet
            var pcWallet = params.fromWallet
            var baseMint = params.toMint
            var quoteMint: PublicKey? = null
            var side = Side.BID

            // Special case USDT/USDC market since the coin is always USDT and
            // the pc is always USDC.
            if (params.toMint.toBase58() == usdcMint.toBase58()) {
                coinWallet = params.fromWallet
                pcWallet = params.toWallet
                baseMint = params.fromMint
                quoteMint = params.toMint
                side = Side.ASK
            }
            // Special case USDC/USDT market since the coin is always USDC and
            // the pc is always USDT.
            else if (params.toMint.toBase58() == usdtMint.toBase58()) {
                coinWallet = params.toWallet
                pcWallet = params.fromWallet
                baseMint = params.toMint
                quoteMint = params.fromMint
                side = Side.BID
            }

            return swapDirectTxs(
                coinWallet = coinWallet,
                pcWallet = pcWallet,
                baseMint = baseMint,
                quoteMint = quoteMint ?: params.fromMint,
                side = side,
                amount = params.amount,
                minExchangeRate = params.minExchangeRate,
                referral = params.referral,
                close = params.close,
                fromMarket = params.fromMarket,
                fromOpenOrders = params.fromOpenOrders,
                feePayer = params.feePayer,
                fromMintIsUSDx = true
            )
        } else if (params.toMint.isUsdx()) {
            return swapDirectTxs(
                coinWallet = params.fromWallet,
                pcWallet = params.toWallet,
                baseMint = params.fromMint,
                quoteMint = params.toMint,
                side = Side.ASK,
                amount = params.amount,
                minExchangeRate = params.minExchangeRate,
                referral = params.referral,
                close = params.close,
                fromMarket = params.fromMarket,
                fromOpenOrders = params.fromOpenOrders,
                feePayer = params.feePayer,
                fromMintIsUSDx = false
            )
        }

        // Direct swap market explicitly given.
        if (params.toMarket == null) {
            val side = if (params.fromMint.toBase58() == params.fromMarket.baseMintAddress.toBase58()) {
                Side.ASK
            } else {
                Side.BID
            }
            return swapDirectTxs(
                coinWallet = params.fromWallet,
                pcWallet = params.toWallet,
                baseMint = params.fromMint,
                quoteMint = params.toMint,
                side = side,
                amount = params.amount,
                minExchangeRate = params.minExchangeRate,
                referral = params.referral,
                close = params.close,
                fromMarket = params.fromMarket,
                fromOpenOrders = params.fromOpenOrders,
                feePayer = params.feePayer,
                fromMintIsUSDx = false
            )
        }

        // Neither wallet is a USD stable coin. So perform a transitive swap.

        val quoteMint = params.fromMarket.quoteMintAddress
        val toMarket = if (params.toMarket != null) params.toMarket!! else {
            throw IllegalStateException("toMarket must be provided for transitive swaps")
        }

        return swapTransitiveTxs(
            fromMint = params.fromMint,
            toMint = params.toMint,
            pcMint = quoteMint,
            fromWallet = params.fromWallet,
            toWallet = params.toWallet,
            pcWallet = params.quoteWallet,
            amount = params.amount,
            minExchangeRate = params.minExchangeRate,
            referral = params.referral,
            close = params.close,
            fromMarket = params.fromMarket,
            toMarket = toMarket,
            fromOpenOrders = params.fromOpenOrders,
            toOpenOrders = params.toOpenOrders,
            feePayer = params.feePayer
        )
    }

    suspend fun swapDirectTxs(
        coinWallet: PublicKey?,
        pcWallet: PublicKey?,
        baseMint: PublicKey,
        quoteMint: PublicKey,
        side: Side,
        amount: BigInteger,
        minExchangeRate: ExchangeRate,
        referral: PublicKey?,
        close: Boolean?,
        fromMarket: Market,
        fromOpenOrders: PublicKey?,
        feePayer: PublicKey?,
        fromMintIsUSDx: Boolean
    ): SignersAndInstructions {
        val owner = tokenKeyProvider.publicKey.toPublicKey()

        // get vaultSigner
        val vaultSigner = PublicKey.getVaultOwnerAndNonce(fromMarket.address).first

        // prepare source account, create associated token address if source wallet is native
        val sourceAccountInstructions = swapInteractor2.prepareValidAccountAndInstructions(
            myAccount = owner,
            address = coinWallet,
            mint = baseMint,
            initAmount = if (fromMintIsUSDx) BigInteger.ZERO else amount,
            feePayer = feePayer ?: owner,
            closeAfterward = baseMint.toBase58() == Token.WRAPPED_SOL_MINT
        )

        // prepare destination account, create associated token if destination wallet is native or nil.
        val destinationAccountInstructions = swapInteractor2.prepareValidAccountAndInstructions(
            myAccount = owner,
            address = pcWallet,
            mint = quoteMint,
            initAmount = BigInteger.ZERO,
            feePayer = feePayer ?: owner,
            closeAfterward = quoteMint.toBase58() == Token.WRAPPED_SOL_MINT
        )

        // get open order
        val openOrdersAccountInstructions = prepareOpenOrder(
            orders = fromOpenOrders,
            market = fromMarket,
            closeAfterward = CLOSE_ENABLED && close == true && fromOpenOrders == null
        )

        val signers = mutableListOf<Account>()
        signers += sourceAccountInstructions.signers
        signers += destinationAccountInstructions.signers
        signers += openOrdersAccountInstructions.signers

        val instructions = mutableListOf<TransactionInstruction>()
        instructions += sourceAccountInstructions.instructions
        instructions += destinationAccountInstructions.instructions
        instructions += openOrdersAccountInstructions.instructions

        val accountCoinWallet = sourceAccountInstructions.account
        val accountPcWallet = destinationAccountInstructions.account
        val openOrders = openOrdersAccountInstructions.account

        instructions.add(
            SerumSwapInstructions.directSwapInstruction(
                authority = owner,
                side = side,
                amount = amount,
                minExchangeRate = minExchangeRate,
                market = fromMarket,
                vaultSigner = vaultSigner,
                openOrders = openOrders,
                pcWallet = accountPcWallet,
                coinWallet = accountCoinWallet,
                referral = referral
            )
        )

        instructions += sourceAccountInstructions.cleanupInstructions
        instructions += destinationAccountInstructions.cleanupInstructions
        instructions += openOrdersAccountInstructions.cleanupInstructions

        return SignersAndInstructions(signers, instructions)
    }

    suspend fun swapTransitiveTxs(
        fromMint: PublicKey,
        toMint: PublicKey,
        pcMint: PublicKey,
        fromWallet: PublicKey?,
        toWallet: PublicKey?,
        pcWallet: PublicKey?,
        amount: BigInteger,
        minExchangeRate: ExchangeRate,
        referral: PublicKey?,
        close: Boolean?,
        fromMarket: Market,
        toMarket: Market,
        fromOpenOrders: PublicKey?,
        toOpenOrders: PublicKey?,
        feePayer: PublicKey?
    ): SignersAndInstructions {

        val owner = tokenKeyProvider.publicKey.toPublicKey()

        // Calculate the vault signers for each market.
        val fromVaultSigner = PublicKey.getVaultOwnerAndNonce(fromMarket.address).first
        val toVaultSigner = PublicKey.getVaultOwnerAndNonce(toMarket.address).first

        // Prepare source, destination and pc wallets
        val sourceAccountInstructions = swapInteractor2.prepareValidAccountAndInstructions(
            myAccount = owner,
            address = fromWallet,
            mint = fromMint,
            initAmount = amount,
            feePayer = feePayer ?: owner,
            closeAfterward = fromMint.toBase58() == Token.WRAPPED_SOL_MINT
        )

        val destinationAccountInstructions = swapInteractor2.prepareValidAccountAndInstructions(
            myAccount = owner,
            address = toWallet,
            mint = toMint,
            initAmount = BigInteger.ZERO,
            feePayer = feePayer ?: owner,
            closeAfterward = toMint.toBase58() == Token.WRAPPED_SOL_MINT
        )
        val pcAccountInstructions = swapInteractor2.prepareValidAccountAndInstructions(
            myAccount = owner,
            address = pcWallet,
            mint = pcMint,
            initAmount = BigInteger.ZERO,
            feePayer = feePayer ?: owner,
            closeAfterward = pcMint.toBase58() == Token.WRAPPED_SOL_MINT
        )

        // Prepare open orders
        val minBalanceForRentExemption = openOrdersInteractor.getMinimumBalanceForRentExemption(dexPID)
        val fromOpenOrdersAccountInstructions = prepareOpenOrder(
            orders = fromOpenOrders,
            market = fromMarket,
            mintRentExemption = minBalanceForRentExemption.toBigInteger(),
            closeAfterward = CLOSE_ENABLED && close == true && fromOpenOrders == null
        )
        val toOpenOrdersAccountInstructions = prepareOpenOrder(
            orders = toOpenOrders,
            market = toMarket,
            mintRentExemption = minBalanceForRentExemption.toBigInteger(),
            closeAfterward = CLOSE_ENABLED && close == true && fromOpenOrders == null
        )

        val signers = mutableListOf<Account>()
        val instructions = mutableListOf<TransactionInstruction>()

        signers += sourceAccountInstructions.signers
        signers += destinationAccountInstructions.signers
        signers += pcAccountInstructions.signers
        signers += fromOpenOrdersAccountInstructions.signers
        signers += toOpenOrdersAccountInstructions.signers

        instructions += sourceAccountInstructions.instructions
        instructions += destinationAccountInstructions.instructions
        instructions += pcAccountInstructions.instructions
        instructions += fromOpenOrdersAccountInstructions.instructions
        instructions += toOpenOrdersAccountInstructions.instructions

        instructions.add(
            SerumSwapInstructions.transitiveSwapInstruction(
                authority = owner,
                fromMarket = fromMarket,
                toMarket = toMarket,
                fromVaultSigner = fromVaultSigner,
                toVaultSigner = toVaultSigner,
                fromOpenOrder = fromOpenOrdersAccountInstructions.account,
                toOpenOrder = toOpenOrdersAccountInstructions.account,
                fromWallet = sourceAccountInstructions.account,
                toWallet = destinationAccountInstructions.account,
                amount = amount,
                minExchangeRate = minExchangeRate,
                pcWallet = pcAccountInstructions.account,
                referral = referral
            )
        )

        instructions += sourceAccountInstructions.cleanupInstructions
        instructions += destinationAccountInstructions.cleanupInstructions
        instructions += pcAccountInstructions.cleanupInstructions

        if (CLOSE_ENABLED && close == true) {
            instructions += fromOpenOrdersAccountInstructions.cleanupInstructions
            instructions += toOpenOrdersAccountInstructions.cleanupInstructions
        }

        return SignersAndInstructions(signers, instructions)
    }

    suspend fun prepareOpenOrder(
        orders: PublicKey?,
        market: Market,
        mintRentExemption: BigInteger? = null,
        closeAfterward: Boolean
    ): AccountInstructions {

        val owner = tokenKeyProvider.publicKey.toPublicKey()

        return if (orders != null) {
            val cleanupInstructions = mutableListOf<TransactionInstruction>()
            if (closeAfterward) {
                val instruction = SerumSwapInstructions.closeOrderInstruction(
                    order = orders,
                    marketAddress = market.address,
                    owner = owner,
                    destination = owner
                )

                cleanupInstructions.add(instruction)
            }

            AccountInstructions(orders, cleanupInstructions)
        } else {
            openOrdersInteractor.makeCreateAccountInstructions(
                marketAddress = market.address,
                ownerAddress = owner,
                programId = dexPID,
                minRentExemption = mintRentExemption,
                shouldInitAccount = OPEN_ENABLED,
                closeAfterward = closeAfterward
            )
        }
    }

    suspend fun route(fromMint: PublicKey, toMint: PublicKey): List<PublicKey>? =
        swapMarketInteractor.route(fromMint, toMint)

    /// Load market with current mint pair
    suspend fun loadMarkets(fromMint: String, toMint: String): List<Market> {
        val fromMintKey = try {
            PublicKey(fromMint)
        } catch (e: Throwable) {
            throw IllegalStateException("Some public keys are not valid", e)
        }

        val toMintKey = try {
            PublicKey(toMint)
        } catch (e: Throwable) {
            throw IllegalStateException("Some public keys are not valid", e)
        }

        return loadMarkets(fromMintKey, toMintKey)
    }

    /// Load market with current mint pair
    suspend fun loadMarkets(fromMint: PublicKey, toMint: PublicKey): List<Market> {
        val route = swapMarketInteractor.route(fromMint, toMint)
            ?: throw IllegalStateException("Could not retrive exchange rate")

        return route.map { loadMarket(it) }
    }

    suspend fun loadMarket(address: PublicKey): Market {
        val market = marketsCache[address]
        if (market != null) return market

        val result = swapMarketInteractor.loadMarket(address = address, programId = SerumSwapInstructions.dexPID)
        marketsCache[address] = result
        return result
    }

    /**
     * Load orderbook for current market
     * @param market market instance
     * @return OrderbookPair
     * */
    suspend fun loadOrderbook(market: Market): OrderbookPair {
        val orderbookPair = orderBooksCache[market.address]
        if (orderbookPair != null) return orderbookPair

        val bids = swapMarketInteractor.loadBids(market)
        val asks = swapMarketInteractor.loadAsks(market)

        val pair = OrderbookPair(bids, asks)
        orderBooksCache[market.address] = pair
        return pair
    }

    /**
     * Load fair price for a given market, as defined by the mid
     * @param orderbookPair asks and bids
     * @return best bids price, best asks price and middle
     * */
    fun loadBbo(orderbookPair: OrderbookPair): Bbo? {
        val bestBid = orderbookPair.bids.getList(true).firstOrNull()
        val bestOffer = orderbookPair.asks.getList().firstOrNull()

        if (bestBid == null && bestOffer == null) return null
        // todo: change bbo to big decimal fields
        return Bbo(bestBid?.price?.toDouble(), bestOffer?.price?.toDouble())
    }
}