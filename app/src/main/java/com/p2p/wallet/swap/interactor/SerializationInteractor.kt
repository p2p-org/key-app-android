package com.p2p.wallet.swap.interactor

import com.p2p.wallet.common.crypto.Base64Utils
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.Transaction
import org.p2p.solanaj.model.core.TransactionInstruction
import timber.log.Timber

class SerializationInteractor(
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
        val transaction = Transaction(
            feePayer = feePayerPublicKey,
            recentBlockhash = blockhash,
            instructions = instructions as MutableList<TransactionInstruction>
        )

        transaction.sign(signers)
        val serializedMessage = transaction.serialize()
        val serializedTransaction: String = Base64Utils.encode(serializedMessage)

        Timber.d("Serialized transaction: $serializedTransaction")
        return serializedTransaction
    }
}