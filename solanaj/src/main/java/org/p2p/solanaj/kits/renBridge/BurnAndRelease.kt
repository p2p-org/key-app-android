package org.p2p.solanaj.kits.renBridge

import org.bitcoinj.core.Base58
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.renBridge.SolanaChain.BurnDetails
import org.p2p.solanaj.kits.renBridge.renVM.RenVMProvider
import org.p2p.solanaj.rpc.BlockChainRepository
import org.p2p.solanaj.rpc.RpcException
import org.p2p.solanaj.utils.Hash
import org.p2p.solanaj.utils.Utils
import org.p2p.solanaj.utils.crypto.Hex.encode
import java.math.BigInteger
import java.nio.ByteBuffer

class BurnAndRelease(
    private val blockChainRepository: BlockChainRepository,
    private val networkConfig: NetworkConfig
) {
    private val renVMProvider: RenVMProvider = RenVMProvider(blockChainRepository)
    private val state = LockAndMint.State()
    private var nonceBuffer: ByteArray = byteArrayOf()
    private var recepient: String? = null
    private lateinit var solanaChain: SolanaChain

    @Throws(Exception::class)
    suspend fun submitBurnTransaction(
        account: PublicKey?,
        amount: String?,
        recepient: String?,
        signer: Account
    ): BurnDetails {
        this.recepient = recepient
        solanaChain = SolanaChain.create(
            blockChainRepository,
            networkConfig
        )
        return solanaChain.submitBurn(account, amount, recepient, signer)
    }

    @Throws(Exception::class)
    fun getBurnState(burnDetails: BurnDetails, amount: String?): LockAndMint.State {
        val txid = Base58.decode(burnDetails.confirmedSignature)
        nonceBuffer = getNonceBuffer(burnDetails.nonce)
        val nHash = Hash.generateNHash(nonceBuffer, txid, "0")
        val pHash = Hash.generatePHash()
        val sHash = Hash.generateSHash("BTC/toBitcoin")
        val gHash = Hash.generateGHash(
            encode(Utils.addressToBytes(burnDetails.recepient)), encode(sHash),
            nonceBuffer
        )
        val txHash = renVMProvider.burnTxHash(
            gHash, byteArrayOf(), nHash, nonceBuffer, amount, pHash,
            burnDetails.recepient, "0", txid
        )
        state.txIndex = "0"
        state.amount = amount
        state.nHash = nHash
        state.txid = txid
        state.pHash = pHash
        state.gHash = gHash
        state.txHash = txHash
        state.gPubKey = byteArrayOf()
        return state
    }

    @Throws(RpcException::class)
    suspend fun release(): String {
        return renVMProvider.submitBurn(
            state.gHash, state.gPubKey, state.nHash, nonceBuffer, state.amount,
            state.pHash, recepient, state.txIndex, state.txid
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