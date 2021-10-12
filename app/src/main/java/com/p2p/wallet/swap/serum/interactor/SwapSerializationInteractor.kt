package com.p2p.wallet.swap.serum.interactor

import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.utils.crypto.Base64Utils
import timber.log.Timber

class SwapSerializationInteractor(
    private val rpcRepository: RpcRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun sendTransaction(serializedTransaction: String, isSimulation: Boolean): String =
        if (isSimulation) {
            rpcRepository.simulateTransaction(serializedTransaction)
        } else {
            rpcRepository.sendTransaction(serializedTransaction)
        }

    suspend fun serializeTransaction(
        instructions: List<TransactionInstruction>,
        recentBlockhash: String? = null,
        signers: List<Account>,
        feePayer: PublicKey? = null
    ): String {
        // get recentBlockhash
        val blockhash = if (recentBlockhash.isNullOrEmpty()) {
            rpcRepository.getRecentBlockhash().recentBlockhash
        } else {
            recentBlockhash
        }

        val accountPublicKey = tokenKeyProvider.publicKey.toPublicKey()
        val feePayerPublicKey = feePayer ?: accountPublicKey

        // serialize transaction
        val transaction = Transaction()
        transaction.addInstructions(instructions)
        transaction.setFeePayer(feePayerPublicKey)
        transaction.setRecentBlockHash(blockhash)
        transaction.sign(signers)

        val serializedMessage = transaction.serialize()
        val serializedTransaction = Base64Utils.encode(serializedMessage)

        Timber.d("Serialized transaction: $serializedTransaction")
        return serializedTransaction
    }
}