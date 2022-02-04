package org.p2p.wallet.swap.interactor.orca

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.utils.crypto.Base64Utils
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.TokenComparator
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.model.OrcaInstructionsData
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getInputAmount
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getOutputAmount
import org.p2p.wallet.swap.model.orca.OrcaPools
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.OrcaRoute
import org.p2p.wallet.swap.model.orca.OrcaRoutes
import org.p2p.wallet.swap.model.orca.OrcaSwapInfo
import org.p2p.wallet.swap.model.orca.OrcaToken
import org.p2p.wallet.swap.model.orca.OrcaTokens
import org.p2p.wallet.swap.repository.OrcaSwapInternalRepository
import org.p2p.wallet.swap.repository.OrcaSwapRepository
import org.p2p.wallet.transaction.interactor.TransactionInteractor
import org.p2p.wallet.transaction.model.AppTransaction
import org.p2p.wallet.transaction.model.TransactionExecutionState
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger

class OrcaSwapInteractor(
    private val swapRepository: OrcaSwapRepository,
    private val rpcRepository: RpcRepository,
    private val feeRelayerRepository: FeeRelayerRepository,
    private val internalRepository: OrcaSwapInternalRepository,
    private val poolInteractor: OrcaPoolInteractor,
    private val userInteractor: UserInteractor,
    private val orcaInstructionsInteractor: OrcaInstructionsInteractor,
    private val transactionInteractor: TransactionInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    companion object {
        private const val DELAY_FIVE_SECONDS_MS = 5000L
        private const val ONE_AND_HALF_MINUTE_WAIT_IN_MS = 1000 * 60 * 1.5
    }

    private var info: OrcaSwapInfo? = null

    private var feePaymentToken: String = Token.SOL_SYMBOL

    // Prepare all needed infos for swapping
    suspend fun load() = withContext(Dispatchers.IO) {
        if (info != null) return@withContext

        val tokens = async { internalRepository.getTokens() }
        val pools = async { internalRepository.getPools() }
        val programIds = async { internalRepository.getProgramID() }

        val tokensLoaded = tokens.await()
        val poolsLoaded = pools.await()
        val routes = findAllAvailableRoutes(tokensLoaded, poolsLoaded)

        val tokenNames = mutableMapOf<String, String>()
        tokensLoaded.forEach { (key, value) -> tokenNames[value.mint] = key }

        info = OrcaSwapInfo(
            routes = routes,
            tokens = tokensLoaded,
            pools = poolsLoaded,
            programIds = programIds.await(),
            tokenNames = tokenNames
        )

        Timber.d("Orca swap info loaded")
    }

    /**
     * Finds possible destination token (symbol)
     * @param fromMint from token mint address
     * @returns list of token symbols that can be swapped to
     * */
    suspend fun findPossibleDestinations(
        fromMint: String
    ): List<Token> {
        val fromTokenName = getTokenFromMint(fromMint)?.first ?: throw IllegalStateException("Token not found")
        val routes = findRoutes(fromTokenName, null)

        val orcaTokens = routes
            .keys
            .mapNotNull { key ->
                key.split("/").find { it != fromTokenName }
            }
            .distinct()
            .mapNotNull { info?.tokens?.get(it) }

        return mapTokensForDestination(orcaTokens)
    }

    // Get all tradable pools pairs for current token pair
    // - Returns: route and parsed pools
    suspend fun getTradablePoolsPairs(
        fromMint: String,
        toMint: String
    ): List<OrcaPoolsPair> {
        val fromTokenName = getTokenFromMint(fromMint)?.first
        val toTokenName = getTokenFromMint(toMint)?.first
        val currentRoutes = findRoutes(fromTokenName, toTokenName).values.firstOrNull()

        if (fromTokenName.isNullOrEmpty() || toTokenName.isNullOrEmpty() || currentRoutes.isNullOrEmpty()) {
            return emptyList()
        }

        poolInteractor.loadBalances(currentRoutes, info?.pools)

        // retrieve all routes
        val result = currentRoutes
            .mapNotNull {
                if (it.size > 2) return@mapNotNull null // FIXME: Support more than 2 paths later
                poolInteractor.getPools(
                    infoPools = info?.pools,
                    route = it,
                    fromTokenName = fromTokenName,
                    toTokenName = toTokenName
                ) as MutableList
            }
        return result
    }

    // Find best pool to swap from input amount
    fun findBestPoolsPairForInputAmount(
        inputAmount: BigInteger,
        poolsPairs: List<OrcaPoolsPair>
    ): OrcaPoolsPair? {
        if (poolsPairs.isEmpty()) return null

        var bestPools = mutableListOf<OrcaPool>()
        var bestEstimatedAmount: BigInteger = BigInteger.ZERO

        for (pair in poolsPairs) {
            val estimatedAmount = pair.getOutputAmount(inputAmount) ?: continue
            if (estimatedAmount > bestEstimatedAmount) {
                bestEstimatedAmount = estimatedAmount
                bestPools = pair
            }
        }

        return bestPools
    }

    fun getFeePaymentToken(): String = feePaymentToken

    fun setFeePayToken(token: Token.Active) {
    }

    // Find best pool to swap from estimated amount
    fun findBestPoolsPairForEstimatedAmount(
        estimatedAmount: BigInteger,
        poolsPairs: List<OrcaPoolsPair>
    ): OrcaPoolsPair? {
        if (poolsPairs.isEmpty()) return null

        var bestPools = mutableListOf<OrcaPool>()
        var bestInputAmount: BigInteger = Int.MAX_VALUE.toBigInteger()

        for (pair in poolsPairs) {
            val inputAmount = pair.getInputAmount(estimatedAmount) ?: continue
            if (inputAmount < bestInputAmount) {
                bestInputAmount = inputAmount
                bestPools = pair
            }
        }

        return bestPools
    }

    // Get fees from current context
    // - Returns: transactions fees (fees for signatures), liquidity provider fees (fees in intermediary token?, fees in destination token)
    fun getFees(
        myWalletsMints: List<String>,
        fromWalletPubkey: String,
        toWalletPubkey: String?,
        feeRelayerFeePayerPubkey: String?,
        bestPoolsPair: OrcaPoolsPair?,
        inputAmount: BigDecimal?,
        slippage: Double,
        lamportsPerSignature: BigInteger,
        minRentExempt: BigInteger
    ): Pair<BigInteger, List<BigInteger>> {
        val owner = tokenKeyProvider.publicKey.toPublicKey()

        var transactionFees: BigInteger = BigInteger.ZERO

        val numberOfPools = BigInteger.valueOf(bestPoolsPair?.size?.toLong() ?: 0L)

        var numberOfTransactions = BigInteger.ONE

        if (numberOfPools == BigInteger.valueOf(2L)) {
            val myTokens = myWalletsMints.mapNotNull { getTokenFromMint(it) }.map { it.first }
            val intermediaryTokenName = bestPoolsPair!![0].tokenBName

            if (!myTokens.contains(intermediaryTokenName) || toWalletPubkey == null) {
                numberOfTransactions += BigInteger.ONE
            }

            if (intermediaryTokenName == Token.SOL_SYMBOL) {
                transactionFees += lamportsPerSignature
                transactionFees += minRentExempt
            }
        }

        // owner's signatures
        transactionFees += lamportsPerSignature * numberOfTransactions

        if (feeRelayerFeePayerPubkey == null) {
            // userAuthority's signatures
            transactionFees += lamportsPerSignature * numberOfPools
        } else {
            throw IllegalStateException("feeRelayer is being implemented")
        }

        // when swap from/to native SOL, a fee for creating it is needed
        if (fromWalletPubkey == owner.toBase58() || bestPoolsPair!!.last().tokenBName == Token.SOL_SYMBOL) {
            transactionFees += lamportsPerSignature
            transactionFees += minRentExempt
        }

        if (toWalletPubkey.isNullOrEmpty()) {
            transactionFees += lamportsPerSignature
            transactionFees += minRentExempt
        }

        val liquidityProviderFees = if (inputAmount != null) {
            poolInteractor.calculateLiquidityProviderFees(
                poolsPair = bestPoolsPair!!,
                inputAmount = inputAmount,
                slippage = slippage
            )
        } else emptyList()

        return transactionFees to liquidityProviderFees
    }

    // Execute swap
    suspend fun swap(
        fromWalletSymbol: String,
        toWalletSymbol: String,
        fromWalletPubkey: String,
        toWalletPubkey: String?,
        bestPoolsPair: OrcaPoolsPair,
        amount: Double,
        slippage: Double,
        isSimulation: Boolean
    ): TransactionExecutionState {
        val owner = Account(tokenKeyProvider.secretKey)

        if (info == null || bestPoolsPair.isEmpty()) {
            throw IllegalStateException("Swap info missing or best pools pair is empty")
        }
        val info = info!!

        val fromDecimals = bestPoolsPair[0].tokenABalance?.decimals
            ?: throw IllegalStateException("Invalid pool")

        val lamports = BigDecimal(amount).toLamports(fromDecimals)

        val feeRelayerFeePayer: PublicKey? = null // TODO: - Fee relayer

        return if (bestPoolsPair.size == 1) {
            swapDirect(
                fromWalletSymbol = fromWalletSymbol,
                toWalletSymbol = toWalletSymbol,
                pool = bestPoolsPair.first(),
                fromTokenPubkey = fromWalletPubkey,
                toTokenPubkey = toWalletPubkey,
                amount = lamports,
                slippage = slippage,
                feeRelayerFeePayer = feeRelayerFeePayer,
                isSimulation = isSimulation
            )
        } else {
            swapTransitive(
                fromWalletSymbol = fromWalletSymbol,
                toWalletSymbol = toWalletSymbol,
                bestPoolsPair = bestPoolsPair,
                toWalletPubkey = toWalletPubkey,
                feeRelayerFeePayer = feeRelayerFeePayer,
                info = info,
                owner = owner,
                fromWalletPubkey = fromWalletPubkey,
                lamports = lamports,
                slippage = slippage,
                isSimulation = isSimulation
            )
        }
    }

    private suspend fun swapDirect(
        fromWalletSymbol: String,
        toWalletSymbol: String,
        pool: OrcaPool,
        fromTokenPubkey: String,
        toTokenPubkey: String?,
        amount: BigInteger,
        slippage: Double,
        feeRelayerFeePayer: PublicKey?,
        isSimulation: Boolean
    ): TransactionExecutionState {
        val owner = Account(tokenKeyProvider.secretKey)
//        val info = info ?: return OrcaSwapResult.InvalidInfoOrPair
        val info = info
            ?: return TransactionExecutionState.Failed("", IllegalStateException("Invalid pool pair"))

        val accountInstructions = poolInteractor.constructExchange(
            pool = pool,
            tokens = info.tokens,
            owner = owner,
            fromTokenPubkey = fromTokenPubkey,
            toTokenPubkey = toTokenPubkey,
            amount = amount,
            slippage = slippage,
            feeRelayerFeePayer = feeRelayerFeePayer,
            shouldCreateAssociatedTokenAccount = true
        )

        if (feeRelayerFeePayer != null) {
            throw IllegalStateException("Fee relayer is implementing")
        } else {
            val feePayerPublicKey = feeRelayerRepository.getFeePayerPublicKey()

            // serialize transaction
            val transaction = Transaction()
            val instructions = accountInstructions.instructions + accountInstructions.cleanupInstructions
            transaction.addInstructions(instructions)
            transaction.setFeePayer(feePayerPublicKey)

            val blockhash = rpcRepository.getRecentBlockhash().recentBlockhash
            transaction.setRecentBlockHash(blockhash)
            val signers = listOf(owner) + accountInstructions.signers
            transaction.sign(signers)

            val signature = transaction.signature

            val transactionId = feeRelayerRepository.relayTransaction(
                instructions = instructions,
                signatures = transaction.allSignatures,
                pubkeys = transaction.accountKeys,
                blockHash = blockhash
            ).firstOrNull().orEmpty()

//            return OrcaSwapResult.Executing(transactionId)
            return TransactionExecutionState.Finished(transactionId, signature.signature)
        }
    }

    private suspend fun swapTransitive(
        fromWalletSymbol: String,
        toWalletSymbol: String,
        bestPoolsPair: OrcaPoolsPair,
        toWalletPubkey: String?,
        feeRelayerFeePayer: PublicKey?,
        info: OrcaSwapInfo,
        owner: Account,
        fromWalletPubkey: String,
        lamports: BigInteger,
        slippage: Double,
        isSimulation: Boolean
    ): TransactionExecutionState =
        if (toWalletPubkey != null) {
            swapTransitiveSingle(
                fromWalletSymbol = fromWalletSymbol,
                toWalletSymbol = toWalletSymbol,
                bestPoolsPair = bestPoolsPair,
                toWalletPubkey = toWalletPubkey,
                feeRelayerFeePayer = feeRelayerFeePayer,
                info = info,
                owner = owner,
                fromWalletPubkey = fromWalletPubkey,
                lamports = lamports,
                slippage = slippage,
                isSimulation = isSimulation
            )
        } else {
            swapTransitiveSeparated(
                fromWalletSymbol = fromWalletSymbol,
                toWalletSymbol = toWalletSymbol,
                bestPoolsPair = bestPoolsPair,
                toWalletPubkey = toWalletPubkey,
                feeRelayerFeePayer = feeRelayerFeePayer,
                info = info,
                owner = owner,
                fromWalletPubkey = fromWalletPubkey,
                lamports = lamports,
                slippage = slippage,
                isSimulation = isSimulation
            )
        }

    private suspend fun swapTransitiveSeparated(
        fromWalletSymbol: String,
        toWalletSymbol: String,
        bestPoolsPair: OrcaPoolsPair,
        toWalletPubkey: String?,
        feeRelayerFeePayer: PublicKey?,
        info: OrcaSwapInfo,
        owner: Account,
        fromWalletPubkey: String,
        lamports: BigInteger,
        slippage: Double,
        isSimulation: Boolean
    ): TransactionExecutionState {
        val pool0 = bestPoolsPair[0]
        val pool1 = bestPoolsPair[1]

        // TO AVOID `TRANSACTION IS TOO LARGE` ERROR, WE SPLIT OPERATION INTO 2 TRANSACTIONS
        // FIRST TRANSACTION IS TO CREATE ASSOCIATED TOKEN ADDRESS FOR INTERMEDIARY TOKEN (IF NOT YET CREATED) AND WAIT FOR CONFIRMATION
        // SECOND TRANSACTION TAKE THE RESULT OF FIRST TRANSACTION (ADDRESSES) TO REDUCE ITS SIZE

        // FIRST TRANSACTION

        var createTransactionConfirmed = false

        var waitingTimeInMs = 0L

        val (intermediary, destination) = constructAndWaitIntermediaryAccount(
            pool0 = pool0,
            pool1 = pool1,
            toWalletPubkey = toWalletPubkey,
            feeRelayerFeePayer = feeRelayerFeePayer,
            onConfirmed = { createTransactionConfirmed = true }
        )

        while (!createTransactionConfirmed) {
            if (waitingTimeInMs > ONE_AND_HALF_MINUTE_WAIT_IN_MS) {
                createTransactionConfirmed = true
                break
            }

            Timber
                .tag("TransitiveSwap")
                .d("Account creation is not confirmed, will check in 5 seconds, waiting time: $waitingTimeInMs")

            waitingTimeInMs += DELAY_FIVE_SECONDS_MS

            /* Checking transaction is confirmed every 5 seconds */
            delay(DELAY_FIVE_SECONDS_MS)
        }

        Timber.tag("TransitiveSwap").d("Account creation is confirmed, going forward")

        val pool0AccountInstructions = poolInteractor.constructExchange(
            pool = pool0,
            tokens = info.tokens,
            owner = owner,
            fromTokenPubkey = fromWalletPubkey,
            toTokenPubkey = intermediary.account.toBase58(),
            amount = lamports,
            slippage = slippage,
            feeRelayerFeePayer = feeRelayerFeePayer,
            shouldCreateAssociatedTokenAccount = false
        )

        val minOutAmount = pool0.getMinimumAmountOut(lamports, slippage)
            ?: throw IllegalStateException("Couldn't calculate min amount out")

        val pool1AccountInstructions = poolInteractor.constructExchange(
            pool = pool1,
            tokens = info.tokens,
            owner = owner,
            fromTokenPubkey = intermediary.account.toBase58(),
            toTokenPubkey = destination.account.toBase58(),
            amount = minOutAmount,
            slippage = slippage,
            feeRelayerFeePayer = feeRelayerFeePayer,
            shouldCreateAssociatedTokenAccount = false
        )

        val instructions =
            pool0AccountInstructions.instructions + pool1AccountInstructions.instructions

        val cleanupInstructions =
            pool0AccountInstructions.cleanupInstructions + pool1AccountInstructions.cleanupInstructions

        val accountInstructions = AccountInstructions(
            account = pool1AccountInstructions.account,
            instructions = instructions as MutableList<TransactionInstruction>,
            cleanupInstructions = cleanupInstructions,
            signers = listOf(owner) + pool0AccountInstructions.signers + pool1AccountInstructions.signers
        )

        if (feeRelayerFeePayer != null) {
            throw IllegalStateException("Fee relayer is implementing")
        } else {
            val transaction = Transaction()
            transaction.addInstructions(accountInstructions.instructions)
            transaction.addInstructions(accountInstructions.cleanupInstructions)

            val feePayerPublicKey = feeRelayerRepository.getFeePayerPublicKey()
            transaction.setFeePayer(feePayerPublicKey)
            val recentBlockhash = rpcRepository.getRecentBlockhash()
            transaction.setRecentBlockHash(recentBlockhash.recentBlockhash)
            transaction.sign(accountInstructions.signers)

            val serializedMessage = transaction.serialize()
            val serializedTransaction = Base64Utils.encode(serializedMessage)

            val appTransaction = AppTransaction(
                serializedTransaction = serializedTransaction,
                sourceSymbol = fromWalletSymbol,
                destinationSymbol = toWalletSymbol,
                isSimulation = isSimulation
            )

            val signature = transaction.signature

            val transactionId = feeRelayerRepository.relayTransaction(
                instructions = instructions,
                signatures = transaction.allSignatures,
                pubkeys = transaction.accountKeys,
                blockHash = recentBlockhash.recentBlockhash
            ).firstOrNull().orEmpty()

//            return OrcaSwapResult.Executing(transactionId)
            return TransactionExecutionState.Finished(transactionId, signature.signature)
        }
    }

    private suspend fun swapTransitiveSingle(
        fromWalletSymbol: String,
        toWalletSymbol: String,
        bestPoolsPair: OrcaPoolsPair,
        toWalletPubkey: String?,
        feeRelayerFeePayer: PublicKey?,
        info: OrcaSwapInfo,
        owner: Account,
        fromWalletPubkey: String,
        lamports: BigInteger,
        slippage: Double,
        isSimulation: Boolean
    ): TransactionExecutionState {
        val pool0 = bestPoolsPair[0]
        val pool1 = bestPoolsPair[1]

        val (intermediary, destination) = constructIntermediaryAccountInstructions(
            pool0 = pool0,
            pool1 = pool1,
            toWalletPubkey = toWalletPubkey,
            feeRelayerFeePayer = feeRelayerFeePayer
        )

        Timber.tag("TransitiveSwap").d("Account creation instructions are created")

        val pool0AccountInstructions = poolInteractor.constructExchange(
            pool = pool0,
            tokens = info.tokens,
            owner = owner,
            fromTokenPubkey = fromWalletPubkey,
            toTokenPubkey = intermediary.account.toBase58(),
            amount = lamports,
            slippage = slippage,
            feeRelayerFeePayer = feeRelayerFeePayer,
            shouldCreateAssociatedTokenAccount = false
        )

        val minOutAmount = pool0.getMinimumAmountOut(lamports, slippage)
            ?: throw IllegalStateException("Couldn't calculate min amount out")

        val pool1AccountInstructions = poolInteractor.constructExchange(
            pool = pool1,
            tokens = info.tokens,
            owner = owner,
            fromTokenPubkey = intermediary.account.toBase58(),
            toTokenPubkey = destination.account.toBase58(),
            amount = minOutAmount,
            slippage = slippage,
            feeRelayerFeePayer = feeRelayerFeePayer,
            shouldCreateAssociatedTokenAccount = false
        )

        val instructions = mutableListOf<TransactionInstruction>()
        instructions += intermediary.instructions
        instructions += destination.instructions
        instructions += pool0AccountInstructions.instructions
        instructions += pool1AccountInstructions.instructions

        val cleanupInstructions = mutableListOf<TransactionInstruction>()
        cleanupInstructions += intermediary.closeInstructions
        cleanupInstructions += destination.closeInstructions
        cleanupInstructions += pool0AccountInstructions.cleanupInstructions
        cleanupInstructions += pool1AccountInstructions.cleanupInstructions

        val accountInstructions = AccountInstructions(
            account = pool1AccountInstructions.account,
            instructions = instructions,
            cleanupInstructions = cleanupInstructions,
            signers = listOf(owner) + pool0AccountInstructions.signers + pool1AccountInstructions.signers
        )

        if (feeRelayerFeePayer != null) {
            throw IllegalStateException("Fee relayer is implementing")
        } else {
            val transaction = Transaction()
            transaction.addInstructions(accountInstructions.instructions)
            transaction.addInstructions(accountInstructions.cleanupInstructions)

            val feePayer = feeRelayerRepository.getFeePayerPublicKey()
            transaction.setFeePayer(feePayer)
            val recentBlockhash = rpcRepository.getRecentBlockhash()
            transaction.setRecentBlockHash(recentBlockhash.recentBlockhash)
            transaction.sign(accountInstructions.signers)

            val serializedMessage = transaction.serialize()
            val serializedTransaction = Base64Utils.encode(serializedMessage)

            val appTransaction = AppTransaction(
                serializedTransaction = serializedTransaction,
                sourceSymbol = fromWalletSymbol,
                destinationSymbol = toWalletSymbol,
                isSimulation = isSimulation
            )

            val signature = transaction.signature

            val transactionId = feeRelayerRepository.relayTransaction(
                instructions = instructions,
                signatures = transaction.allSignatures,
                pubkeys = transaction.accountKeys,
                blockHash = recentBlockhash.recentBlockhash
            ).firstOrNull().orEmpty()
//            return OrcaSwapResult.Executing(serializedTransaction)
            return TransactionExecutionState.Finished(transactionId, signature.signature)
        }
    }

    // / Find routes for from and to token name, aka symbol
    private fun findRoutes(
        fromTokenName: String?,
        toTokenName: String?
    ): OrcaRoutes {
        val info = info ?: throw IllegalStateException("Swap info missing")
        // if fromToken isn't selected
        if (fromTokenName.isNullOrEmpty()) return mutableMapOf()

        // if toToken isn't selected
        if (toTokenName == null) {
            // get all routes that have token A
            return info.routes.filter { it.key.split("/").contains(fromTokenName) } as MutableMap
        }

        // get routes with fromToken and toToken
        val pair = listOf(fromTokenName, toTokenName)
        val validRoutesNames = listOf(
            pair.joinToString("/"),
            pair.reversed().joinToString("/")
        )
        return info.routes.filter { validRoutesNames.contains(it.key) } as MutableMap
    }

    /**
     * For test purposes only
     * */
    fun getSwapInfo(): OrcaSwapInfo? = info

    private suspend fun constructIntermediaryAccountInstructions(
        pool0: OrcaPool,
        pool1: OrcaPool,
        toWalletPubkey: String?,
        feeRelayerFeePayer: PublicKey?
    ): List<OrcaInstructionsData> {
        val owner = tokenKeyProvider.publicKey.toPublicKey()
        val intermediaryTokenMint = info?.tokens?.get(pool0.tokenBName)?.mint?.toPublicKey()
        val destinationMint = info?.tokens?.get(pool1.tokenBName)?.mint?.toPublicKey()

        if (intermediaryTokenMint == null || destinationMint == null) {
            throw IllegalStateException("Pool mints not found")
        }

        /* building instructions for intermediary token */
        val intermediaryData = orcaInstructionsInteractor.buildDestinationInstructions(
            owner = owner,
            destination = null,
            destinationMint = intermediaryTokenMint,
            feePayer = feeRelayerFeePayer ?: owner,
            closeAfterward = true // todo:
        )

        /* building instructions for destination token */
        val destinationData = orcaInstructionsInteractor.buildDestinationInstructions(
            owner = owner,
            destination = toWalletPubkey?.toPublicKey(),
            destinationMint = destinationMint,
            feePayer = feeRelayerFeePayer ?: owner,
            closeAfterward = false // todo:
        )

        return listOf(intermediaryData, destinationData)
    }

    /**
     * Creating intermediary and destination addresses in a separate transaction if needed,
     * to avoid TransactionTooLargeException
     * */
    private suspend fun constructAndWaitIntermediaryAccount(
        pool0: OrcaPool,
        pool1: OrcaPool,
        toWalletPubkey: String?,
        feeRelayerFeePayer: PublicKey?,
        onConfirmed: () -> Unit
    ): List<OrcaInstructionsData> {

        val owner = tokenKeyProvider.publicKey.toPublicKey()
        val intermediaryTokenMint = info?.tokens?.get(pool0.tokenBName)?.mint?.toPublicKey()
        val destinationMint = info?.tokens?.get(pool1.tokenBName)?.mint?.toPublicKey()

        if (intermediaryTokenMint == null || destinationMint == null) {
            throw IllegalStateException("Pool mints not found")
        }
        val transaction = Transaction()

        /* building instructions for intermediary token */
        val intermediaryData = orcaInstructionsInteractor.buildDestinationInstructions(
            owner = owner,
            destination = null,
            destinationMint = intermediaryTokenMint,
            feePayer = feeRelayerFeePayer ?: owner,
            closeAfterward = true // todo:
        )

        intermediaryData.instructions.forEach { transaction.addInstruction(it) }

        /* building instructions for destination token */
        val destinationData = orcaInstructionsInteractor.buildDestinationInstructions(
            owner = owner,
            destination = toWalletPubkey?.toPublicKey(),
            destinationMint = destinationMint,
            feePayer = feeRelayerFeePayer ?: owner,
            closeAfterward = false // todo:
        )

        destinationData.instructions.forEach { transaction.addInstruction(it) }

        // if token address has already been created, then no need to send any transactions
        if (intermediaryData.instructions.isEmpty() && destinationData.instructions.isEmpty()) {
            onConfirmed()
            return listOf(intermediaryData, destinationData)
        }

        // if creating transaction is needed
        val feePayerPublicKey = feeRelayerFeePayer ?: owner
        transaction.setFeePayer(feePayerPublicKey)

        val blockhash = rpcRepository.getRecentBlockhash().recentBlockhash
        transaction.setRecentBlockHash(blockhash)

        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.sign(signers)

        val serializedMessage = transaction.serialize()
        val serializedTransaction = Base64Utils.encode(serializedMessage)

        /**
         * [swapRepository.sendAndWait] sends transaction and connects to the socket client
         * where transaction confirmation status is awaiting.
         * Once it came, [transactionInteractor] callback triggers and we proceed
         * */
        transactionInteractor.onSignatureReceived = {
            onConfirmed()
        }

        swapRepository.sendAndWait(serializedTransaction)

        return listOf(intermediaryData, destinationData)
    }

    private fun findAllAvailableRoutes(tokens: OrcaTokens, pools: OrcaPools): OrcaRoutes {
        val filteredTokens = tokens.filter { it.value.poolToken != true }.map { it.key }
        val pairs = getPairs(filteredTokens)
        return getAllRoutes(pairs, pools)
    }

    private fun getPairs(tokens: List<String>): List<List<String>> {
        val pairs: MutableList<List<String>> = mutableListOf()

        if (tokens.isEmpty()) return pairs

        for (i in 0 until tokens.size - 1) {
            for (j in i + 1 until tokens.size) {
                val tokenA = tokens[i]
                val tokenB = tokens[j]

                pairs.add(orderTokenPair(tokenA, tokenB))
            }
        }

        return pairs
    }

    // / Map mint to token info
    private fun getTokenFromMint(mint: String): Pair<String, OrcaToken>? {
        val key = info?.tokens?.filterValues { it.mint == mint }?.keys?.firstOrNull() ?: return null
        val tokenInfo = info!!.tokens[key] ?: return null
        return key to tokenInfo
    }

    private fun getAllRoutes(pairs: List<List<String>>, pools: OrcaPools): OrcaRoutes {
        val routes: OrcaRoutes = mutableMapOf()
        pairs.forEach { pair ->
            val tokenA = pair.firstOrNull()
            val tokenB = pair.lastOrNull()

            if (tokenA.isNullOrEmpty() || tokenB.isNullOrEmpty()) return@forEach

            routes[getTradeId(tokenA, tokenB)] = getRoutes(tokenA, tokenB, pools)
        }
        return routes
    }

    private fun getTradeId(tokenX: String, tokenY: String): String =
        orderTokenPair(tokenX, tokenY).joinToString("/")

    private fun orderTokenPair(tokenX: String, tokenY: String): List<String> {
        return if (tokenX == "USDC" && tokenY == "USDT") {
            listOf(tokenX, tokenY)
        } else if (tokenY == "USDC" && tokenX == "USDT") {
            listOf(tokenY, tokenX)
        } else if (tokenY == "USDC" || tokenY == "USDT") {
            listOf(tokenX, tokenY)
        } else if (tokenX == "USDC" || tokenX == "USDT") {
            listOf(tokenY, tokenX)
        } else if (tokenX < tokenY) {
            listOf(tokenX, tokenY)
        } else {
            listOf(tokenY, tokenX)
        }
    }

    private fun getRoutes(tokenA: String, tokenB: String, pools: OrcaPools): List<OrcaRoute> {
        val routes = mutableListOf<OrcaRoute>()

        // Find all pools that contain the same tokens.
        // Checking tokenAName and tokenBName will find Stable pools.
        pools.forEach { (poolId, poolConfig) ->
            if ((poolConfig.tokenAName == tokenA && poolConfig.tokenBName == tokenB) ||
                (poolConfig.tokenAName == tokenB && poolConfig.tokenBName == tokenA)
            ) {
                routes.add(mutableListOf(poolId))
            }
        }

        // Find all pools that contain the first token but not the second
        val filteredPools = pools
            .filter {
                (it.value.tokenAName == tokenA && it.value.tokenBName != tokenB) ||
                    (it.value.tokenBName == tokenA && it.value.tokenAName != tokenB)
            }

        val firstLegPools = mutableMapOf<String, String>()

        filteredPools.forEach { pool ->
            firstLegPools[pool.key] = if (pool.value.tokenBName == tokenA) {
                pool.value.tokenAName
            } else {
                pool.value.tokenBName
            }
        }

        // Find all routes that can include firstLegPool and a second pool.
        firstLegPools.forEach { (firstLegPoolId, intermediateTokenName) ->
            pools.forEach { (secondLegPoolId, poolConfig) ->
                if ((poolConfig.tokenAName == intermediateTokenName && poolConfig.tokenBName == tokenB) ||
                    (poolConfig.tokenBName == intermediateTokenName && poolConfig.tokenAName == tokenB)
                ) {
                    routes.add(listOf(firstLegPoolId, secondLegPoolId))
                }
            }
        }

        return routes
    }

    private suspend fun mapTokensForDestination(orcaTokens: List<OrcaToken>): List<Token> {
        val userTokens = userInteractor.getUserTokens()
        val publicKey = tokenKeyProvider.publicKey
        val allTokens = orcaTokens
            .mapNotNull { orcaToken ->
                val userToken = userTokens.find { it.mintAddress == orcaToken.mint }
                return@mapNotNull when {
                    userToken != null ->
                        if (userToken.isSOL && userToken.publicKey != publicKey) null else userToken
                    else ->
                        userInteractor.findTokenData(orcaToken.mint)
                }
            }
            .sortedWith(TokenComparator())

        return allTokens
    }
}