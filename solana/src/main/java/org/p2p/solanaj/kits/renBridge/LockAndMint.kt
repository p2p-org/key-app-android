package org.p2p.solanaj.kits.renBridge

import org.bitcoinj.core.Base58
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.renBridge.renVM.RenVMRepository
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.script.Script
import org.p2p.core.network.environment.RpcEnvironment
import org.p2p.solanaj.rpc.RpcSolanaInteractor
import org.p2p.solanaj.utils.Hash
import org.p2p.solanaj.utils.Utils
import org.p2p.core.crypto.Hex
import java.math.BigInteger
import java.util.concurrent.TimeUnit

class LockAndMint(
    private val renVMRepository: RenVMRepository,
    private val session: Session,
    private val solanaChain: RpcSolanaInteractor,
    private val state: State = State(),
) {

    companion object {
        fun buildSession(
            renVMRepository: RenVMRepository,
            session: Session,
            solanaChain: RpcSolanaInteractor,
            state: State
        ) = LockAndMint(renVMRepository, session, solanaChain, state)

        fun getSession(
            renVMRepository: RenVMRepository,
            session: Session,
            solanaChain: RpcSolanaInteractor,
            state: State
        ): LockAndMint = LockAndMint(renVMRepository, session, solanaChain, state)
    }

    suspend fun generateGatewayAddress(environment: RpcEnvironment): String {
        val sendTo = solanaChain.getAssociatedTokenAddress(session.destinationAddress)
        state.sendTo = sendTo

        val sendToHex = Hex.encode(sendTo?.asByteArray() ?: byteArrayOf())
        val tokenGatewayContractHex = Hex.encode(Hash.generateSHash())
        val gHash = Hash.generateGHash(sendToHex, tokenGatewayContractHex, Hex.decode(session.nonce))
        state.gHash = gHash

        val gPubKey = renVMRepository.selectPublicKey()
        state.gPubKey = gPubKey

        val gatewayAddress =
            Script.createAddressByteArray(Hash.hash160(gPubKey), gHash, byteArrayOf(environment.p2shPrefix.toByte()))

        session.gatewayAddress = Base58.encode(gatewayAddress)
        return session.gatewayAddress
    }

    fun getDepositState(transactionHash: String, txIndex: String, amount: String): State {
        val nonce: ByteArray = Hex.decode(session.nonce)
        val txId: ByteArray = Hex.decode(Utils.reverseHex(transactionHash))
        val nHash: ByteArray = Hash.generateNHash(nonce, txId, txIndex)
        val pHash: ByteArray = Hash.generatePHash()

        val txHash = renVMRepository.getTxHash(
            selector = RenVMRepository.MINT_SELECTOR,
            gHash = state.gHash,
            gPubKey = state.gPubKey,
            nHash = nHash,
            nonce = nonce,
            amount = amount,
            pHash = pHash,
            state.sendTo?.toBase58().orEmpty(),
            txIndex = txIndex,
            txId = txId
        )

        with(state) {
            this.txIndex = txIndex
            this.amount = amount
            this.nHash = nHash
            this.txId = txId
            this.pHash = pHash
            this.txHash = txHash
        }
        return state
    }

    suspend fun submitMintTransaction(): String = renVMRepository.submit(
        selector = RenVMRepository.MINT_SELECTOR,
        gHash = state.gHash,
        gPubKey = state.gPubKey,
        nHash = state.nHash,
        nonce = Hex.decode(session.nonce),
        amount = state.amount,
        pHash = state.pHash,
        to = state.sendTo?.toBase58().orEmpty(),
        txIndex = state.txIndex,
        txId = state.txId
    )

    suspend fun mint(signer: Account): String {
        val destination =
            session.destinationAddress ?: throw IllegalStateException("Destination address cannot be null")

        val responseQueryMint = renVMRepository.getQueryMint(state.txHash)
        return solanaChain.submitMint(destination, signer, responseQueryMint)
    }

    suspend fun lockAndMint(txHash: String): ResponseQueryTxMint =
        renVMRepository.getQueryMint(txHash)

    suspend fun estimateTransactionFee(): BigInteger {
        val fee = renVMRepository.estimateTransactionFee()
        session.fee = fee
        return fee
    }

    fun getSession() = session

    class Session {

        var destinationAddress: PublicKey? = null
        var nonce: String = ""
        var createdAt: Long = 0
        var expiryTime: Long = 0
        var gatewayAddress: String = ""
        var fee: BigInteger = BigInteger.ZERO

        constructor(destinationAddress: PublicKey?) {
            this.destinationAddress = destinationAddress
            nonce = Utils.generateNonce()
            createdAt = System.currentTimeMillis()
            expiryTime = Utils.getSessionExpiry()
        }

        constructor(
            destinationAddress: PublicKey?,
            nonce: String?,
            createdAt: Long,
            expiryTime: Long,
            gatewayAddress: String?
        ) {
            this.destinationAddress = destinationAddress
            this.nonce = nonce!!
            this.createdAt = createdAt
            this.expiryTime = expiryTime
            this.gatewayAddress = gatewayAddress!!
        }

        constructor(
            destinationAddress: PublicKey?,
            nonce: String?,
            createdAt: Long,
            expiryTime: Long,
            gatewayAddress: String?,
            fee: BigInteger?
        ) {
            this.destinationAddress = destinationAddress
            this.nonce = nonce!!
            this.createdAt = createdAt
            this.expiryTime = expiryTime
            this.gatewayAddress = gatewayAddress!!
            this.fee = fee!!
        }

        val isValid: Boolean
            get() {
                val dayInMillis = TimeUnit.DAYS.toMillis(1)
                val currentTime = System.currentTimeMillis()
                /* We should subtract one day from expiry time to make it valid  */
                return currentTime < expiryTime - dayInMillis
            }
    }

    class State {
        var gHash = byteArrayOf()
        var gPubKey = byteArrayOf()
        var sendTo: PublicKey? = null
        var txId = byteArrayOf()
        var nHash = byteArrayOf()
        var pHash = byteArrayOf()
        var txHash = ""
        var txIndex = ""
        var amount = ""
    }
}
