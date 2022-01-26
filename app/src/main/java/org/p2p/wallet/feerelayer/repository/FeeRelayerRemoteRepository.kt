package org.p2p.wallet.feerelayer.repository

import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Signature
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.feerelayer.FeeRelayerConverter
import org.p2p.wallet.feerelayer.api.FeeRelayerApi
import org.p2p.wallet.feerelayer.api.RelayTopUpSwapRequest
import org.p2p.wallet.feerelayer.api.SendTransactionRequest
import org.p2p.wallet.feerelayer.api.TopUpSwapRequest
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import org.p2p.wallet.feerelayer.model.TopUpSwap
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.utils.toPublicKey

class FeeRelayerRemoteRepository(
    private val api: FeeRelayerApi,
    private val environmentManager: EnvironmentManager
) : FeeRelayerRepository {

    override suspend fun getPublicKey(): PublicKey {
        val environment = environmentManager.loadEnvironment()
        return if (environment == Environment.DEVNET) {
            api.getPublicKeyV2().toPublicKey()
        } else {
            api.getPublicKey().toPublicKey()
        }
    }

    override suspend fun relayTopUpSwap(
        userSourceTokenAccountPubkey: String,
        sourceTokenMintPubkey: String,
        userAuthorityPubkey: String,
        topUpSwap: TopUpSwap,
        feeAmount: Long,
        signatures: SwapTransactionSignatures,
        blockhash: String
    ): List<String> {

        return emptyList()
//        val request = RelayTopUpSwapRequest(
//            userSourceTokenAccountPubkey = userSourceTokenAccountPubkey,
//            sourceTokenMintPubkey = sourceTokenMintPubkey,
//            userAuthorityPubkey = userAuthorityPubkey,
//        )

//        val environment = environmentManager.loadEnvironment()
//        return if (environment == Environment.DEVNET) {
//            api.relayTopUpSwapV2(request)
//        } else {
//            api.relayTopUpSwap(request)
//        }
    }

    override suspend fun relayTransaction(
        instructions: List<TransactionInstruction>,
        signatures: List<Signature>,
        pubkeys: List<AccountMeta>,
        blockHash: String
    ): List<String> {
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
        val environment = environmentManager.loadEnvironment()
        return if (environment == Environment.DEVNET) {
            api.relayTransactionV2(request)
        } else {
            api.relayTransaction(request)
        }
    }
}