package org.p2p.wallet.renbtc.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import org.p2p.solanaj.utils.crypto.Hex
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger
import java.nio.ByteBuffer

class BurnBtcInteractor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val rpcAmountRepository: RpcAmountRepository,
    private val renVMRepository: RenVMRepository,
    private val rpcSolanaInteractor: RpcSolanaInteractor,
    private val state: LockAndMint.State = LockAndMint.State()
) {

    companion object {
        private const val BURN_FEE_LENGTH = 97
        private const val REN_BTC_DECIMALS = 6
        private const val BURN_FEE_VALUE = "0.000005"
    }

    private var nonceBuffer: ByteArray = byteArrayOf()
    private var recepient: String = ""

    suspend fun submitBurnTransaction(recipient: String, amount: BigInteger): String = withContext(Dispatchers.IO) {
        val signer = tokenKeyProvider.publicKey.toPublicKey()
        val signerSecretKey = tokenKeyProvider.secretKey

        val burnDetails = submitBurnTransaction(
            signer,
            amount.toString(),
            recipient,
            Account(signerSecretKey)
        )

        val burnState = getBurnState(burnDetails, amount.toString())
        val hash = try {
            release()
        } catch (e: RpcException) {
            // TODO: Handle this error [invalid burn info: cannot get burn info: decoding solana burn log: expected data len=97, got=0]
            // TODO: It crashes even if transaction is valid
            if (e.message?.startsWith("invalid burn info") == true) {
                return@withContext burnDetails.confirmedSignature
            } else {
                throw e
            }
        }
        return@withContext burnDetails.confirmedSignature
    }

    suspend fun getBurnFee(): BigInteger {
        val fee = rpcAmountRepository.getMinBalanceForRentExemption(BURN_FEE_LENGTH)
        val feeLamports = BURN_FEE_VALUE.toBigDecimal().toLamports(REN_BTC_DECIMALS)
        return fee + feeLamports
    }

    private suspend fun submitBurnTransaction(
        account: PublicKey,
        amount: String,
        recepient: String,
        signer: Account
    ): BurnDetails {
        this.recepient = recepient
        return rpcSolanaInteractor.submitBurn(account, amount, recepient, signer)
    }

    private fun getBurnState(burnDetails: BurnDetails, amount: String): LockAndMint.State {
        val txId = Base58.decode(burnDetails.confirmedSignature)
        nonceBuffer = getNonceBuffer(burnDetails.nonce)
        val nHash = Hash.generateNHash(nonceBuffer, txId, "0")
        val pHash = Hash.generatePHash()
        val sHash = Hash.generateSHash("BTC/toBitcoin")
        val gHash =
            Hash.generateGHash(
                Hex.encode(Utils.addressToBytes(burnDetails.recepient)),
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
        return renVMRepository.submit(
            selector = RenVMRepository.BURN_SELECTOR,
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
