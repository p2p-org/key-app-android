package org.p2p.solanaj.kits.renBridge

import org.bitcoinj.core.Base58
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.renBridge.renVM.RenVMProvider
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.script.Script
import org.p2p.solanaj.rpc.BlockChainRepository
import org.p2p.solanaj.rpc.RpcException
import org.p2p.solanaj.utils.Hash
import org.p2p.solanaj.utils.Utils
import org.p2p.solanaj.utils.crypto.Hex.decode
import org.p2p.solanaj.utils.crypto.Hex.encode
import java.math.BigInteger
import java.util.concurrent.TimeUnit

class LockAndMint(
    private val networkConfig: NetworkConfig,
    private val renVMProvider: RenVMProvider,
    private val solanaChain: SolanaChain,
    val session: Session
) {

    private val state = State()

    @Throws(Exception::class)
    suspend fun generateGatewayAddress(): String? {
        val sendTo = solanaChain.getAssociatedTokenAddress(session.destinationAddress)
        state.sendTo = sendTo
        val sendToHex = encode(sendTo.toByteArray())
        val tokenGatewayContractHex = encode(Hash.generateSHash())
        val gHash = Hash.generateGHash(
            sendToHex, tokenGatewayContractHex,
            decode(
                session.nonce
            )
        )
        state.gHash = gHash
        val gPubKey = renVMProvider.selectPublicKey()
        state.gPubKey = gPubKey
        val gatewayAddress = Script.createAddressByteArray(
            Hash.hash160(gPubKey), gHash,
            byteArrayOf(
                networkConfig.p2shPrefix.toByte()
            )
        )
        session.gatewayAddress = Base58.encode(gatewayAddress)
        return session.gatewayAddress
    }

    @Throws(Exception::class)
    fun getDepositState(transactionHash: String?, txIndex: String?, amount: String?): State {
        val nonce = decode(session.nonce)
        val txid = decode(Utils.reverseHex(transactionHash))
        val nHash = Hash.generateNHash(nonce, txid, txIndex)
        val pHash = Hash.generatePHash()
        val txHash = renVMProvider.mintTxHash(
            state.gHash, state.gPubKey, nHash, nonce, amount, pHash,
            state.sendTo!!.toBase58(), txIndex, txid
        )
        state.txIndex = txIndex
        state.amount = amount
        state.nHash = nHash
        state.txid = txid
        state.pHash = pHash
        state.txHash = txHash
        return state
    }

    @Throws(RpcException::class)
    suspend fun submitMintTransaction(): String {
        return renVMProvider.submitMint(
            state.gHash, state.gPubKey, state.nHash,
            decode(
                session.nonce
            ),
            state.amount, state.pHash, state.sendTo!!.toBase58(), state.txIndex, state.txid
        )
    }

    @Throws(Exception::class)
    suspend fun mint(signer: Account?): String {
        val responseQueryMint = renVMProvider.queryMint(state.txHash!!)
        return solanaChain.submitMint(session.destinationAddress, signer!!, responseQueryMint)
    }

    @Throws(Exception::class)
    suspend fun lockAndMint(txHash: String?): ResponseQueryTxMint {
        return renVMProvider.queryMint(txHash!!)
    }

    @Throws(Exception::class)
    suspend fun estimateTransactionFee(): BigInteger {
        val fee = renVMProvider.estimateTransactionFee()
        session.fee = fee
        return fee
    }

    class State {
        var gHash: ByteArray = byteArrayOf()
        var gPubKey: ByteArray = byteArrayOf()
        var sendTo: PublicKey? = null
        var txid: ByteArray = byteArrayOf()
        var nHash: ByteArray = byteArrayOf()
        var pHash: ByteArray = byteArrayOf()

        @JvmField
        var txHash: String? = null
        var txIndex: String? = null
        var amount: String? = null
    }

    class Session {
        var destinationAddress: PublicKey

        @JvmField
        var nonce: String
        var createdAt: Long

        @JvmField
        var expiryTime: Long
        var gatewayAddress: String? = null
        var fee: BigInteger? = null

        constructor(destinationAddress: PublicKey) {
            this.destinationAddress = destinationAddress
            nonce = Utils.generateNonce()
            createdAt = System.currentTimeMillis()
            expiryTime = Utils.getSessionExpiry()
        }

        constructor(
            destinationAddress: PublicKey,
            nonce: String,
            createdAt: Long,
            expiryTime: Long,
            gatewayAddress: String?
        ) {
            this.destinationAddress = destinationAddress
            this.nonce = nonce
            this.createdAt = createdAt
            this.expiryTime = expiryTime
            this.gatewayAddress = gatewayAddress
        }

        constructor(
            destinationAddress: PublicKey,
            nonce: String,
            createdAt: Long,
            expiryTime: Long,
            gatewayAddress: String?,
            fee: BigInteger?
        ) {
            this.destinationAddress = destinationAddress
            this.nonce = nonce
            this.createdAt = createdAt
            this.expiryTime = expiryTime
            this.gatewayAddress = gatewayAddress
            this.fee = fee
        }

        /* We should subtract one day from expiry time to make it valid  */
        val isValid: Boolean
            get() {
                val dayInMillis = TimeUnit.DAYS.toMillis(1)
                val currentTime = System.currentTimeMillis()
                /* We should subtract one day from expiry time to make it valid  */
                return currentTime < expiryTime - dayInMillis
            }
    }

    companion object {
        @Throws(Exception::class)
        suspend fun buildSession(
            blockChainRepository: BlockChainRepository,
            networkConfig: NetworkConfig,
            destinationAddress: PublicKey
        ): LockAndMint {
            return LockAndMint(
                networkConfig,
                RenVMProvider(blockChainRepository),
                SolanaChain.create(
                    blockChainRepository,
                    networkConfig
                ),
                Session(destinationAddress)
            )
        }

        @Throws(Exception::class)
        suspend fun getSession(
            blockChainRepository: BlockChainRepository,
            networkConfig: NetworkConfig,
            session: Session
        ): LockAndMint {
            return LockAndMint(
                networkConfig,
                RenVMProvider(blockChainRepository),
                SolanaChain.create(
                    blockChainRepository,
                    networkConfig
                ),
                session
            )
        }
    }
}