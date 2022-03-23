package org.p2p.wallet.feerelayer.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.wallet.feerelayer.api.FeeRelayerApi
import org.p2p.wallet.feerelayer.api.FeeRelayerDevnetApi
import org.p2p.wallet.feerelayer.api.RelayTopUpSwapRequest
import org.p2p.wallet.feerelayer.api.SendTransactionRequest
import org.p2p.wallet.feerelayer.model.FeeRelayerConverter
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.SwapDataConverter
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerRemoteRepository(
    private val api: FeeRelayerApi,
    private val devnetApi: FeeRelayerDevnetApi,
    private val environmentManager: EnvironmentManager
) : FeeRelayerRepository {

    override suspend fun getFeePayerPublicKey(): PublicKey {
        return if (environmentManager.isDevnet()) {
            devnetApi.getPublicKeyV2().toPublicKey()
        } else {
            api.getPublicKey().toPublicKey()
        }
    }

    override suspend fun getFreeFeeLimits(owner: String): FreeTransactionFeeLimit {
        val response = if (environmentManager.isDevnet()) {
            devnetApi.getFreeFeeLimits(owner)
        } else {
            api.getFreeFeeLimits(owner)
        }

        return FreeTransactionFeeLimit(
            maxUsage = response.limits.maxCount,
            currentUsage = response.processedFee.count,
            maxAmount = response.limits.maxAmount,
            amountUsed = response.processedFee.totalAmount
        )
    }

    override suspend fun relayTransaction(transaction: Transaction): List<String> {
        val instructions = transaction.instructions
        val signatures = transaction.allSignatures
        val pubkeys = transaction.accountKeys
        val blockHash = transaction.recentBlockHash

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
        return if (environmentManager.isDevnet()) {
            devnetApi.relayTransactionV2(request)
        } else {
            api.relayTransaction(request)
        }
    }

    override suspend fun relayTopUpSwap(
        userSourceTokenAccountPubkey: String,
        sourceTokenMintPubkey: String,
        userAuthorityPubkey: String,
        swapData: SwapData,
        feeAmount: BigInteger,
        signatures: SwapTransactionSignatures,
        blockhash: String
    ): List<String> {
        val request = RelayTopUpSwapRequest(
            userSourceTokenAccountPubkey = userSourceTokenAccountPubkey,
            sourceTokenMintPubkey = sourceTokenMintPubkey,
            userAuthorityPubkey = userAuthorityPubkey,
            topUpSwap = SwapDataConverter.toNetwork(swapData),
            feeAmount = feeAmount.toLong(),
            signatures = FeeRelayerConverter.toNetwork(signatures),
            blockhash = blockhash,
        )

        return if (environmentManager.isDevnet()) {
            devnetApi.relayTopUpSwapV2(request)
        } else {
            api.relayTopUpSwap(request)
        }
    }
}
