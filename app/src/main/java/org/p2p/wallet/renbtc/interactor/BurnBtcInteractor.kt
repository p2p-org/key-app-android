package org.p2p.wallet.renbtc.interactor

import org.bitcoinj.core.Base58
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.renBridge.BurnDetails
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.solanaj.kits.renBridge.renVM.RenVMRepository
import org.p2p.solanaj.rpc.RpcException
import org.p2p.solanaj.rpc.RpcSolanaInteractor
import org.p2p.solanaj.utils.Hash
import org.p2p.solanaj.utils.Utils
import org.p2p.core.crypto.Hex
import org.p2p.wallet.auth.analytics.RenBtcAnalytics
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.core.network.data.ServerException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigInteger
import java.nio.ByteBuffer
import kotlinx.coroutines.withContext

class BurnBtcInteractor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val renVMRepository: RenVMRepository,
    private val rpcSolanaInteractor: RpcSolanaInteractor,
    private val renBtcAnalytics: RenBtcAnalytics,
    private val dispatchers: CoroutineDispatchers
) {
    companion object {
        private const val BURN_FEE_LENGTH = 97
        private const val REN_BTC_DECIMALS = 6
        private const val BURN_FEE_VALUE = 20000L
    }

    private val state: LockAndMint.State = LockAndMint.State()

    private var nonceBuffer: ByteArray = byteArrayOf()
    private var recipient: String = ""

    suspend fun submitBurnTransaction(recipient: String, amount: BigInteger): String = withContext(dispatchers.io) {
        val signer = tokenKeyProvider.publicKey.toPublicKey()
        val signerSecretKey = tokenKeyProvider.keyPair

        val burnDetails: BurnDetails = submitBurnTransaction(
            account = signer,
            amount = amount.toString(),
            recipient = recipient,
            signer = Account(signerSecretKey)
        )

        // TODO: We are not using state and hash.
        // TODO: WORKAROUND. it's crashing anyway. Temporary commenting it.
        val burnState = try {
            getBurnState(burnDetails, amount.toString())
        } catch (e: Throwable) {
            // TODO: We are not using state, therefore ignoring errors for now
            Timber.e(e, "Error getting state")
        }

        val hash = try {
            release()
        } catch (e: ServerException) {
            // TODO: Handle this error [invalid burn info: cannot get burn info: decoding solana burn log: expected data len=97, got=0]
            // TODO: It crashes even if transaction is valid
            if (e.message?.contains("invalid burn info") == true) {
                return@withContext burnDetails.confirmedSignature
            } else {
                renBtcAnalytics.logRenBtcSend(sendSuccess = false)
                throw e
            }
        }
        renBtcAnalytics.logRenBtcSend(sendSuccess = true)
        return@withContext burnDetails.confirmedSignature
    }

    // TODO: WORKAROUND. it's crashing anyway. Temporary commenting it.
    fun getBurnFee(): BigInteger {
//        val feeLamports = BURN_FEE_VALUE.toBigDecimal().toLamports(REN_BTC_DECIMALS)
        return BURN_FEE_VALUE.toBigInteger()
    }

    private suspend fun submitBurnTransaction(
        account: PublicKey,
        amount: String,
        recipient: String,
        signer: Account
    ): BurnDetails {
        this.recipient = recipient
        return rpcSolanaInteractor.submitBurn(account, amount, recipient, signer)
    }

    private fun getBurnState(burnDetails: BurnDetails, amount: String): LockAndMint.State {
        val txId = Base58.decode(burnDetails.confirmedSignature)
        nonceBuffer = getNonceBuffer(burnDetails.nonce)
        val nHash = Hash.generateNHash(nonceBuffer, txId, "0")
        val pHash = Hash.generatePHash()
        val sHash = Hash.generateSHash("BTC/toBitcoin")
        val gHash =
            Hash.generateGHash(
                Hex.encode(Utils.addressToBytes(burnDetails.recipient)),
                Hex.encode(sHash),
                nonceBuffer
            )

        val txHash = renVMRepository.getTxHash(
            selector = RenVMRepository.BURN_SELECTOR,
            gHash = gHash,
            gPubKey = byteArrayOf(),
            nHash = nHash,
            nonce = nonceBuffer,
            amount = amount,
            pHash = pHash,
            to = burnDetails.recipient,
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
        return renVMRepository.submit(
            selector = RenVMRepository.BURN_SELECTOR,
            gHash = state.gHash,
            gPubKey = state.gPubKey,
            nHash = state.nHash,
            nonce = nonceBuffer,
            amount = state.amount,
            pHash = state.pHash,
            to = recipient,
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
