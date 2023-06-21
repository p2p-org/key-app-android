package org.p2p.wallet.feerelayer.repository

import java.math.BigInteger
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.feerelayer.api.FeeRelayerApi
import org.p2p.wallet.feerelayer.api.FeeRelayerDevnetApi
import org.p2p.wallet.feerelayer.api.RelayTopUpSwapRequest
import org.p2p.wallet.feerelayer.api.SendTransactionRequest
import org.p2p.wallet.feerelayer.api.SignTransactionRequest
import org.p2p.wallet.feerelayer.model.FeeRelayerConverter
import org.p2p.wallet.feerelayer.model.FeeRelayerLimitsConverter
import org.p2p.wallet.feerelayer.model.FeeRelayerSignTransaction
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.SwapDataConverter
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.utils.toPublicKey

class FeeRelayerRemoteRepository(
    private val api: FeeRelayerApi,
    private val devnetApi: FeeRelayerDevnetApi,
    private val environmentManager: NetworkEnvironmentManager
) : FeeRelayerRepository {

    private var feeRelayerPublicKey: PublicKey? = null

    override suspend fun getFeePayerPublicKey(): PublicKey {
        if (feeRelayerPublicKey == null) {
            feeRelayerPublicKey = if (environmentManager.isDevnet()) {
                devnetApi.getPublicKeyV2().toPublicKey()
            } else {
                api.getPublicKey().toPublicKey()
            }
        }

        return feeRelayerPublicKey!!
    }

    override suspend fun getFreeFeeLimits(owner: String): TransactionFeeLimits {
        val response = if (environmentManager.isDevnet()) {
            devnetApi.getFreeFeeLimits(owner)
        } else {
            api.getFreeFeeLimits(owner)
        }

        return FeeRelayerLimitsConverter.fromNetwork(response)
    }

    override suspend fun signTransaction(
        transaction: Base64String,
        statistics: FeeRelayerStatistics
    ): FeeRelayerSignTransaction {
        val infoRequest = FeeRelayerConverter.toNetwork(statistics)

        val request = SignTransactionRequest(
            transaction = transaction,
            info = infoRequest
        )
        return if (environmentManager.isDevnet()) {
            devnetApi.signTransactionV2(request)
        } else {
            api.signRelayTransaction(request)
        }.let { response ->
            FeeRelayerSignTransaction(
                signature = response.signature,
                transaction = response.transaction
            )
        }
    }

    override suspend fun relayTransaction(transaction: Transaction, statistics: FeeRelayerStatistics): List<String> {
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

        val infoRequest = FeeRelayerConverter.toNetwork(statistics)

        val request = SendTransactionRequest(
            instructions = requestInstructions,
            signatures = relayTransactionSignatures,
            pubkeys = keys,
            blockHash = blockHash,
            info = infoRequest
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
        blockhash: String,
        info: FeeRelayerStatistics
    ): List<String> {
        val request = RelayTopUpSwapRequest(
            userSourceTokenAccountPubkey = userSourceTokenAccountPubkey,
            sourceTokenMintPubkey = sourceTokenMintPubkey,
            userAuthorityPubkey = userAuthorityPubkey,
            topUpSwap = SwapDataConverter.toNetwork(swapData),
            feeAmount = feeAmount.toLong(),
            signatures = FeeRelayerConverter.toNetwork(signatures),
            blockhash = blockhash,
            info = FeeRelayerConverter.toNetwork(info)
        )

        return if (environmentManager.isDevnet()) {
            devnetApi.relayTopUpSwapV2(request)
        } else {
            api.relayTopUpSwap(request)
        }
    }
}
