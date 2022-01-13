package org.p2p.wallet.rpc.repository

import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Signature
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.wallet.rpc.api.FeeRelayerApi
import org.p2p.wallet.rpc.api.FeeSolTransferRequest
import org.p2p.wallet.rpc.api.FeeSplTransferRequest
import org.p2p.wallet.rpc.api.SendTransactionRequest
import org.p2p.wallet.rpc.model.FeeRelayerConverter
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigInteger

class FeeRelayerRemoteRepository(
    private val api: FeeRelayerApi
) : FeeRelayerRepository {

    override suspend fun getPublicKey(): PublicKey = api.getPublicKey().toPublicKey()

    override suspend fun send(
        instructions: List<TransactionInstruction>,
        signatures: List<Signature>,
        pubkeys: List<AccountMeta>,
        blockHash: String
    ) {
        val keys = pubkeys.map { it.publicKey.toBase58() }
        val requestInstructions = instructions.map { FeeRelayerConverter.toNetwork(it, keys) }

        val relayTransactionSignatures = mutableMapOf<Int, String>()
        signatures.forEach { signature ->
            val index = pubkeys.indexOfFirst { it.publicKey.toBase58() == signature.publicKey.toBase58() }
            relayTransactionSignatures[index] = signature.signature
        }

        val request = SendTransactionRequest(
            instructions = requestInstructions,
            signatures = relayTransactionSignatures,
            pubkeys = keys,
            blockHash = blockHash,
        )
        val response = api.send(request)
    }

    override suspend fun sendSolToken(
        senderPubkey: String,
        recipientPubkey: String,
        lamports: BigInteger,
        signature: String,
        blockhash: String
    ): String {
        val request = FeeSolTransferRequest(
            senderPubkey = senderPubkey,
            recipientPubkey = recipientPubkey,
            lamports = lamports,
            signature = signature,
            blockhash = blockhash
        )

        return api.sendSolToken(request).first()
    }

    override suspend fun sendSplToken(
        senderTokenAccountPubkey: String,
        recipientPubkey: String,
        tokenMintPubkey: String,
        authorityPubkey: String,
        lamports: BigInteger,
        decimals: Int,
        signature: String,
        blockhash: String
    ): String {

        val request = FeeSplTransferRequest(
            senderTokenAccountPubkey = senderTokenAccountPubkey,
            recipientPubkey = recipientPubkey,
            tokenMintPubkey = tokenMintPubkey,
            authorityPubkey = authorityPubkey,
            lamports = lamports,
            decimals = decimals,
            signature = signature,
            blockhash = blockhash
        )

        return api.sendSplToken(request).first()
    }
}