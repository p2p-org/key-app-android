package com.p2p.wallet.swap.interactor.orca

import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.swap.model.orca.OrcaInstructionsData
import com.p2p.wallet.swap.model.orca.OrcaPool
import com.p2p.wallet.swap.model.orca.OrcaPool.Companion.getInputAmount
import com.p2p.wallet.swap.model.orca.OrcaPool.Companion.getOutputAmount
import com.p2p.wallet.swap.model.orca.OrcaPools
import com.p2p.wallet.swap.model.orca.OrcaPoolsPair
import com.p2p.wallet.swap.model.orca.OrcaRoute
import com.p2p.wallet.swap.model.orca.OrcaRoutes
import com.p2p.wallet.swap.model.orca.OrcaSwapInfo
import com.p2p.wallet.swap.model.orca.OrcaSwapResult
import com.p2p.wallet.swap.model.orca.OrcaToken
import com.p2p.wallet.swap.model.orca.OrcaTokens
import com.p2p.wallet.swap.repository.OrcaSwapInternalRepository
import com.p2p.wallet.swap.repository.OrcaSwapRepository
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.toLamports
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.programs.TokenSwapProgram
import org.p2p.solanaj.utils.crypto.Base64Utils
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger

class OrcaSwapInteractor2(
    private val swapRepository: OrcaSwapRepository,
    private val rpcRepository: RpcRepository,
    private val internalRepository: OrcaSwapInternalRepository,
    private val poolInteractor: OrcaPoolInteractor,
    private val userInteractor: UserInteractor,
    private val orcaInstructionsInteractor: OrcaInstructionsInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    private var info: OrcaSwapInfo? = null

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

    // Find possible destination token (symbol)
    // - Parameter fromMint: from token mint address
    // - Returns: List of token symbols that can be swapped to
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

        // retrieve all routes
        val result = currentRoutes.mapNotNull {
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

    // / Find best pool to swap from input amount
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

    // Find best pool to swap from estimated amount
    fun findBestPoolsPairForEstimatedAmount(
        estimatedAmount: BigInteger,
        poolsPairs: List<OrcaPoolsPair>
    ): OrcaPoolsPair? {
        if (poolsPairs.isEmpty()) return null

        var bestPools = mutableListOf<OrcaPool>()
        var bestInputAmount: BigInteger = BigInteger.TEN // FIXME: find .max value

        for (pair in poolsPairs) {
            val inputAmount = pair.getInputAmount(estimatedAmount) ?: continue
            if (inputAmount < bestInputAmount) {
                bestInputAmount = inputAmount
                bestPools = pair
            }
        }

        return bestPools
    }

    fun calculateFees(
        availablePoolsPairs: List<OrcaPoolsPair>,
        bestPoolsPair: OrcaPoolsPair?
    ): BigInteger? {
        // TODO: - Later
//        let poolsPair = bestPoolsPair ?? availablePoolsPairs.first
        return null
    }

    // / Execute swap
    suspend fun swap(
        fromWalletPubkey: String,
        toWalletPubkey: String?,
        bestPoolsPair: OrcaPoolsPair,
        amount: Double,
        slippage: Double,
        isSimulation: Boolean
    ): OrcaSwapResult {
        val owner = Account(tokenKeyProvider.secretKey)

        if (info == null || bestPoolsPair.isEmpty()) {
            throw IllegalStateException("Swap info missing or best pools pair is empty")
        }
        val info = info!!

        val fromDecimals = bestPoolsPair[0].tokenABalance?.decimals
            ?: throw IllegalStateException("Invalid pool")

        val lamports = BigDecimal(amount).toLamports(fromDecimals)

        var feeRelayerFeePayer: PublicKey? = null // TODO: - Fee relayer

        if (bestPoolsPair.size == 1) {
            return swapDirect(
                pool = bestPoolsPair.first(),
                tokens = info.tokens,
                owner = owner,
                lamports = lamports,
                feeRelayerFeePayer = feeRelayerFeePayer,
                slippage = slippage,
                isSimulation = isSimulation
            )
        } else {
            val pool0 = bestPoolsPair[0]
            val pool1 = bestPoolsPair[1]

            // TO AVOID `TRANSACTION IS TOO LARGE` ERROR, WE SPLIT OPERATION INTO 2 TRANSACTIONS
            // FIRST TRANSACTION IS TO CREATE ASSOCIATED TOKEN ADDRESS FOR INTERMEDIARY TOKEN (IF NOT YET CREATED) AND WAIT FOR CONFIRMATION
            // SECOND TRANSACTION TAKE THE RESULT OF FIRST TRANSACTION (ADDRESSES) TO REDUCE ITS SIZE

            // FIRST TRANSACTION

            // todo: should be default owner?
            val signers = mutableListOf(owner)
            val transaction = Transaction()

            val (intermediary, destination) = createIntermediaryTokenAndDestinationTokenAddressIfNeeded(
                pool0 = pool0,
                pool1 = pool1,
                toWalletPubkey = toWalletPubkey,
                feeRelayerFeePayer = feeRelayerFeePayer
            )

            buildSwapTransaction(
                transaction = transaction,
                signers = signers,
                tokens = info.tokens,
                pool = pool0,
                owner = owner,
                source = fromWalletPubkey.toPublicKey(),
                destination = intermediary,
                lamports = lamports,
                feeRelayerFeePayer = feeRelayerFeePayer ?: owner.publicKey,
                slippage = slippage,
                shouldCreateAssociatedAddress = false
            )

            val finalAmount = pool0.getMinimumAmountOut(lamports, slippage)
                ?: throw IllegalStateException("Min amount is null, make sure inputs are correct")

            buildSwapTransaction(
                transaction = transaction,
                signers = signers,
                tokens = info.tokens,
                pool = pool0,
                owner = owner,
                source = intermediary,
                destination = destination,
                lamports = finalAmount,
                feeRelayerFeePayer = feeRelayerFeePayer ?: owner.publicKey,
                slippage = slippage,
                shouldCreateAssociatedAddress = false
            )

            val recentBlockhash = rpcRepository.getRecentBlockhash()
            transaction.setRecentBlockHash(recentBlockhash.recentBlockhash)
            transaction.sign(signers)
            val signature = if (isSimulation) {
                rpcRepository.simulateTransaction(transaction)
            } else {
                rpcRepository.sendTransaction(transaction)
            }
            return OrcaSwapResult.Success(signature)
        }
    }

    private suspend fun swapDirect(
        pool: OrcaPool,
        tokens: OrcaTokens,
        owner: Account,
        lamports: BigInteger,
        feeRelayerFeePayer: PublicKey?,
        slippage: Double,
        isSimulation: Boolean
    ): OrcaSwapResult {
        // todo: should be default owner?
        val signers = mutableListOf(owner)
        val transaction = Transaction()

        buildSwapTransaction(
            transaction = transaction,
            signers = signers,
            tokens = tokens,
            pool = pool,
            owner = owner,
            source = owner.publicKey,
            destination = null,
            lamports = lamports,
            feeRelayerFeePayer = feeRelayerFeePayer ?: owner.publicKey,
            slippage = slippage,
            shouldCreateAssociatedAddress = true
        )

        val recentBlockhash = rpcRepository.getRecentBlockhash()
        transaction.setRecentBlockHash(recentBlockhash.recentBlockhash)
        transaction.sign(signers)
        val signature = if (isSimulation) {
            rpcRepository.simulateTransaction(transaction)
        } else {
            rpcRepository.sendTransaction(transaction)
        }
        return OrcaSwapResult.Success(signature)
    }

    private suspend fun buildSwapTransaction(
        transaction: Transaction,
        signers: MutableList<Account>,
        tokens: OrcaTokens,
        pool: OrcaPool,
        owner: Account,
        source: PublicKey,
        destination: PublicKey?,
        lamports: BigInteger,
        feeRelayerFeePayer: PublicKey,
        slippage: Double,
        shouldCreateAssociatedAddress: Boolean,
    ) {
        val fromMint = tokens[pool.tokenAName]?.mint?.toPublicKey()
        val toMint = tokens[pool.tokenBName]?.mint?.toPublicKey()

        if (fromMint == null || toMint == null) {
            throw IllegalStateException("Pool mints not found")
        }

        val sourceData = orcaInstructionsInteractor.buildSourceInstructions(
            source = source,
            pool = pool,
            amount = lamports,
            sourceMint = fromMint,
            destinationMint = toMint,
            feePayer = feeRelayerFeePayer
        )

        sourceData.instructions.forEach { transaction.addInstruction(it) }
        signers += sourceData.signers

        val destinationData = if (destination != null && !shouldCreateAssociatedAddress) {
            OrcaInstructionsData(destination)
        } else {
            orcaInstructionsInteractor.buildDestinationInstructions(
                owner = owner.publicKey,
                destination = destination,
                destinationMint = toMint,
                feePayer = feeRelayerFeePayer,
                closeAfterward = false // TODO: check later
            )
        }

        destinationData.instructions.forEach { transaction.addInstruction(it) }
        signers += destinationData.signers

        val userTransferAuthority = Account()
        val approve = TokenProgram.approveInstruction(
            TokenProgram.PROGRAM_ID,
            sourceData.account,
            userTransferAuthority.publicKey,
            owner.publicKey,
            lamports
        )

        val minAmountOut = pool.getMinimumAmountOut(lamports, slippage)
            ?: throw IllegalStateException("Couldn't estimate minimum out amount")

        val swap = TokenSwapProgram.swapInstruction(
            pool.account,
            pool.authority,
            userTransferAuthority.publicKey,
            sourceData.account,
            pool.tokenAccountA,
            pool.tokenAccountB,
            destinationData.account,
            pool.poolTokenMint,
            pool.feeAccount,
            pool.feeAccount,
            TokenProgram.PROGRAM_ID,
            pool.swapProgramId,
            lamports,
            minAmountOut
        )
        transaction.addInstruction(approve)
        transaction.addInstruction(swap)

        sourceData.closeInstructions.forEach { transaction.addInstruction(it) }
        destinationData.closeInstructions.forEach { transaction.addInstruction(it) }

        signers.add(userTransferAuthority)
    }

    // / Find routes for from and to token name, aka symbol
    fun findRoutes(
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

    /* For test purposes */
    fun getSwapInfo(): OrcaSwapInfo? = info

    private suspend fun createIntermediaryTokenAndDestinationTokenAddressIfNeeded(
        pool0: OrcaPool,
        pool1: OrcaPool,
        toWalletPubkey: String?,
        feeRelayerFeePayer: PublicKey?
    ): Pair<PublicKey, PublicKey> {

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
            return intermediaryData.account to destinationData.account
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

        val signature = swapRepository.sendAndWait(serializedTransaction) {
            // TODO: wait for confirmation and return the addresses
            // notificationHandler.observeSignatureNotification(signature: txid)
        }

        return intermediaryData.account to destinationData.account
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
        val allTokens = orcaTokens.mapNotNull { orcaToken ->
            val userToken = userTokens.find { it.mintAddress == orcaToken.mint }
            if (userToken != null) {
                return@mapNotNull userToken
            } else {
                return@mapNotNull userInteractor.findTokenData(orcaToken.mint)
            }
        }

        return allTokens
            .sortedByDescending { it.isSOL }
            .sortedByDescending { it is Token.Active }
    }
}