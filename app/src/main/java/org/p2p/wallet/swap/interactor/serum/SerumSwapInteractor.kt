package org.p2p.wallet.swap.interactor.serum

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.programs.SerumSwapProgram
import org.p2p.solanaj.programs.SerumSwapProgram.dexPID
import org.p2p.solanaj.programs.SerumSwapProgram.usdcMint
import org.p2p.solanaj.programs.SerumSwapProgram.usdtMint
import org.p2p.solanaj.serumswap.Market
import org.p2p.solanaj.serumswap.model.Bbo
import org.p2p.solanaj.serumswap.model.ExchangeRate
import org.p2p.solanaj.serumswap.model.OrderbookPair
import org.p2p.solanaj.serumswap.model.Side
import org.p2p.solanaj.serumswap.model.SignersAndInstructions
import org.p2p.solanaj.serumswap.model.SwapParams
import org.p2p.solanaj.serumswap.utils.SerumSwapUtils
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.rpc.interactor.TransactionInteractor
import org.p2p.wallet.swap.interactor.SwapInstructionsInteractor
import org.p2p.wallet.utils.Constants.SOL_MINT
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.isUsdx
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toPublicKey
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.pow
import kotlin.math.roundToInt

class SerumSwapInteractor(
    private val instructionsInteractor: SwapInstructionsInteractor,
    private val openOrdersInteractor: SerumOpenOrdersInteractor,
    private val marketInteractor: SerumMarketInteractor,
    private val swapMarketInteractor: SerumSwapMarketInteractor,
    private val transactionInteractor: TransactionInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    companion object {
        // Close account feature flag.
        // TODO: enable once the DEX supports closing open orders accounts.
        private const val CLOSE_ENABLED = false

        // Initialize open orders feature flag.
        // TODO: enable once the DEX supports initializing open orders accounts.
        private const val OPEN_ENABLED = false

        const val BASE_TAKER_FEE_BPS = 0.0022
        const val FEE_MULTIPLIER = 1.0 - BASE_TAKER_FEE_BPS

        private const val DEFAULT_SERUM_DEX_FEE = 23357760L
    }

    private val orderBooksCache = mutableMapOf<PublicKey, OrderbookPair>()
    private val marketsCache = mutableMapOf<PublicKey, Market>()

    suspend fun calculateNetworkFee(
        fromWallet: Token,
        toWallet: Token,
        lamportsPerSignature: BigInteger,
        minRentExemption: BigInteger
    ): BigInteger {
        val owner = tokenKeyProvider.publicKey.toPublicKey()

        // default fee for creating serum dex account
        val creatingSerumDexFee = BigInteger.valueOf(DEFAULT_SERUM_DEX_FEE)

        // get fee for opening orders
        val markets = loadMarkets(fromWallet.mintAddress, toWallet.mintAddress)
        val openOrders = openOrdersInteractor.findForOwner(owner, dexPID)

        // calculate number of created orders
        var numberOfCreatedOrders = 0

        val fromMarket = markets.getOrNull(0)
        if (fromMarket != null && openOrders.any { it.address == fromMarket.address.toBase58() }) {
            numberOfCreatedOrders += 1
        }

        val toMarket = markets.getOrNull(1)
        if (toMarket != null && openOrders.any { it.address == toMarket.address.toBase58() }) {
            numberOfCreatedOrders += 1
        }

        // calculate number of orders that have to be created
        val numberOfOrdersToCreate = markets.size - numberOfCreatedOrders

        // fee for creating orders
        var feeForCreatingNewOrders = BigInteger.valueOf(numberOfOrdersToCreate.toLong()).multiply(
            (creatingSerumDexFee + lamportsPerSignature)
        )

        // for transitive swap: there is an lps needed for creating a separated open orders transaction
        // for direct swap: the creating orders transaction is embedded to swap instruction, so this lps is NOT needed
        if (markets.size == 2 && numberOfOrdersToCreate > 0) {
            feeForCreatingNewOrders += lamportsPerSignature
        }

        var fee = BigInteger.ZERO

        // fee for opening
        fee += feeForCreatingNewOrders

        // fee for owner's signature
        fee += lamportsPerSignature

        // if source token is native, a fee for creating wrapped SOL is needed,
        // thus a fee for new account's signature (not associated token address) is also needed
        if (fromWallet.isSOL) {
            fee += minRentExemption + lamportsPerSignature
        }

        // if destination wallet is a wrapped sol or not yet created,
        // a fee for creating it is needed, as new address is an associated token address,
        // the signature fee is NOT needed
        if (toWallet.mintAddress == WRAPPED_SOL_MINT || toWallet is Token.Other) {
            fee += minRentExemption
        }

        return fee
    }

    // Load minimum amount for trading
    suspend fun loadMinOrderSize(
        fromMint: String,
        toMint: String
    ): BigDecimal {
        val market = loadMarkets(fromMint, toMint).firstOrNull()
        return (market?.minOrderSize() ?: BigDecimal.ZERO).scaleMedium()
    }

    // todo: change double to BigDecimal
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
                (market.baseMintAddress.toBase58() == WRAPPED_SOL_MINT && fromMint.toBase58() == SOL_MINT)
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

        if (fromBbo?.bestBids == null || toBbo?.bestOffer == null) {
            throw IllegalStateException("Could not retrieve exchange rate")
        }

        if (fromBbo.bestBids == 0.0) throw IllegalStateException("Could not retrieve exchange rate")

        return toBbo.bestOffer!!.div(fromBbo.bestBids!!)
    }

    // Executes a swap against the Serum DEX.
    // - Returns: transaction id
    suspend fun swap(
        fromWallet: Token.Active,
        toWallet: Token,
        amount: BigDecimal,
        slippage: Double,
        isSimulation: Boolean = false
    ): String {
        val owner = tokenKeyProvider.publicKey.toPublicKey()

        val markets = loadMarkets(
            fromMint = fromWallet.mintAddress,
            toMint = toWallet.mintAddress
        )

        val fair = loadFair(fromWallet.mintAddress, toWallet.mintAddress, markets)

        val openOrders = openOrdersInteractor.findForOwner(owner, dexPID)

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

        val toWalletPubkey = if (toWallet is Token.Active) PublicKey(toWallet.publicKey) else null
        val toMarket = markets.getOrNull(1)

        val fromOpenOrder = openOrders.firstOrNull { it.data.market.toBase58() == fromMarket.address.toBase58() }
        val toOpenOrder = openOrders.firstOrNull { it.data.market.toBase58() == toMarket?.address?.toBase58() }

        // calculation
        var toDecimal = toWallet.decimals
        // For a direct swap, toDecimal should be zero.
        // https://github.com/project-serum/swap/blob/master/programs/swap/src/lib.rs#L696
        if (markets.size == 1) {
            toDecimal = 0
        }

        val rate = calculateExchangeRate(fair, slippage, toDecimal)

        val params = SwapParams(
            fromMint = fromMint,
            toMint = toMint,
            amount = amount.toLamports(fromWallet.decimals),
            minExchangeRate = ExchangeRate(rate, fromWallet.decimals, toDecimal, false),
            referral = null,
            fromWallet = fromWalletPubkey,
            toWallet = toWalletPubkey,
            quoteWallet = null,
            fromMarket = fromMarket,
            toMarket = toMarket,
            fromOpenOrders = fromOpenOrder?.address?.toPublicKey(),
            toOpenOrders = toOpenOrder?.address?.toPublicKey(),
            close = true
        )

        val signersAndInstructions = swap(params, isSimulation)

        val instructions = signersAndInstructions.map { it.instructions }.flatten()
        val signers = signersAndInstructions.map { it.signers }.flatten().toMutableList()

        // TODO: If fee relayer is available, remove account as signer
        signers.add(0, Account(tokenKeyProvider.secretKey))

        // serialize transaction
//        val serializedTransaction = transactionInstruction.serializeTransaction(
//            instructions = instructions,
//            recentBlockhash = null,
//            signers = signers,
//            feePayer = null // TODO: modify for fee relayer
//        )

        try {
//            val appTransaction = AppTransaction(
//                serializedTransaction = serializedTransaction,
//                sourceSymbol = fromWallet.tokenSymbol,
//                destinationSymbol = toWallet.tokenSymbol,
//                isSimulation = isSimulation
//            )
//            transactionInteractor.sendTransaction(appTransaction)
            return emptyString()
        } catch (e: Throwable) {
            throw e
            // TODO: catch error
        }
    }

    // Executes a swap against the Serum DEX.
    // - Parameter params: SwapParams
    // - Returns: Signers and instructions for creating multiple transactions
    suspend fun swap(params: SwapParams, isSimulation: Boolean): List<SignersAndInstructions> {
        val data = swapTxs(params, isSimulation)
        if (!params.additionalTransactions.isNullOrEmpty()) {
            return listOf(data) + params.additionalTransactions!!
        }

        return listOf(data)
    }

    private suspend fun swapTxs(params: SwapParams, isSimulation: Boolean): SignersAndInstructions {
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
            feePayer = params.feePayer,
            isSimulation = isSimulation
        )
    }

    private suspend fun swapDirectTxs(
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
        val vaultSigner = SerumSwapUtils.getVaultOwnerAndNonce(fromMarket.address)

        // prepare source account, create associated token address if source wallet is native
        val sourceAccountInstructions = instructionsInteractor.prepareValidAccountAndInstructions(
            myAccount = owner,
            address = coinWallet,
            mint = baseMint,
            initAmount = if (fromMintIsUSDx) BigInteger.ZERO else amount,
            feePayer = feePayer ?: owner,
            closeAfterward = baseMint.toBase58() == WRAPPED_SOL_MINT
        )

        // prepare destination account, create associated token if destination wallet is native or nil.
        val destinationAccountInstructions = instructionsInteractor.prepareValidAccountAndInstructions(
            myAccount = owner,
            address = pcWallet,
            mint = quoteMint,
            initAmount = BigInteger.ZERO,
            feePayer = feePayer ?: owner,
            closeAfterward = quoteMint.toBase58() == WRAPPED_SOL_MINT
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
            SerumSwapProgram.directSwapInstruction(
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

    private suspend fun swapTransitiveTxs(
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
        feePayer: PublicKey?,
        isSimulation: Boolean = false
    ): SignersAndInstructions {

        val owner = tokenKeyProvider.publicKey.toPublicKey()

        // Request open orders
        val (fromOpenOrdersResult, toOpenOrdersResult, openOrdersCleanupInstructions) =
            if (fromOpenOrders != null && toOpenOrders != null) {
                Triple(fromOpenOrders, toOpenOrders, emptyList())
            } else {
                createFromAndToOpenOrdersForSwapTransitive(
                    fromMarket = fromMarket,
                    toMarket = toMarket,
                    feePayer = feePayer,
                    close = close,
                    isSimulation = isSimulation
                )
            }

        // Calculate the vault signers for each market.
        val fromVaultSigner = SerumSwapUtils.getVaultOwnerAndNonce(fromMarket.address)
        val toVaultSigner = SerumSwapUtils.getVaultOwnerAndNonce(toMarket.address)

        // Prepare source, destination and pc wallets
        val sourceAccountInstructions = instructionsInteractor.prepareValidAccountAndInstructions(
            myAccount = owner,
            address = fromWallet,
            mint = fromMint,
            initAmount = amount,
            feePayer = feePayer ?: owner,
            closeAfterward = fromMint.toBase58() == WRAPPED_SOL_MINT
        )

        val destinationAccountInstructions = instructionsInteractor.prepareValidAccountAndInstructions(
            myAccount = owner,
            address = toWallet,
            mint = toMint,
            initAmount = BigInteger.ZERO,
            feePayer = feePayer ?: owner,
            closeAfterward = toMint.toBase58() == WRAPPED_SOL_MINT
        )
        val pcAccountInstructions = instructionsInteractor.prepareValidAccountAndInstructions(
            myAccount = owner,
            address = pcWallet,
            mint = pcMint,
            initAmount = BigInteger.ZERO,
            feePayer = feePayer ?: owner,
            closeAfterward = pcMint.toBase58() == WRAPPED_SOL_MINT
        )

        val signers = mutableListOf<Account>()
        val instructions = mutableListOf<TransactionInstruction>()

        signers += sourceAccountInstructions.signers
        signers += destinationAccountInstructions.signers
        signers += pcAccountInstructions.signers

        instructions += sourceAccountInstructions.instructions
        instructions += destinationAccountInstructions.instructions
        instructions += pcAccountInstructions.instructions

        instructions.add(
            SerumSwapProgram.transitiveSwapInstruction(
                authority = owner,
                fromMarket = fromMarket,
                toMarket = toMarket,
                fromVaultSigner = fromVaultSigner,
                toVaultSigner = toVaultSigner,
                fromOpenOrder = fromOpenOrdersResult,
                toOpenOrder = toOpenOrdersResult,
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
            instructions += openOrdersCleanupInstructions
        }

        return SignersAndInstructions(signers, instructions)
    }

    suspend fun loadMarket(address: PublicKey): Market {
        val market = marketsCache[address]
        if (market != null) return market

        val result = marketInteractor.loadMarket(address = address, programId = dexPID)
        marketsCache[address] = result
        return result
    }

    // / Load price of current markets
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

    /**
     * Calculates minExchangeRate needed for swap
     * @param fair which is got from loadFair(fromMint:toMint)
     * @param slippage user input slippage
     * @param toDecimal to token decimal
     * @return ExchangeRate
     * */
    fun calculateExchangeRate(
        fair: Double,
        slippage: Double,
        toDecimal: Int
    ): BigInteger {
        val number = (10.0.pow(toDecimal.toDouble()) * FEE_MULTIPLIER) / fair
        var roundedValue = number.roundToInt().toDouble()
        roundedValue *= (1 - slippage)
        return BigInteger.valueOf(roundedValue.toLong())
    }

    private suspend fun prepareOpenOrder(
        orders: PublicKey?,
        market: Market,
        minRentExemption: BigInteger? = null,
        closeAfterward: Boolean
    ): AccountInstructions {

        val owner = tokenKeyProvider.publicKey.toPublicKey()

        return if (orders != null) {
            val cleanupInstructions = mutableListOf<TransactionInstruction>()
            if (closeAfterward) {
                val instruction = SerumSwapProgram.closeOrderInstruction(
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
                minRentExemption = minRentExemption,
                shouldInitAccount = OPEN_ENABLED,
                closeAfterward = closeAfterward
            )
        }
    }

    // Create from and to open orders and wait for confirmation before transitive swaping
    suspend fun createFromAndToOpenOrdersForSwapTransitive(
        fromMarket: Market,
        toMarket: Market,
        feePayer: PublicKey?,
        close: Boolean?,
        isSimulation: Boolean
    ): Triple<PublicKey, PublicKey, List<TransactionInstruction>> {
        val minRentExemption = openOrdersInteractor.getMinimumBalanceForRentExemption(dexPID)

        val from = prepareOpenOrder(
            orders = null,
            market = fromMarket,
            minRentExemption = minRentExemption,
            closeAfterward = CLOSE_ENABLED && close == true
        )

        val to = prepareOpenOrder(
            orders = null,
            market = toMarket,
            minRentExemption = minRentExemption,
            closeAfterward = CLOSE_ENABLED && close == true
        )

        val signers = mutableListOf<Account>()
        val instructions = mutableListOf<TransactionInstruction>()

        signers += from.signers
        signers += to.signers

        instructions += from.instructions
        instructions += to.instructions

        if (feePayer == null) {
            signers.add(0, Account(tokenKeyProvider.secretKey))
        }

//        val serializedTransaction = serializationInteractor.serializeTransaction(
//            instructions = instructions,
//            recentBlockhash = null,
//            signers = signers,
//            feePayer = feePayer
//        )
//
//        val appTransaction = AppTransaction(
//            serializedTransaction = serializedTransaction,
//            sourceSymbol = from.account.toBase58().cutMiddle(),
//            destinationSymbol = to.account.toBase58().cutMiddle(),
//            isSimulation = isSimulation
//        )
//        serializationInteractor.sendTransaction(appTransaction)
        return Triple(from.account, to.account, from.cleanupInstructions + to.cleanupInstructions)
    }

    // / Load market with current mint pair
    private suspend fun loadMarkets(fromMint: String, toMint: String): List<Market> {
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

    // Load market with current mint pair
    suspend fun loadMarkets(fromMint: PublicKey, toMint: PublicKey): List<Market> {
        val route = swapMarketInteractor.route(fromMint, toMint)
            ?: throw IllegalStateException("Could not retrieve exchange rate")

        return route.map { loadMarket(it) }
    }

    /**
     * Load orderbook for current market
     * @param market market instance
     * @return OrderbookPair
     * */
    suspend fun loadOrderbook(market: Market): OrderbookPair {
        val orderbookPair = orderBooksCache[market.address]
        if (orderbookPair != null) return orderbookPair

        val bids = marketInteractor.loadBids(market)
        val asks = marketInteractor.loadAsks(market)

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
