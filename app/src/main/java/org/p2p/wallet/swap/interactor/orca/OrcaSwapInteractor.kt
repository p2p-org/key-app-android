package org.p2p.wallet.swap.interactor.orca

import kotlinx.coroutines.delay
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.utils.crypto.Base64Utils
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerSwapInteractor
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAmountInteractor
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.model.OrcaInstructionsData
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.OrcaSwapInfo
import org.p2p.wallet.swap.model.orca.OrcaSwapResult
import org.p2p.wallet.swap.repository.OrcaSwapRepository
import org.p2p.wallet.transaction.TransactionManager
import org.p2p.wallet.transaction.interactor.TransactionStatusInteractor
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger

class OrcaSwapInteractor(
    private val rpcRepository: RpcRepository,
    private val swapRepository: OrcaSwapRepository,
    private val feeRelayerSwapInteractor: FeeRelayerSwapInteractor,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val amountInteractor: TransactionAmountInteractor,
    private val orcaRouteInteractor: OrcaRouteInteractor,
    private val orcaInstructionsInteractor: OrcaInstructionsInteractor,
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val orcaPoolInteractor: OrcaPoolInteractor,
    private val transactionStatusInteractor: TransactionStatusInteractor,
    private val transactionManager: TransactionManager,
    private val environmentManager: EnvironmentManager,
    private val tokenKeyProvider: TokenKeyProvider
) {

    companion object {
        private const val DELAY_FIVE_SECONDS_MS = 5000L
        private const val ONE_AND_HALF_MINUTE_WAIT_IN_MS = 1000 * 60 * 1.5
    }

    /*
    * If transaction will need to create a new account,
    * then the fee for account creation will be paid via this token
    * */
    private lateinit var feePayerToken: Token.Active

    /*
    * Initialize fee payer token
    * */
    suspend fun initialize(sol: Token.Active) {
        feePayerToken = sol
        orcaInfoInteractor.load()
    }

    fun getFeePayerToken(): Token.Active = feePayerToken

    fun setFeePayerToken(newToken: Token.Active) {
        if (!this::feePayerToken.isInitialized) throw IllegalStateException("PayToken is not initialized")
        if (newToken.publicKey.equals(feePayerToken)) return

        feePayerToken = newToken
    }

    suspend fun initialize() {
        feeRelayerInteractor.load()
    }

    // Execute swap
    suspend fun swap(
        fromToken: Token.Active,
        toToken: Token,
        bestPoolsPair: OrcaPoolsPair,
        amount: Double,
        slippage: Double
    ): OrcaSwapResult {
        val info = orcaInfoInteractor.getInfo() ?: throw IllegalStateException("Orca info is null")
        val fromDecimals = bestPoolsPair[0].tokenABalance?.decimals
            ?: throw IllegalStateException("Invalid pool")

        val lamports = BigDecimal(amount).toLamports(fromDecimals)
        val feeRelayerProgramId = FeeRelayerProgram.getProgramId(environmentManager.isMainnet())
        val payingFeeToken = TokenInfo(feePayerToken.publicKey, feePayerToken.mintAddress)

        when {
            feePayerToken.isSOL ->
                return if (bestPoolsPair.size == 1) {
                    swapDirect(
                        pool = bestPoolsPair.first(),
                        fromTokenPubkey = fromToken.publicKey,
                        toTokenPubkey = toToken.publicKey,
                        amount = lamports,
                        slippage = slippage
                    )
                } else {
                    swapTransitive(
                        fromWalletPubkey = fromToken.publicKey,
                        toWalletPubkey = toToken.publicKey,
                        bestPoolsPair = bestPoolsPair,
                        feeRelayerFeePayer = null,
                        info = info,
                        owner = Account(tokenKeyProvider.secretKey),
                        lamports = lamports,
                        slippage = slippage
                    )
                }
            else -> {
                val preparedTransaction = feeRelayerSwapInteractor.prepareSwapTransaction(
                    feeRelayerProgramId = feeRelayerProgramId,
                    sourceToken = TokenInfo(fromToken.publicKey, fromToken.mintAddress),
                    destinationTokenMint = toToken.mintAddress,
                    destinationAddress = toToken.publicKey,
                    payingFeeToken = payingFeeToken,
                    swapPools = bestPoolsPair,
                    inputAmount = lamports,
                    slippage = slippage,
                )

                val signature = preparedTransaction.transaction.signature.signature
                transactionManager.emitTransactionId(signature)

                val transactionId = feeRelayerInteractor.topUpAndRelayTransaction(
                    preparedTransaction = preparedTransaction,
                    payingFeeToken = payingFeeToken
                ).firstOrNull().orEmpty()

                return OrcaSwapResult.Finished(transactionId, toToken.publicKey.orEmpty())
            }
        }
    }

    private suspend fun swapDirect(
        pool: OrcaPool,
        fromTokenPubkey: String,
        toTokenPubkey: String?,
        amount: BigInteger,
        slippage: Double
    ): OrcaSwapResult {
        val owner = Account(tokenKeyProvider.secretKey)
        val info = orcaInfoInteractor.getInfo() ?: return OrcaSwapResult.InvalidInfoOrPair

        val accountInstructions = orcaRouteInteractor.constructExchange(
            pool = pool,
            tokens = info.tokens,
            owner = owner,
            fromTokenPubkey = fromTokenPubkey,
            toTokenPubkey = toTokenPubkey,
            feeRelayerFeePayer = null,
            amount = amount,
            slippage = slippage
        )

        // serialize transaction
        val transaction = Transaction()
        val instructions = accountInstructions.instructions + accountInstructions.cleanupInstructions
        transaction.addInstructions(instructions)
        val blockhash = rpcRepository.getRecentBlockhash().recentBlockhash
        transaction.recentBlockHash = blockhash

        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
        val freeTransactionLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
        val transactionNetworkFee = BigInteger.valueOf(2) * relayInfo.lamportsPerSignature // feePayer, owner
        val freeTransactionFeeAvailable = freeTransactionLimit.isFreeTransactionFeeAvailable(transactionNetworkFee)
        if (freeTransactionFeeAvailable) {
            val feeRelayerFeePayer = feeRelayerAccountInteractor.getFeePayerPublicKey()
            transaction.feePayer = feeRelayerFeePayer
        } else {
            transaction.feePayer = owner.publicKey
        }

        val signers = listOf(owner) + accountInstructions.signers
        transaction.sign(signers)

//            val sm = transaction.serialize()
//            val st = Base64Utils.encode(sm)
//            Timber.tag("###").d(st)

        val transactionId = if (freeTransactionFeeAvailable) {
            feeRelayerInteractor.relayTransaction(transaction).firstOrNull().orEmpty()
        } else {
            rpcRepository.sendTransaction(transaction)
        }

        // fixme: find correct address
        return OrcaSwapResult.Finished(transactionId, toTokenPubkey.orEmpty())
    }

    private suspend fun swapTransitive(
        fromWalletPubkey: String,
        toWalletPubkey: String?,
        bestPoolsPair: OrcaPoolsPair,
        feeRelayerFeePayer: PublicKey?,
        info: OrcaSwapInfo,
        owner: Account,
        lamports: BigInteger,
        slippage: Double
    ): OrcaSwapResult =
        if (toWalletPubkey != null) {
            swapTransitiveSingle(
                bestPoolsPair = bestPoolsPair,
                toWalletPubkey = toWalletPubkey,
                feeRelayerFeePayer = feeRelayerFeePayer,
                info = info,
                owner = owner,
                fromWalletPubkey = fromWalletPubkey,
                lamports = lamports,
                slippage = slippage
            )
        } else {
            swapTransitiveSeparated(
                bestPoolsPair = bestPoolsPair,
                toWalletPubkey = toWalletPubkey,
                feeRelayerFeePayer = feeRelayerFeePayer,
                info = info,
                owner = owner,
                fromWalletPubkey = fromWalletPubkey,
                lamports = lamports,
                slippage = slippage
            )
        }

    private suspend fun swapTransitiveSeparated(
        bestPoolsPair: OrcaPoolsPair,
        toWalletPubkey: String?,
        feeRelayerFeePayer: PublicKey?,
        info: OrcaSwapInfo,
        owner: Account,
        fromWalletPubkey: String,
        lamports: BigInteger,
        slippage: Double
    ): OrcaSwapResult {
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

        val pool0AccountInstructions = orcaRouteInteractor.constructExchange(
            pool = pool0,
            tokens = info.tokens,
            owner = owner,
            fromTokenPubkey = fromWalletPubkey,
            toTokenPubkey = intermediary.account.toBase58(),
            feeRelayerFeePayer = null,
            amount = lamports,
            slippage = slippage
        )

        val minOutAmount = pool0.getMinimumAmountOut(lamports, slippage)
            ?: throw IllegalStateException("Couldn't calculate min amount out")

        val pool1AccountInstructions = orcaRouteInteractor.constructExchange(
            pool = pool1,
            tokens = info.tokens,
            owner = owner,
            fromTokenPubkey = intermediary.account.toBase58(),
            toTokenPubkey = destination.account.toBase58(),
            feeRelayerFeePayer = null,
            amount = minOutAmount,
            slippage = slippage
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

        val transaction = Transaction()
        transaction.addInstructions(accountInstructions.instructions)
        transaction.addInstructions(accountInstructions.cleanupInstructions)

        transaction.feePayer = feeRelayerFeePayer
        val recentBlockhash = rpcRepository.getRecentBlockhash()
        transaction.recentBlockHash = recentBlockhash.recentBlockhash
        transaction.sign(accountInstructions.signers)

        val serializedMessage = transaction.serialize()
        val serializedTransaction = Base64Utils.encode(serializedMessage)

        val signature = transaction.signature

        val transactionId = feeRelayerInteractor.relayTransaction(transaction).firstOrNull().orEmpty()

        return OrcaSwapResult.Finished(transactionId, signature.signature)
    }

    private suspend fun swapTransitiveSingle(
        bestPoolsPair: OrcaPoolsPair,
        toWalletPubkey: String?,
        feeRelayerFeePayer: PublicKey?,
        info: OrcaSwapInfo,
        owner: Account,
        fromWalletPubkey: String,
        lamports: BigInteger,
        slippage: Double
    ): OrcaSwapResult {
        val pool0 = bestPoolsPair[0]
        val pool1 = bestPoolsPair[1]

        val (intermediary, destination) = constructIntermediaryAccountInstructions(
            pool0 = pool0,
            pool1 = pool1,
            toWalletPubkey = toWalletPubkey
        )

        Timber.tag("TransitiveSwap").d("Account creation instructions are created")

        val pool0AccountInstructions = orcaRouteInteractor.constructExchange(
            pool = pool0,
            tokens = info.tokens,
            owner = owner,
            fromTokenPubkey = fromWalletPubkey,
            toTokenPubkey = intermediary.account.toBase58(),
            feeRelayerFeePayer = null,
            amount = lamports,
            slippage = slippage
        )

        val minOutAmount = pool0.getMinimumAmountOut(lamports, slippage)
            ?: throw IllegalStateException("Couldn't calculate min amount out")

        val pool1AccountInstructions = orcaRouteInteractor.constructExchange(
            pool = pool1,
            tokens = info.tokens,
            owner = owner,
            fromTokenPubkey = intermediary.account.toBase58(),
            toTokenPubkey = destination.account.toBase58(),
            feeRelayerFeePayer = null,
            amount = minOutAmount,
            slippage = slippage
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

        val transaction = Transaction()
        transaction.addInstructions(accountInstructions.instructions)
        transaction.addInstructions(accountInstructions.cleanupInstructions)
        transaction.feePayer = feeRelayerFeePayer

        val recentBlockhash = rpcRepository.getRecentBlockhash()
        transaction.recentBlockHash = recentBlockhash.recentBlockhash
        transaction.sign(accountInstructions.signers)

        val serializedMessage = transaction.serialize()
        val serializedTransaction = Base64Utils.encode(serializedMessage)

        val signature = transaction.signature

        val transactionId = feeRelayerInteractor.relayTransaction(transaction).firstOrNull().orEmpty()
        return OrcaSwapResult.Finished(transactionId, signature.signature)
    }

    // Get fees from current context
    // - Returns: transactions fees (fees for signatures), liquidity provider fees (fees in intermediary token?, fees in destination token)
    suspend fun getFees(
        myWalletsMints: List<String>,
        fromWalletPubkey: String,
        toWalletPubkey: String?,
        feeRelayerFeePayerPubkey: String?,
        bestPoolsPair: OrcaPoolsPair?,
        inputAmount: BigDecimal?,
        slippage: Double
    ): Pair<BigInteger, List<BigInteger>> {
        val owner = tokenKeyProvider.publicKey.toPublicKey()

        val lamportsPerSignature = amountInteractor.getLamportsPerSignature()
        val minRentExempt = amountInteractor.getMinBalanceForRentExemption()

        var transactionFees: BigInteger = BigInteger.ZERO

        val numberOfPools = BigInteger.valueOf(bestPoolsPair?.size?.toLong() ?: 0L)

        var numberOfTransactions = BigInteger.ONE

        if (numberOfPools == BigInteger.valueOf(2L)) {
            val myTokens = myWalletsMints.mapNotNull { orcaPoolInteractor.getTokenFromMint(it) }.map { it.first }
            val intermediaryTokenName = bestPoolsPair!![0].tokenBName

            if (!myTokens.contains(intermediaryTokenName) || toWalletPubkey == null) {
                numberOfTransactions += BigInteger.ONE
            }

            if (intermediaryTokenName == SOL_SYMBOL) {
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
        if (fromWalletPubkey == owner.toBase58() || bestPoolsPair!!.last().tokenBName == SOL_SYMBOL) {
            transactionFees += lamportsPerSignature
            transactionFees += minRentExempt
        }

        if (toWalletPubkey.isNullOrEmpty()) {
            transactionFees += lamportsPerSignature
            transactionFees += minRentExempt
        }

        val liquidityProviderFees = if (inputAmount != null) {
            orcaRouteInteractor.calculateLiquidityProviderFees(
                poolsPair = bestPoolsPair!!,
                inputAmount = inputAmount,
                slippage = slippage
            )
        } else emptyList()

        return transactionFees to liquidityProviderFees
    }

    private suspend fun constructIntermediaryAccountInstructions(
        pool0: OrcaPool,
        pool1: OrcaPool,
        toWalletPubkey: String?
    ): List<OrcaInstructionsData> {
        val info = orcaInfoInteractor.getInfo()

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
            feePayer = owner,
            closeAfterward = true // todo:
        )

        /* building instructions for destination token */
        val destinationData = orcaInstructionsInteractor.buildDestinationInstructions(
            owner = owner,
            destination = toWalletPubkey?.toPublicKey(),
            destinationMint = destinationMint,
            feePayer = owner,
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
        val info = orcaInfoInteractor.getInfo()
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
            feePayer = owner,
            closeAfterward = true // todo:
        )

        intermediaryData.instructions.forEach { transaction.addInstruction(it) }

        /* building instructions for destination token */
        val destinationData = orcaInstructionsInteractor.buildDestinationInstructions(
            owner = owner,
            destination = toWalletPubkey?.toPublicKey(),
            destinationMint = destinationMint,
            feePayer = owner,
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
        transaction.feePayer = feePayerPublicKey

        val blockhash = rpcRepository.getRecentBlockhash().recentBlockhash
        transaction.recentBlockHash = blockhash

        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.sign(signers)

        val serializedMessage = transaction.serialize()
        val serializedTransaction = Base64Utils.encode(serializedMessage)

        /**
         * [swapRepository.sendAndWait] sends transaction and connects to the socket client
         * where transaction confirmation status is awaiting.
         * Once it came, [transactionStatusInteractor] callback triggers and we proceed
         * */
        transactionStatusInteractor.onSignatureReceived = {
            onConfirmed()
        }

        swapRepository.sendAndWait(serializedTransaction)

        return listOf(intermediaryData, destinationData)
    }
}