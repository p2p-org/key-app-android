package org.p2p.solanaj.kits.renBridge.renVM

import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryBlockState
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryConfig
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseSubmitTxMint
import org.p2p.solanaj.rpc.RenPoolRepository
import org.p2p.solanaj.rpc.RpcException
import org.p2p.solanaj.utils.Utils
import org.p2p.core.crypto.Base64UrlUtils
import java.math.BigInteger

const val MINT_TRANSACTION_INPUT =
    "aHQBEVgedhqiYDUtzYKdu1Qg1fc781PEV4D1gLsuzfpHNwH8yK2A2BuZK4uZoMC6" +
        "pp8o7GWQxmsp52gsDrfbipkyeQZnXigCmscJY4aJDxF9tT8DQP3XRa1cBzQL8S8PTzi9nPnBkAxBhtNv6q1"

class RenVMRepository(
    private val rpcSolanaApi: RenPoolRepository
) {

    companion object {
        const val MINT_SELECTOR = "BTC/toSolana"
        const val BURN_SELECTOR = "BTC/fromSolana"
    }

    @Throws(RpcException::class)
    suspend fun getQueryMint(txHash: String): ResponseQueryTxMint =
        rpcSolanaApi.getQueryMint(txHash)

    @Throws(RpcException::class)
    suspend fun getQueryBlockState(): ResponseQueryBlockState =
        rpcSolanaApi.getQueryBlockState()

    @Throws(RpcException::class)
    suspend fun getQueryConfig(): ResponseQueryConfig = rpcSolanaApi.getQueryConfig()

    @Throws(RpcException::class)
    suspend fun submitTx(
        hash: String,
        mintTx: ParamsSubmitMint.MintTransactionInput,
        selector: String
    ): ResponseSubmitTxMint = rpcSolanaApi.submitTx(hash, mintTx, selector)

    @Throws(RpcException::class)
    suspend fun selectPublicKey(): ByteArray {
        val pubKey = getQueryBlockState().pubKey
        return Base64UrlUtils.fromURLBase64(pubKey)
    }

    suspend fun submit(
        selector: String,
        gHash: ByteArray,
        gPubKey: ByteArray,
        nHash: ByteArray,
        nonce: ByteArray,
        amount: String,
        pHash: ByteArray,
        to: String,
        txIndex: String,
        txId: ByteArray
    ): String {
        val mintTx = createMintTransaction(txId, txIndex, gHash, gPubKey, nHash, nonce, pHash, to, amount)
        val mintTransactionHash = mintTx.hashTransactionMint(selector)
        val hash = Utils.toURLBase64(mintTransactionHash)

        submitTx(hash, mintTx, selector)
        return hash
    }

    fun getTxHash(
        selector: String,
        gHash: ByteArray,
        gPubKey: ByteArray,
        nHash: ByteArray,
        nonce: ByteArray,
        amount: String,
        pHash: ByteArray,
        to: String,
        txIndex: String,
        txId: ByteArray
    ): String {
        val mintTx = createMintTransaction(
            gHash = gHash,
            gPubKey = gPubKey,
            nHash = nHash,
            nonce = nonce,
            amount = amount,
            pHash = pHash,
            to = to,
            txIndex = txIndex,
            txId = txId
        )
        val mintTxHash = mintTx.hashTransactionMint(selector)
        return Utils.toURLBase64(mintTxHash)
    }

    private fun createMintTransaction(
        txId: ByteArray,
        txIndex: String,
        gHash: ByteArray,
        gPubKey: ByteArray,
        nHash: ByteArray,
        nonce: ByteArray,
        pHash: ByteArray,
        to: String,
        amount: String
    ): ParamsSubmitMint.MintTransactionInput {
        val mintTx = ParamsSubmitMint.MintTransactionInput()
        with(mintTx) {
            this.txid = Utils.toURLBase64(txId)
            this.txindex = txIndex
            this.ghash = Utils.toURLBase64(gHash)
            this.gpubkey = if (gPubKey.isEmpty()) "" else Utils.toURLBase64(gPubKey)
            this.nhash = Utils.toURLBase64(nHash)
            this.nonce = Utils.toURLBase64(nonce)
            this.phash = Utils.toURLBase64(pHash)
            this.to = to
            this.amount = amount
        }
        return mintTx
    }

    suspend fun estimateTransactionFee(): BigInteger {
        val queryBlockState = getQueryBlockState()
        return BigInteger(queryBlockState.state.v.btc.gasLimit).multiply(
            BigInteger(queryBlockState.state.v.btc.gasCap)
        )
    }
}
