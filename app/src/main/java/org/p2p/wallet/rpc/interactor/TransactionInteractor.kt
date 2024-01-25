package org.p2p.wallet.rpc.interactor

import timber.log.Timber
import java.math.BigInteger
import java.nio.ByteBuffer
import org.p2p.core.crypto.Base64String
import org.p2p.core.crypto.Base64Utils
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.model.types.ConfirmationStatus
import org.p2p.solanaj.utils.ShortvecEncoding
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.rpc.repository.history.RpcTransactionRepository
import org.p2p.wallet.send.model.send_service.GeneratedTransaction
import org.p2p.wallet.utils.toPublicKey

class TransactionInteractor(
    private val rpcBlockhashRepository: RpcBlockhashRepository,
    private val rpcTransactionRepository: RpcTransactionRepository,
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
        transaction.setRecentBlockhash(recentBlockhash ?: rpcBlockhashRepository.getRecentBlockhash().recentBlockhash)
        transaction.setFeePayer(feePayer)

        // calculate fee first
        val expectedFee = FeeAmount(
            transactionFee = transaction.calculateTransactionFee(actualLamportsPerSignature),
            accountCreationFee = accountsCreationFee
        )

        // resign transaction
        transaction.sign(signers)
        return PreparedTransaction(transaction, signers, expectedFee)
    }

    suspend fun serializeAndSend(
        transaction: Transaction,
        isSimulation: Boolean
    ): String {

        return if (isSimulation) {
            rpcTransactionRepository.simulateTransaction(transaction)
        } else {
            rpcTransactionRepository.sendTransaction(transaction)
        }
    }

    fun signGeneratedTransaction(
        signer: Account,
        generatedTransaction: GeneratedTransaction,
    ): ByteArray {
        val transactionData = generatedTransaction.transaction.decodeToBytes()
        // decode the number of signatures
        val existingSignatures = ShortvecEncoding.decodeLength(transactionData)
        require(existingSignatures == 2) {
            "Pre-generated transaction must contain exactly 2 signatures: 1 fee payer, 2nd - ours (to be replaced)"
        }

        // calculate offsets, assuming it's 1 byte for <= 127 signatures
        val signaturesOffset = 1
        // message goes right after signatures count length and signatures itself (64 bytes each)
        val messageOffset = signaturesOffset + existingSignatures * Transaction.SIGNATURE_LENGTH

        // extract and keep all existing signatures
        val signatures = (0 until existingSignatures).map { i ->
            transactionData.copyOfRange(
                signaturesOffset + i * Transaction.SIGNATURE_LENGTH,
                signaturesOffset + (i + 1) * Transaction.SIGNATURE_LENGTH
            )
        }.toMutableList()

        // extract the message part
        val message = transactionData.copyOfRange(messageOffset, transactionData.size)

        // create a new signature for the message part
        val signatureProvider = TweetNaclFast.Signature(ByteArray(0), signer.keypair)
        val newSignature = signatureProvider.detached(message)

        // replace the last signature (supposed to be replaced as send-service generates invalid sig for us)
        // with the new one
        signatures[signatures.size - 1] = newSignature

        // allocate buffer for the final transaction
        val capacity = signaturesOffset + signatures.size * Transaction.SIGNATURE_LENGTH + message.size
        val out = ByteBuffer.allocate(capacity)

        // put the signatures count, signatures, and message into the buffer
        out.put(ShortvecEncoding.encodeLength(signatures.size))
        signatures.forEach(out::put)
        out.put(message)

        return out.array()
    }

    suspend fun sendTransaction(
        signedTransaction: Base64String,
        isSimulation: Boolean,
        preflightCommitment: ConfirmationStatus = ConfirmationStatus.CONFIRMED
    ): String {
        return if (isSimulation) {
            rpcTransactionRepository.simulateTransaction(signedTransaction.base64Value)
        } else {
            rpcTransactionRepository.sendTransaction(
                signedTransaction.base64Value,
                preflightCommitment
            )
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
            setRecentBlockhash(blockhash)
            sign(signers)
        }

        val serializedMessage = transaction.serialize()
        val serializedTransaction = Base64Utils.encode(serializedMessage)

        Timber.d("Serialized transaction: $serializedTransaction")
        return serializedTransaction
    }
}
