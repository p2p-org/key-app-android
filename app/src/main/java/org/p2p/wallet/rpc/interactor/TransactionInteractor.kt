package org.p2p.wallet.rpc.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.utils.crypto.Base64Utils
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.rpc.repository.history.RpcHistoryRepository
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigInteger

class TransactionInteractor(
    private val rpcBlockhashRepository: RpcBlockhashRepository,
    private val rpcTransactionRepository: RpcHistoryRepository,
    private val rpcAmountRepository: RpcAmountRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun prepareTransaction(
        instructions: List<TransactionInstruction>,
        signers: List<Account>,
        feePayer: PublicKey,
        accountsCreationFee: BigInteger,
        recentBlockhash: String? = null,
        lamportsPerSignature: BigInteger? = null
    ): PreparedTransaction {
        val actualLamportsPerSignature =
            lamportsPerSignature ?: rpcAmountRepository.getLamportsPerSignature(commitment = null)

        val transaction = Transaction()
        transaction.addInstructions(instructions)
        transaction.recentBlockHash = recentBlockhash ?: rpcBlockhashRepository.getRecentBlockhash().recentBlockhash
        transaction.feePayer = feePayer

        // calculate fee first
        val expectedFee = FeeAmount(
            transaction = transaction.calculateTransactionFee(actualLamportsPerSignature),
            accountBalances = accountsCreationFee
        )

        // resign transaction
        transaction.sign(signers)
        return PreparedTransaction(transaction, signers, expectedFee)
    }

    suspend fun serializeAndSend(
        preparedTransaction: PreparedTransaction,
        isSimulation: Boolean
    ): String {

        return if (isSimulation) {
            rpcTransactionRepository.simulateTransaction(preparedTransaction.transaction)
        } else {
            rpcTransactionRepository.sendTransaction(preparedTransaction.transaction)
        }
    }

    suspend fun serializeTransaction(
        instructions: List<TransactionInstruction>,
        recentBlockhash: String? = null,
        signers: List<Account>,
        feePayer: PublicKey? = null
    ): String {
        // get recentBlockhash
        val blockhash = if (recentBlockhash.isNullOrEmpty()) {
            rpcBlockhashRepository.getRecentBlockhash().recentBlockhash
        } else {
            recentBlockhash
        }

        val accountPublicKey = tokenKeyProvider.publicKey.toPublicKey()
        val feePayerPublicKey = feePayer ?: accountPublicKey

        // serialize transaction
        val transaction = Transaction().apply {
            addInstructions(instructions)
            setFeePayer(feePayerPublicKey)
            recentBlockHash = blockhash
            sign(signers)
        }

        val serializedMessage = transaction.serialize()
        val serializedTransaction = Base64Utils.encode(serializedMessage)

        Timber.d("Serialized transaction: $serializedTransaction")
        return serializedTransaction
    }
}
