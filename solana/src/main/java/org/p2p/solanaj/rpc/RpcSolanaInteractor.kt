package org.p2p.solanaj.rpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bitcoinj.core.Base58
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.kits.renBridge.BurnDetails
import org.p2p.solanaj.kits.renBridge.GatewayRegistryData
import org.p2p.solanaj.kits.renBridge.GatewayStateData
import org.p2p.solanaj.kits.renBridge.RenProgram
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.utils.Hash
import org.p2p.solanaj.utils.SolanjLogger
import org.p2p.solanaj.utils.Utils
import org.p2p.solanaj.utils.crypto.Base64Utils
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.Arrays
import org.p2p.core.network.environment.RpcEnvironment

private const val GatewayRegistryStateKey = "GatewayRegistryState"
private const val GatewayStateKey = "GatewayStateV0.1.4"

class RpcSolanaInteractor(
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val rpcEnvironment: RpcEnvironment,
    appScope: CoroutineScope
) {
    private var gatewayRegistryData: GatewayRegistryData? = null

    init {
        appScope.launch {
            try {
                val pubk = PublicKey(rpcEnvironment.gatewayRegistry)
                val stateKey = PublicKey.findProgramAddress(
                    seeds = listOf(GatewayRegistryStateKey.toByteArray()),
                    programId = pubk
                )
                val accountInfo = rpcSolanaRepository.getAccountInfo(stateKey = stateKey.address)
                val base64Data = accountInfo.value.data?.get(0).orEmpty()
                gatewayRegistryData = GatewayRegistryData.decode(Base64Utils.decode(base64Data))
            } catch (e: Exception) {
                SolanjLogger.e(e)
            }
        }
    }

    fun resolveTokenGatewayContract(): PublicKey {
        if (gatewayRegistryData == null && gatewayRegistryData?.gateways == null) {
            throw IllegalStateException("Chain not initialized")
        }
        val sHash = Base58.encode(Hash.generateSHash())
        val index = this.gatewayRegistryData?.selectors?.indexOf(sHash)

        return index?.let { gatewayRegistryData?.gateways?.get(it) }
            ?: error("Gateway not found at index $index")
    }

    fun getSPLTokenPubkey(): PublicKey {
        val program = resolveTokenGatewayContract()
        val sHash = Base58.encode(Hash.generateSHash())
        val mint = program.let { PublicKey.findProgramAddress(listOf(Base58.decode(sHash)), it) }
        return mint.address
    }

    fun getAssociatedTokenAddress(address: PublicKey?): PublicKey? {
        val mint = getSPLTokenPubkey()
        return address?.let { TokenTransaction.getAssociatedTokenAddress(mint, it) }
    }

    suspend fun createAssociatedTokenAccount(address: PublicKey, signer: Account): String {
        val tokenMint = getSPLTokenPubkey()
        val associatedTokenAddress = getAssociatedTokenAddress(address)

        val transactionInstruction = TokenProgram.createAssociatedTokenAccountInstruction(
            TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
            TokenProgram.PROGRAM_ID,
            tokenMint,
            associatedTokenAddress,
            address,
            signer.publicKey
        )

        val transaction = Transaction().apply {
            addInstruction(transactionInstruction)
        }
        return rpcSolanaRepository.sendTransaction(transaction, signer)
    }

    suspend fun submitMint(
        address: PublicKey,
        signer: Account,
        responseQueryMint: ResponseQueryTxMint
    ): String {

        val pHash = Utils.fromURLBase64(responseQueryMint.valueIn.phash)
        val amount = responseQueryMint.valueOut.amount
        val nHash = Utils.fromURLBase64(responseQueryMint.valueIn.nhash)
        val sig = Utils.fixSignatureSimple(responseQueryMint.valueOut.sig)

        val program = resolveTokenGatewayContract()

        val gatewayAccountId = PublicKey.findProgramAddress(listOf(GatewayStateKey.toByteArray()), program).address
        val sHash = Hash.generateSHash()

        val tokenMint = getSPLTokenPubkey()
        val mintAuthority = PublicKey.findProgramAddress(listOf(tokenMint.asByteArray()), program).address

        val recipientTokenAccount = getAssociatedTokenAddress(address)

        val recipientTokenAccountArray = recipientTokenAccount?.asByteArray() ?: byteArrayOf()
        val renVMMessage = buildRenVMMessage(pHash, amount, sHash, recipientTokenAccountArray, nHash)
        val mintLogAccount = PublicKey.findProgramAddress(listOf(Hash.keccak256(renVMMessage)), program).address

        val mintInstruction = RenProgram.mintInstruction(
            signer.publicKey,
            gatewayAccountId,
            tokenMint,
            recipientTokenAccount,
            mintLogAccount,
            mintAuthority,
            program
        )

        val gatewayInfo = rpcSolanaRepository.getAccountInfo(gatewayAccountId)
        val base64Data = gatewayInfo.value.data?.get(0).orEmpty()
        val gatewayState = GatewayStateData.decode(Base64Utils.decode(base64Data))

        val secpInstruction = RenProgram.createInstructionWithEthAddress2(
            gatewayState.renVMAuthority, renVMMessage, Arrays.copyOfRange(sig, 0, 64), sig[64] - 27
        )

        val transaction = Transaction().apply {
            addInstructions(listOf(mintInstruction, secpInstruction))
        }

        return rpcSolanaRepository.sendTransaction(transaction, signer)
    }

    suspend fun findMintByDepositDetails(
        nHash: ByteArray,
        pHash: ByteArray,
        to: ByteArray,
        amount: String
    ): String {
        val program = resolveTokenGatewayContract()
        val sHash = Hash.generateSHash()
        val publicKey = PublicKey(to).asByteArray()
        val renVMMessage = buildRenVMMessage(pHash, amount, sHash, publicKey, nHash)
        val mintLogAccount = PublicKey.findProgramAddress(listOf(Hash.keccak256(renVMMessage)), program).address

        var signature = ""
        try {
            val accountInfo = rpcSolanaRepository.getAccountInfo(mintLogAccount)

            val mintData = TokenTransaction.getMintData(accountInfo, program)
            val isMintInitialized = mintData?.isInitialized ?: false
            if (!isMintInitialized) {
                return signature
            }

            val signatures = rpcSolanaRepository.getConfirmedSignaturesForAddress(mintLogAccount, 1)
            signature = signatures[0].signature
        } catch (e: Exception) {
            // TODO provide exception logging
        }
        return signature
    }

    suspend fun submitBurn(
        account: PublicKey,
        amount: String,
        recepient: String,
        signer: Account
    ): BurnDetails {

        val program = resolveTokenGatewayContract()
        val tokenMint = getSPLTokenPubkey()
        val source = getAssociatedTokenAddress(account)

        val checkedBurnInstruction = TokenProgram.createBurnCheckedInstruction(
            TokenProgram.PROGRAM_ID,
            tokenMint,
            source,
            account,
            BigInteger(amount),
            8
        )

        val gatewayAccountId = PublicKey.findProgramAddress(listOf(GatewayStateKey.toByteArray()), program).address
        val gatewayInfo = rpcSolanaRepository.getAccountInfo(gatewayAccountId)
        val base64Data = gatewayInfo.value.data?.get(0).orEmpty()

        val gatewayState = GatewayStateData.decode(Base64Utils.decode(base64Data))

        val nonceBN = gatewayState.burnCount.add(BigInteger.ONE)

        val burnLogAccountId = PublicKey.findProgramAddress(listOf(Utils.uint64ToByteArrayLE(nonceBN)), program).address

        val burnInstruction = RenProgram.burnInstruction(
            account,
            source,
            gatewayAccountId,
            tokenMint,
            burnLogAccountId,
            recepient.toByteArray(),
            program
        )

        val transaction = Transaction().apply {
            addInstructions(listOf(checkedBurnInstruction, burnInstruction))
        }

        val signature = rpcSolanaRepository.sendTransaction(transaction, signer)

        val burnDetails = BurnDetails().apply {
            confirmedSignature = signature
            nonce = nonceBN
            this.recipient = recepient
        }
        return burnDetails
    }

    fun buildRenVMMessage(
        pHash: ByteArray,
        amount: String,
        token: ByteArray,
        to: ByteArray,
        nHash: ByteArray
    ): ByteArray {
        val message = ByteBuffer.allocate(160)
        message.put(pHash)

        val amountBytes = BigInteger(amount).toByteArray()
        val amountBuffer = ByteBuffer.allocate(32).apply {
            position(32 - amountBytes.size)
            put(amountBytes)
        }
        message.put(amountBuffer.array())

        with(message) {
            put(token)
            put(to)
            put(nHash)
        }
        return message.array()
    }
}
