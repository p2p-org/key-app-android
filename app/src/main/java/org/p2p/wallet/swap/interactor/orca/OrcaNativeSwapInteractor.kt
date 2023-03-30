package org.p2p.wallet.swap.interactor.orca

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.delay
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import org.p2p.solanaj.utils.crypto.Base64Utils
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.rpc.repository.history.RpcTransactionRepository
import org.p2p.wallet.swap.model.OrcaInstructionsData
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.OrcaSwapInfo
import org.p2p.wallet.swap.model.orca.OrcaSwapResult
import org.p2p.wallet.swap.repository.OrcaSwapRepository
import org.p2p.wallet.transaction.interactor.TransactionStatusInteractor
import org.p2p.wallet.utils.retryRequest
import org.p2p.wallet.utils.toPublicKey

class OrcaNativeSwapInteractor(
    private val rpcBlockhashRepository: RpcBlockhashRepository,
    private val rpcTransactionRepository: RpcTransactionRepository,
    private val swapRepository: OrcaSwapRepository,
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val orcaRouteInteractor: OrcaRouteInteractor,
    private val orcaInstructionsInteractor: OrcaInstructionsInteractor,
    private val transactionStatusInteractor: TransactionStatusInteractor,
    private val rpcAmountRepository: RpcAmountRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    companion object {
        private const val DELAY_FIVE_SECONDS_MS = 5000L
        private const val ONE_AND_HALF_MINUTE_WAIT_IN_MS = 1000 * 60 * 1.5
    }

    suspend fun swap(
        fromWalletPubkey: String,
        toWalletPubkey: String?,
        bestPoolsPair: OrcaPoolsPair,
        amount: BigInteger,
        slippage: Double
    ): OrcaSwapResult {
        val owner = Account(tokenKeyProvider.keyPair)
        val info = orcaInfoInteractor.getInfo() ?: throw IllegalStateException("Swap info missing")

        if (bestPoolsPair.isEmpty()) {
            throw IllegalStateException("Cannot swap these tokens")
        }

        val minRenExemption = rpcAmountRepository.getMinBalanceForRentExemption(ACCOUNT_INFO_DATA_LENGTH)

        return if (bestPoolsPair.size == 1) {
            swapDirect(
                owner = owner,
                pool = bestPoolsPair[0],
                fromTokenPubkey = fromWalletPubkey,
                toTokenPubkey = toWalletPubkey,
                amount = amount,
                feePayer = owner.publicKey,
                slippage = slippage,
                minRenExemption = minRenExemption
            )
        } else {
            swapTransitive(
                fromWalletPubkey = fromWalletPubkey,
                toWalletPubkey = toWalletPubkey,
                bestPoolsPair = bestPoolsPair,
                feePayer = owner.publicKey,
                info = info,
                owner = Account(tokenKeyProvider.keyPair),
                lamports = amount,
                slippage = slippage,
                minRenExemption = minRenExemption
            )
        }
    }

    private suspend fun swapDirect(
        owner: Account,
        pool: OrcaPool,
        fromTokenPubkey: String,
        toTokenPubkey: String?,
        feePayer: PublicKey,
        amount: BigInteger,
        slippage: Double,
        minRenExemption: BigInteger
    ): OrcaSwapResult {
        val info = orcaInfoInteractor.getInfo() ?: return OrcaSwapResult.InvalidInfoOrPair

        val accountInstructions = orcaRouteInteractor.constructExchange(
            pool = pool,
            tokens = info.tokens,
            owner = owner,
            fromTokenPublicKeyB64 = fromTokenPubkey,
            toTokenPublicKeyB64 = toTokenPubkey,
            feeRelayerFeePayer = null,
            amount = amount,
            slippage = slippage,
            minRenExemption = minRenExemption
        )

        // serialize transaction
        val transaction = Transaction()
        val instructions = accountInstructions.instructions + accountInstructions.cleanupInstructions
        transaction.addInstructions(instructions)
        val blockhash = rpcBlockhashRepository.getRecentBlockhash().recentBlockhash
        transaction.setRecentBlockhash(blockhash)
        transaction.setFeePayer(feePayer)

        val signers = listOf(owner) + accountInstructions.signers
        transaction.sign(signers)

        val transactionId = rpcTransactionRepository.sendTransaction(transaction)

        // fixme: find correct address
        return OrcaSwapResult.Finished(transactionId, toTokenPubkey.orEmpty())
    }

    private suspend fun swapTransitive(
        fromWalletPubkey: String,
        toWalletPubkey: String?,
        bestPoolsPair: OrcaPoolsPair,
        feePayer: PublicKey,
        info: OrcaSwapInfo,
        owner: Account,
        lamports: BigInteger,
        slippage: Double,
        minRenExemption: BigInteger
    ): OrcaSwapResult =
        swapTransitiveSeparated(
            bestPoolsPair = bestPoolsPair,
            toWalletPubkey = toWalletPubkey,
            feePayer = feePayer,
            info = info,
            owner = owner,
            fromWalletPubkey = fromWalletPubkey,
            lamports = lamports,
            slippage = slippage,
            minRenExemption = minRenExemption
        )

    private suspend fun swapTransitiveSeparated(
        bestPoolsPair: OrcaPoolsPair,
        toWalletPubkey: String?,
        feePayer: PublicKey,
        info: OrcaSwapInfo,
        owner: Account,
        fromWalletPubkey: String,
        lamports: BigInteger,
        slippage: Double,
        minRenExemption: BigInteger
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
            feePayer = owner.publicKey,
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
            fromTokenPublicKeyB64 = fromWalletPubkey,
            toTokenPublicKeyB64 = intermediary.account.toBase58(),
            feeRelayerFeePayer = null,
            amount = lamports,
            slippage = slippage,
            minRenExemption = minRenExemption
        )

        val minOutAmount = pool0.getMinimumAmountOut(lamports, slippage)
            ?: throw IllegalStateException("Couldn't calculate min amount out")

        val pool1AccountInstructions = orcaRouteInteractor.constructExchange(
            pool = pool1,
            tokens = info.tokens,
            owner = owner,
            fromTokenPublicKeyB64 = intermediary.account.toBase58(),
            toTokenPublicKeyB64 = destination.account.toBase58(),
            feeRelayerFeePayer = null,
            amount = minOutAmount,
            slippage = slippage,
            minRenExemption = minRenExemption
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

        transaction.setFeePayer(feePayer)
        val transactionId = retryRequest {
            val recentBlockhash = rpcBlockhashRepository.getRecentBlockhash()
            transaction.setRecentBlockhash(recentBlockhash.recentBlockhash)
            transaction.sign(accountInstructions.signers)
            rpcTransactionRepository.sendTransaction(transaction)
        }

        val signature = transaction.signature?.signature.orEmpty()
        return OrcaSwapResult.Finished(transactionId, signature)
    }

    private suspend fun swapTransitiveSingle(
        bestPoolsPair: OrcaPoolsPair,
        toWalletPubkey: String?,
        feePayer: PublicKey,
        info: OrcaSwapInfo,
        owner: Account,
        fromWalletPubkey: String,
        lamports: BigInteger,
        slippage: Double,
        minRenExemption: BigInteger
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
            fromTokenPublicKeyB64 = fromWalletPubkey,
            toTokenPublicKeyB64 = intermediary.account.toBase58(),
            feeRelayerFeePayer = null,
            amount = lamports,
            slippage = slippage,
            minRenExemption = minRenExemption
        )

        val minOutAmount = pool0.getMinimumAmountOut(lamports, slippage)
            ?: throw IllegalStateException("Couldn't calculate min amount out")

        val pool1AccountInstructions = orcaRouteInteractor.constructExchange(
            pool = pool1,
            tokens = info.tokens,
            owner = owner,
            fromTokenPublicKeyB64 = intermediary.account.toBase58(),
            toTokenPublicKeyB64 = destination.account.toBase58(),
            feeRelayerFeePayer = null,
            amount = minOutAmount,
            slippage = slippage,
            minRenExemption = minRenExemption
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
        transaction.setFeePayer(feePayer)

        val recentBlockhash = rpcBlockhashRepository.getRecentBlockhash()
        transaction.setRecentBlockhash(recentBlockhash.recentBlockhash)
        transaction.sign(accountInstructions.signers)

        val signature = transaction.signature?.signature.orEmpty()

        val transactionId = rpcTransactionRepository.sendTransaction(transaction)
        return OrcaSwapResult.Finished(transactionId, signature)
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
        feePayer: PublicKey,
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
        transaction.setFeePayer(feePayer)

        val blockhash = rpcBlockhashRepository.getRecentBlockhash().recentBlockhash
        transaction.setRecentBlockhash(blockhash)

        val signers = listOf(Account(tokenKeyProvider.keyPair))
        transaction.sign(signers)

        val serializedMessage = transaction.serialize()
        val serializedTransaction = Base64Utils.encode(serializedMessage)

        /**
         * swapRepository.sendAndWait sends transaction and connects to the socket client
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
