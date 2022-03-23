package org.p2p.wallet.swap.interactor.orca

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.programs.TokenSwapProgram
import org.p2p.wallet.swap.interactor.SwapInstructionsInteractor
import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.wallet.swap.model.orca.OrcaPools
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.OrcaRoute
import org.p2p.wallet.swap.model.orca.OrcaRoutes
import org.p2p.wallet.swap.model.orca.OrcaSwapInfo
import org.p2p.wallet.swap.model.orca.OrcaTokens
import org.p2p.wallet.swap.repository.OrcaSwapRepository
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toPublicKey
import java.math.BigDecimal
import java.math.BigInteger

class OrcaRouteInteractor(
    private val orcaSwapRepository: OrcaSwapRepository,
    private val instructionsInteractor: SwapInstructionsInteractor
) {

    companion object {
        private const val PATH_STABLE = "[stable]"
    }

    private val balancesCache = mutableMapOf<String, AccountBalance>()

    // Construct exchange
    suspend fun constructExchange(
        pool: OrcaPool,
        tokens: OrcaTokens,
        owner: Account,
        fromTokenPubkey: String,
        toTokenPubkey: String?,
        amount: BigInteger,
        slippage: Double,
        feeRelayerFeePayer: PublicKey?,
        minRenExemption: BigInteger
    ): AccountInstructions {
        val fromMint = tokens[pool.tokenAName]?.mint?.toPublicKey()
        val toMint = tokens[pool.tokenBName]?.mint?.toPublicKey()
        val fromTokenPubkey = fromTokenPubkey.toPublicKey()

        if (fromMint == null || toMint == null) {
            throw IllegalStateException("Pool mints are not found")
        }

        // Create fromTokenAccount when needed
        val sourceAccountInstructions =
            if (fromMint.toBase58() == WRAPPED_SOL_MINT && owner.publicKey.equals(fromTokenPubkey)) {
                instructionsInteractor.prepareCreatingWSOLAccountAndCloseWhenDone(
                    from = owner.publicKey,
                    amount = amount,
                    payer = feeRelayerFeePayer ?: owner.publicKey
                )
            } else {
                AccountInstructions(fromTokenPubkey)
            }

        // If necessary, create a TokenAccount for the output token
        // If destination token is Solana, create WSOL if needed
        val destinationAccountInstructions = if (toMint.toBase58() == WRAPPED_SOL_MINT) {
            val toTokenPublicKey = toTokenPubkey?.toPublicKey()
            if (toTokenPublicKey != null && !toTokenPublicKey.equals(owner.publicKey)) {
                // wrapped sol has already been created, just return it, then close later
                val cleanupInstructions = listOf(
                    TokenProgram.closeAccountInstruction(
                        TokenProgram.PROGRAM_ID,
                        toTokenPublicKey,
                        owner.publicKey,
                        owner.publicKey
                    )
                )
                AccountInstructions(
                    account = toTokenPublicKey,
                    cleanupInstructions = cleanupInstructions
                )
            } else {
                // create wrapped sol
                instructionsInteractor.prepareCreatingWSOLAccountAndCloseWhenDone(
                    from = owner.publicKey,
                    amount = BigInteger.ZERO,
                    payer = owner.publicKey
                )
            }
        } else if (toTokenPubkey != null) {
            // If destination is another token and has already been created
            AccountInstructions(toTokenPubkey.toPublicKey())
        } else {
            // create wrapped sol
            instructionsInteractor.prepareForCreatingAssociatedTokenAccount(
                owner = owner.publicKey,
                mint = toMint,
                feePayer = owner.publicKey,
                closeAfterward = false
            )
        }

        // form instructions
        val instructions = mutableListOf<TransactionInstruction>()
        val cleanupInstructions = mutableListOf<TransactionInstruction>()
        var accountCreationFee: BigInteger = BigInteger.ZERO

        // source
        instructions += sourceAccountInstructions.instructions
        cleanupInstructions += sourceAccountInstructions.cleanupInstructions

        if (sourceAccountInstructions.instructions.isNotEmpty()) {
            accountCreationFee += minRenExemption
        }

        // destination
        instructions += destinationAccountInstructions.instructions
        cleanupInstructions += destinationAccountInstructions.cleanupInstructions
        if (destinationAccountInstructions.instructions.isNotEmpty()) {
            accountCreationFee += minRenExemption
        }

        // swap instructions
        val minAmountOut = pool.getMinimumAmountOut(amount, slippage)
            ?: throw IllegalStateException("Couldn't estimate minimum out amount")

        val swapInstruction = TokenSwapProgram.swapInstruction(
            pool.account,
            pool.authority,
            owner.publicKey,
            sourceAccountInstructions.account,
            pool.tokenAccountA,
            pool.tokenAccountB,
            destinationAccountInstructions.account,
            pool.poolTokenMint,
            pool.feeAccount,
            pool.hostFeeAccount,
            TokenProgram.PROGRAM_ID,
            pool.swapProgramId,
            amount,
            minAmountOut
        )

        instructions += swapInstruction

        val signers = mutableListOf<Account>()
        signers += sourceAccountInstructions.signers
        signers += destinationAccountInstructions.signers

        return AccountInstructions(
            destinationAccountInstructions.account,
            instructions,
            cleanupInstructions,
            signers
        )
    }

    suspend fun loadBalances(currentRoutes: List<OrcaRoute>, infoPools: OrcaPools?) {
        val balanceAccounts = currentRoutes
            .flatten()
            .flatMap {
                listOfNotNull(
                    infoPools?.get(it)?.tokenAccountA?.toBase58(),
                    infoPools?.get(it)?.tokenAccountB?.toBase58()
                )
            }
            .distinct()
            .filterNot {
                balancesCache.containsKey(it)
            }

        if (balanceAccounts.isEmpty()) return

        orcaSwapRepository.loadTokenBalances(balanceAccounts).forEach { (publicKey, balance) ->
            balancesCache[publicKey] = balance
        }
    }

    suspend fun getPools(
        infoPools: OrcaPools?,
        route: OrcaRoute,
        fromTokenName: String,
        toTokenName: String
    ): List<OrcaPool> {
        if (route.isEmpty()) return emptyList()

        val pools = route.mapNotNull { fixedPool(infoPools, it) }.toMutableList()
        // modify orders
        if (pools.size == 2) {
            // reverse order of the 2 pools
            // Ex: Swap from SOCN -> BTC, but paths are
            // [
            //     "BTC/SOL[aquafarm]",
            //     "SOCN/SOL[stable][aquafarm]"
            // ]
            // Need to change to
            // [
            //     "SOCN/SOL[stable][aquafarm]",
            //     "BTC/SOL[aquafarm]"
            // ]

            if (pools[0].tokenAName != fromTokenName && pools[0].tokenBName != fromTokenName) {
                val temp = pools[0]
                pools[0] = pools[1]
                pools[1] = temp
            }
        }

        // reverse token A and token B in pool if needed
        for (i in 0 until pools.size) {
            if (i == 0) {
                var pool = pools[0]
                if (pool.tokenAName.fixedTokenName() != fromTokenName.fixedTokenName()) {
                    pool = pool.reversed
                }
                pools[0] = pool
            }

            if (i == 1) {
                var pool = pools[1]
                if (pool.tokenBName.fixedTokenName() != toTokenName.fixedTokenName()) {
                    pool = pool.reversed
                }
                pools[1] = pool
            }
        }

        return pools
    }

    private suspend fun fixedPool(
        infoPools: OrcaPools?,
        path: String // Ex. BTC/SOL[aquafarm][stable]){}
    ): OrcaPool? {

        val pool = infoPools?.get(path) ?: return null

        pool.isStable = path.contains(PATH_STABLE)

        // get balances
        var tokenABalance = pool.tokenABalance ?: balancesCache[pool.tokenAccountA.toBase58()]
        var tokenBBalance = pool.tokenBBalance ?: balancesCache[pool.tokenAccountB.toBase58()]

        if (tokenABalance == null || tokenBBalance == null) {
            tokenABalance = orcaSwapRepository.loadTokenBalance(pool.tokenAccountA)
            tokenBBalance = orcaSwapRepository.loadTokenBalance(pool.tokenAccountB)

            balancesCache[pool.tokenAccountA.toBase58()] = tokenABalance
            balancesCache[pool.tokenAccountB.toBase58()] = tokenBBalance
        }

        pool.tokenABalance = tokenABalance
        pool.tokenBBalance = tokenBBalance

        return pool
    }

    // Find possible destination token (symbol)
    // - Parameter fromTokenName: from token name (symbol)
    // - Returns: List of token symbols that can be swapped to
    fun findPossibleDestinations(
        info: OrcaSwapInfo,
        fromTokenName: String
    ): List<String> {
        return findRoutes(info, fromTokenName, null).keys
            .mapNotNull { key ->
                key.split("/").find { it != fromTokenName }
            }
            .distinct()
            .sorted()
    }

    fun calculateLiquidityProviderFees(
        poolsPair: OrcaPoolsPair,
        inputAmount: BigDecimal,
        slippage: Double
    ): List<BigInteger> {
        if (poolsPair.size < 1) return emptyList()
        val pool0 = poolsPair[0]

        val sourceDecimals = pool0.tokenABalance?.decimals ?: throw IllegalStateException("Token A balance is null")
        val inputAmount = inputAmount.toLamports(sourceDecimals)

        // 1 pool
        val result = mutableListOf<BigInteger>()
        val fee0 = pool0.calculatingFees(inputAmount)
        result.add(fee0)

        // 2 pool
        if (poolsPair.size == 2) {
            val pool1 = poolsPair[1]

            val inputAmount = pool0.getMinimumAmountOut(inputAmount, slippage)
            if (inputAmount != null) {
                val fee1 = pool1.calculatingFees(inputAmount)
                result.add(fee1)
            }
        }
        return result
    }

    // / Find routes for from and to token name, aka symbol
    fun findRoutes(
        info: OrcaSwapInfo,
        fromTokenName: String?,
        toTokenName: String?
    ): OrcaRoutes {
        // if fromToken isn't selected
        if (fromTokenName.isNullOrEmpty()) return mutableMapOf()

        // if toToken isn't selected
        if (toTokenName == null) {
            // get all routes that have token A
            return info.routes.filter { it.key.split("/").contains(fromTokenName) } as OrcaRoutes
        }

        // get routes with fromToken and toToken
        val pair = listOf(fromTokenName, toTokenName)
        val validRoutesNames = listOf(
            pair.joinToString("/"),
            pair.reversed().joinToString("/")
        )
        return info.routes.filter { validRoutesNames.contains(it.key) } as OrcaRoutes
    }
}

private fun String.fixedTokenName(): String = this.split("[").first()
