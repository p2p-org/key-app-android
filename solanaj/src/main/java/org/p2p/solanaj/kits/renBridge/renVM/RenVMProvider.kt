package org.p2p.solanaj.kits.renBridge.renVM

import org.bitcoinj.core.Base58
import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint.MintTransactionInput
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryBlockState
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseSubmitTxMint
import org.p2p.solanaj.rpc.BlockChainRepository
import org.p2p.solanaj.rpc.RpcException
import org.p2p.solanaj.utils.ByteUtils
import org.p2p.solanaj.utils.Hash
import org.p2p.solanaj.utils.Utils
import org.p2p.solanaj.utils.crypto.Base64UrlUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger

private const val HASH_TRANSACTION_INPUT = "aHQBEVgedhqiYDUtzYKdu1Qg1fc781PEV4D1gLsuzfpHNwH8yK2A2" +
    "BuZK4uZoMC6pp8o7GWQxmsp52gsDrfbipkyeQZnXigCmscJY4aJDxF9tT8DQP3XRa1cBzQL8S8PTzi9nPnBkAxBhtNv6q1"

class RenVMProvider(private val blockChainRepository: BlockChainRepository) {

    @Throws(RpcException::class)
    suspend fun queryMint(txHash: String): ResponseQueryTxMint = blockChainRepository.queryMint(txHash)

    @Throws(RpcException::class)
    suspend fun queryBlockState(): ResponseQueryBlockState = blockChainRepository.queryBlockState()

    @Throws(RpcException::class)
    suspend fun submitTx(
        hash: String?,
        mintTx: MintTransactionInput?,
        selector: String?
    ): ResponseSubmitTxMint = blockChainRepository.submitTx(hash, mintTx, selector)

    @Throws(RpcException::class)
    suspend fun selectPublicKey(): ByteArray {
        val pubKey = queryBlockState().pubKey
        return Base64UrlUtils.fromURLBase64(pubKey)
    }

    @Throws(RpcException::class)
    suspend fun submitMint(
        gHash: ByteArray?,
        gPubKey: ByteArray,
        nHash: ByteArray?,
        nonce: ByteArray?,
        amount: String?,
        pHash: ByteArray?,
        to: String?,
        txIndex: String?,
        txid: ByteArray?
    ): String {
        val selector = "BTC/toSolana"
        val mintTx = buildTransaction(gHash, gPubKey, nHash, nonce, amount, pHash, to, txIndex, txid)
        val hash = Utils.toURLBase64(hashTransactionMint(mintTx, selector))
        submitTx(hash, mintTx, selector)
        return hash
    }

    @Throws(RpcException::class)
    suspend fun submitBurn(
        gHash: ByteArray?,
        gPubKey: ByteArray,
        nHash: ByteArray?,
        nonce: ByteArray?,
        amount: String?,
        pHash: ByteArray?,
        to: String?,
        txIndex: String?,
        txid: ByteArray?
    ): String {
        val selector = "BTC/fromSolana"
        val mintTx = buildTransaction(gHash, gPubKey, nHash, nonce, amount, pHash, to, txIndex, txid)
        val hash = Utils.toURLBase64(hashTransactionMint(mintTx, selector))
        submitTx(hash, mintTx, selector)
        return hash
    }

    fun mintTxHash(
        gHash: ByteArray?,
        gPubKey: ByteArray,
        nHash: ByteArray?,
        nonce: ByteArray?,
        amount: String?,
        pHash: ByteArray?,
        to: String?,
        txIndex: String?,
        txid: ByteArray?
    ): String {
        val mintTx = buildTransaction(gHash, gPubKey, nHash, nonce, amount, pHash, to, txIndex, txid)
        return Utils.toURLBase64(hashTransactionMint(mintTx, "BTC/toSolana"))
    }

    fun burnTxHash(
        gHash: ByteArray?,
        gPubKey: ByteArray,
        nHash: ByteArray?,
        nonce: ByteArray?,
        amount: String?,
        pHash: ByteArray?,
        to: String?,
        txIndex: String?,
        txid: ByteArray?
    ): String {
        val burnTx = buildTransaction(gHash, gPubKey, nHash, nonce, amount, pHash, to, txIndex, txid)
        return Utils.toURLBase64(hashTransactionMint(burnTx, "BTC/fromSolana"))
    }

    @Throws(RpcException::class)
    suspend fun estimateTransactionFee(): BigInteger {
        val queryBlockState = queryBlockState()
        return BigInteger(queryBlockState.state.v.btc.gasLimit)
            .multiply(BigInteger(queryBlockState.state.v.btc.gasCap))
    }

    companion object {
        fun buildTransaction(
            gHash: ByteArray?,
            gPubKey: ByteArray,
            nHash: ByteArray?,
            nonce: ByteArray?,
            amount: String?,
            pHash: ByteArray?,
            to: String?,
            txIndex: String?,
            txid: ByteArray?
        ): MintTransactionInput {
            val mintTx = MintTransactionInput()
            mintTx.txid = Utils.toURLBase64(txid)
            mintTx.txindex = txIndex
            mintTx.ghash = Utils.toURLBase64(gHash)
            mintTx.gpubkey = if (gPubKey.isEmpty()) "" else Utils.toURLBase64(gPubKey)
            mintTx.nhash = Utils.toURLBase64(nHash)
            mintTx.nonce = Utils.toURLBase64(nonce)
            mintTx.phash = Utils.toURLBase64(pHash)
            mintTx.to = to
            mintTx.amount = amount
            return mintTx
        }

        // txHash
        @JvmStatic
        fun hashTransactionMint(mintTx: MintTransactionInput, selector: String): ByteArray {
            val out = ByteArrayOutputStream()
            val version = "1"
            try {
                // todo: it was out.writeBytes() workaround
                out.write(marshalString(version))
                out.write(marshalString(selector))

                // marshalledType MintTransactionInput
                out.write(
                    Base58.decode(
                        HASH_TRANSACTION_INPUT
                    )
                )
                out.write(marshalBytes(Utils.fromURLBase64(mintTx.txid)))
                out.write(ByteUtils.uint32ToByteArrayBE(mintTx.txindex.toLong()))
                out.write(Utils.amountToUint256ByteArrayBE(mintTx.amount))
                out.write(byteArrayOf(0, 0, 0, 0))
                out.write(Utils.fromURLBase64(mintTx.phash))
                out.write(marshalString(mintTx.to))
                out.write(Utils.fromURLBase64(mintTx.nonce))
                out.write(Utils.fromURLBase64(mintTx.nhash))
                out.write(marshalBytes(Utils.fromURLBase64(mintTx.gpubkey)))
                out.write(Utils.fromURLBase64(mintTx.ghash))
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return Hash.sha256(out.toByteArray())
        }

        private fun marshalString(src: String): ByteArray {
            return marshalBytes(src.toByteArray())
        }

        private fun marshalBytes(`in`: ByteArray): ByteArray {
            val out = ByteArray(ByteUtils.UINT_32_LENGTH + `in`.size)
            System.arraycopy(ByteUtils.uint32ToByteArrayBE(`in`.size.toLong()), 0, out, 0, ByteUtils.UINT_32_LENGTH)
            System.arraycopy(`in`, 0, out, ByteUtils.UINT_32_LENGTH, `in`.size)
            return out
        }
    }
}