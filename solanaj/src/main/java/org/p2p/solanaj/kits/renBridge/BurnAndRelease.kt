package org.p2p.solanaj.kits.renBridge

import org.bitcoinj.core.Base58
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.renBridge.renVM.RenVMProvider
import org.p2p.solanaj.rpc.RpcException
import org.p2p.solanaj.rpc.RpcSolanaInteractor
import org.p2p.solanaj.utils.Hash
import org.p2p.solanaj.utils.Utils
import org.p2p.solanaj.utils.crypto.Hex
import java.math.BigInteger
import java.nio.ByteBuffer

class BurnAndRelease(
    private val renVMProvider: RenVMProvider,
    private val rpcSolanaInteractor: RpcSolanaInteractor,
    private val state: LockAndMint.State
) {

    private var nonceBuffer: ByteArray = byteArrayOf()
    private var recepient: String = ""

    suspend fun submitBurnTransaction(account: PublicKey, amount: String, recepient: String, signer: Account): BurnDetails {
        this.recepient = recepient
        return rpcSolanaInteractor.submitBurn(account, amount, recepient, signer)
    }

    fun getBurnState(burnDetails: BurnDetails, amount: String): LockAndMint.State {
        val txId = Base58.decode(burnDetails.confirmedSignature)
        nonceBuffer = getNonceBuffer(burnDetails.nonce)
        val nHash = Hash.generateNHash(nonceBuffer, txId, "0")
        val pHash = Hash.generatePHash()
        val sHash = Hash.generateSHash("BTC/toBitcoin")
        val gHash =
            Hash.generateGHash(Hex.encode(Utils.addressToBytes(burnDetails.recepient)), Hex.encode(sHash), nonceBuffer)

        val txHash = renVMProvider.getTxHash(
            selector = RenVMProvider.BURN_SELECTOR,
            gHash = gHash,
            gPubKey = byteArrayOf(),
            nHash = nHash,
            nonce = nonceBuffer,
            amount = amount,
            pHash = pHash,
            to = burnDetails.recepient,
            txIndex = "0",
            txId = txId
        )

        with(state) {
            this.txIndex = "0"
            this.amount = amount
            this.nHash = nHash
            this.txId = txId
            this.pHash = pHash
            this.gHash = gHash
            this.txHash = txHash
            this.gPubKey = byteArrayOf()
        }
        return state
    }

    @Throws(RpcException::class)
    suspend fun release(): String {
        return renVMProvider.submit(
            selector = RenVMProvider.BURN_SELECTOR,
            gHash = state.gHash,
            gPubKey = state.gPubKey,
            nHash = state.nHash,
            nonce = nonceBuffer,
            amount = state.amount,
            pHash = state.pHash,
            to = recepient,
            txIndex = state.txIndex,
            txId = state.txId
        )
    }

    private fun getNonceBuffer(nonce: BigInteger): ByteArray {
        val amountBytes = nonce.toByteArray()
        val amountBuffer = ByteBuffer.allocate(32)
        amountBuffer.position(32 - amountBytes.size)
        amountBuffer.put(amountBytes)
        return amountBuffer.array()
    }
}
