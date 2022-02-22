package org.p2p.solanaj.kits.renBridge

import android.util.Log
import org.bitcoinj.core.Base58
import org.p2p.solanaj.core.AbstractData
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.PublicKey.Companion.findProgramAddress
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.TokenTransaction.getAssociatedTokenAddress
import org.p2p.solanaj.kits.TokenTransaction.getMintData
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.rpc.BlockChainRepository
import org.p2p.solanaj.utils.ByteUtils
import org.p2p.solanaj.utils.Hash
import org.p2p.solanaj.utils.Utils
import org.p2p.solanaj.utils.crypto.Base64Utils.decode
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.Arrays

class SolanaChain(
    private val blockChainRepository: BlockChainRepository,
) {
    private var gatewayRegistryData: GatewayRegistryData? = null

    private suspend fun init(networkConfig: NetworkConfig) {
        val publicKey = PublicKey(networkConfig.gatewayRegistry)
        val stateKey = findProgramAddress(
            listOf(GatewayRegistryStateKey.toByteArray()),
            publicKey
        ).address
        val accountInfo = blockChainRepository.getAccountInfo(stateKey.toString())
        val base64Data = accountInfo?.value?.data?.getOrNull(0)
        gatewayRegistryData = GatewayRegistryData.decode(decode(base64Data!!))
    }

    fun resolveTokenGatewayContract(): PublicKey {
        if (gatewayRegistryData == null) {
            throw RuntimeException("chain not initialized")
        }
        val sHash = Base58.encode(Hash.generateSHash())
        val index = gatewayRegistryData!!.selectors.indexOf(sHash)
        return gatewayRegistryData!!.gateways[index]
    }

    @get:Throws(Exception::class)
    val sPLTokenPublicKey: PublicKey
        get() {
            val program = resolveTokenGatewayContract()
            val sHash = Base58.encode(Hash.generateSHash())
            return findProgramAddress(listOf(Base58.decode(sHash)), program).address
        }

    @Throws(Exception::class)
    fun getAssociatedTokenAddress(address: PublicKey?): PublicKey {
        val mint = sPLTokenPublicKey
        return getAssociatedTokenAddress(mint, address!!)
    }

    @Throws(Exception::class)
    suspend fun createAssociatedTokenAccount(address: PublicKey?, signer: Account): String {
        val tokenMint = sPLTokenPublicKey
        val associatedTokenAddress = getAssociatedTokenAddress(address)
        val createAccountInstruction = TokenProgram.createAssociatedTokenAccountInstruction(
            TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
            TokenProgram.PROGRAM_ID,
            tokenMint,
            associatedTokenAddress,
            address,
            signer.publicKey
        )
        val transaction = Transaction()
        transaction.addInstruction(createAccountInstruction)
        return blockChainRepository.sendTransaction(transaction, listOf(signer))
    }

    @Throws(Exception::class)
    suspend fun submitMint(
        address: PublicKey?,
        signer: Account,
        responceQueryMint: ResponseQueryTxMint
    ): String {
        val pHash = Utils.fromURLBase64(responceQueryMint.valueIn.phash)
        val amount = responceQueryMint.valueOut.amount
        val nHash = Utils.fromURLBase64(responceQueryMint.valueIn.nhash)
        val sig = Utils.fixSignatureSimple(responceQueryMint.valueOut.sig)
        val program = resolveTokenGatewayContract()
        val gatewayAccountId =
            findProgramAddress(
                listOf(GatewayStateKey.toByteArray()),
                program
            ).address
        val sHash = Hash.generateSHash()
        val tokenMint = sPLTokenPublicKey
        val mintAuthority = findProgramAddress(
            listOf(tokenMint.toByteArray()),
            program
        ).address
        val recipientTokenAccount = getAssociatedTokenAddress(address)
        val renVMMessage =
            buildRenVMMessage(pHash, amount, sHash, recipientTokenAccount.toByteArray(), nHash)
        val mintLogAccount =
            findProgramAddress(
                listOf(
                    Hash.keccak256(renVMMessage)
                ),
                program
            ).address
        val mintInstruction = RenProgram.mintInstruction(
            signer.publicKey, gatewayAccountId,
            tokenMint, recipientTokenAccount, mintLogAccount, mintAuthority, program
        )
        val gatewayInfo = blockChainRepository.getAccountInfo(gatewayAccountId.toString())
        val base64Data = gatewayInfo?.value?.data?.getOrNull(0)
        val gatewayState = GatewayStateData.decode(decode(base64Data!!))
        val secpInstruction = RenProgram.createInstructionWithEthAddress2(
            gatewayState.renVMAuthority, renVMMessage, Arrays.copyOfRange(sig, 0, 64), sig[64] - 27
        )
        val transaction = Transaction()
        transaction.addInstruction(mintInstruction)
        transaction.addInstruction(secpInstruction)
        return blockChainRepository.sendTransaction(transaction, listOf(signer))
    }

    @Throws(Exception::class)
    suspend fun findMintByDepositDetails(
        nHash: ByteArray?,
        pHash: ByteArray?,
        to: ByteArray?,
        amount: String?
    ): String {
        val program = resolveTokenGatewayContract()
        val sHash = Hash.generateSHash()
        val renVMMessage = buildRenVMMessage(
            pHash, amount, sHash,
            PublicKey(
                to!!
            ).toByteArray(),
            nHash
        )
        val mintLogAccount = findProgramAddress(listOf(Hash.keccak256(renVMMessage)), program).address
        var signature = ""
        try {
            val accountInfo = blockChainRepository.getAccountInfo(mintLogAccount.toString())
            val mintData = getMintData(accountInfo, program)
            if (!mintData.isInitialized) {
                return signature
            }
            val signatures = blockChainRepository.getConfirmedSignaturesForAddress(
                mintLogAccount,
                null,
                1
            )
            signature = signatures[0].signature
        } catch (exception: Exception) {
            Log.e(
                SolanaChain::class.java.canonicalName,
                "Error on findMintByDepositDetails",
                exception
            )
        }
        return signature
    }

    @Throws(Exception::class)
    suspend fun submitBurn(account: PublicKey?, amount: String?, recepient: String?, signer: Account): BurnDetails {
        val program = resolveTokenGatewayContract()
        val tokenMint = sPLTokenPublicKey
        val source = getAssociatedTokenAddress(account)
        val checkedBurnInstruction = TokenProgram.createBurnCheckedInstruction(
            TokenProgram.PROGRAM_ID,
            tokenMint,
            source,
            account,
            BigInteger(amount),
            8
        )
        val gatewayAccountId = findProgramAddress(listOf(GatewayStateKey.toByteArray()), program).address
        val gatewayInfo = blockChainRepository.getAccountInfo(gatewayAccountId.toString())
        val base64Data = gatewayInfo?.value?.data?.getOrNull(0)
        val gatewayState = GatewayStateData.decode(decode(base64Data!!))
        val nonceBN = gatewayState.burnCount.add(BigInteger.ONE)
        val burnLogAccountId = findProgramAddress(listOf(Utils.uint64ToByteArrayLE(nonceBN)), program).address
        val burnInstruction = RenProgram.burnInstruction(
            account, source, gatewayAccountId,
            tokenMint, burnLogAccountId, recepient?.toByteArray(), program
        )
        val transaction = Transaction()
        transaction.addInstruction(checkedBurnInstruction)
        transaction.addInstruction(burnInstruction)
        val confirmedSignature = blockChainRepository.sendTransaction(transaction, listOf(signer))
        return BurnDetails(confirmedSignature, nonceBN, recepient)
    }

    class BurnDetails(
        var confirmedSignature: String,
        var nonce: BigInteger,
        var recepient: String?,
    )

    class GatewayStateData private constructor(data: ByteArray?) : AbstractData(data!!, GATEWAY_STATE_DATA_LENGTH) {
        val isInitialized: Boolean
        val renVMAuthority: ByteArray
        private val selectors: ByteArray
        val burnCount: BigInteger
        private val underlyingDecimals: Int

        init {
            val isInitializedValue = readByte().toInt()
            isInitialized = isInitializedValue != 0
            renVMAuthority = readBytes(20)
            selectors = readBytes(32)
            burnCount = readUint64()
            underlyingDecimals = readByte().toInt()
        }

        companion object {
            private const val GATEWAY_STATE_DATA_LENGTH = 1 + 20 + 32 + ByteUtils.UINT_64_LENGTH + 1
            fun decode(data: ByteArray?): GatewayStateData {
                return GatewayStateData(data)
            }
        }
    }

    class GatewayRegistryData private constructor(data: ByteArray) : AbstractData(data, GATEWAY_REGISTRY_DATA_LENGTH) {
        val isInitialized: Boolean
        val owner: PublicKey
        val count: Int
        val selectors: ArrayList<String>
        val gateways: ArrayList<PublicKey>

        init {
            val isInitializedValue = readByte().toInt()
            isInitialized = isInitializedValue != 0
            owner = readPublicKey()
            val countValue = readUint64()
            count = countValue.toInt()
            val selectorsSize = readUint32()
            selectors = ArrayList(selectorsSize.toInt())
            for (i in 0 until selectorsSize) {
                val selector = readBytes(32)
                selectors.add(Base58.encode(selector))
            }
            val gatewaysSize = readUint32()
            gateways = ArrayList(gatewaysSize.toInt())
            for (i in 0 until gatewaysSize) {
                gateways.add(readPublicKey())
            }
        }

        companion object {
            private const val GATEWAY_REGISTRY_DATA_LENGTH = (
                1 + PublicKey.PUBLIC_KEY_LENGTH +
                    ByteUtils.UINT_64_LENGTH + ByteUtils.UINT_32_LENGTH + 32 * PublicKey.PUBLIC_KEY_LENGTH +
                    ByteUtils.UINT_32_LENGTH + 32 * PublicKey.PUBLIC_KEY_LENGTH
                )

            @JvmStatic
            fun decode(data: ByteArray): GatewayRegistryData {
                return GatewayRegistryData(data)
            }
        }
    }

    companion object {
        @JvmField
        var GatewayRegistryStateKey = "GatewayRegistryState"
        var GatewayStateKey = "GatewayStateV0.1.4"

        @JvmStatic
        fun buildRenVMMessage(
            pHash: ByteArray?,
            amount: String?,
            token: ByteArray?,
            to: ByteArray?,
            nHash: ByteArray?
        ): ByteArray {
            val message = ByteBuffer.allocate(160)
            message.put(pHash)
            val amountBytes = BigInteger(amount).toByteArray()
            val amountBuffer = ByteBuffer.allocate(32)
            amountBuffer.position(32 - amountBytes.size)
            amountBuffer.put(amountBytes)
            message.put(amountBuffer.array())
            message.put(token)
            message.put(to)
            message.put(nHash)
            return message.array()
        }

        suspend fun create(
            blockChainRepository: BlockChainRepository,
            networkConfig: NetworkConfig
        ): SolanaChain {
            return SolanaChain(blockChainRepository).also {
                it.init(networkConfig)
            }
        }
    }
}