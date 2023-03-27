package org.p2p.solanaj.core

import org.bitcoinj.core.Base58
import java.math.BigInteger
import java.nio.ByteBuffer
import org.p2p.solanaj.utils.ShortvecEncoding
import org.p2p.solanaj.utils.TweetNaclFast

private const val SIGNATURE_LENGTH = 64

class Transaction {

    private val message: Message = Message()
    private val signatures = mutableListOf<Signature>()
    private var feePayer: PublicKey? = null

    private lateinit var serializedMessage: ByteArray

    val accountKeys: List<AccountMeta>
        get() = message.accountKeys

    val instructions: List<TransactionInstruction>
        get() = message.instructions

    val recentBlockHash: String
        get() = message.recentBlockHash

    val signature: Signature?
        get() = signatures.firstOrNull()

    val allSignatures: List<Signature>
        get() = signatures

    fun addInstruction(instruction: TransactionInstruction?): Transaction {
        message.addInstruction(instruction)
        return this
    }

    fun addInstructions(instructions: List<TransactionInstruction?>): Transaction {
        for (instruction in instructions) {
            message.addInstruction(instruction)
        }
        return this
    }

    fun setRecentBlockhash(recentBlockhash: String) {
        message.recentBlockHash = recentBlockhash
    }

    fun setFeePayer(feePayer: PublicKey) {
        message.setFeePayer(feePayer)
        this.feePayer = feePayer
    }

    fun getFeePayer(): PublicKey? = feePayer

    fun sign(signer: Account) {
        sign(listOf(signer))
    }

    fun signWithoutSignatures(owner: PublicKey) {
        if (feePayer == null) {
            feePayer = owner
        }
        message.setFeePayer(feePayer)
        serializedMessage = message.serialize()
    }

    fun sign(signers: List<Account>) {
        require(signers.isNotEmpty()) { "No signers" }
        if (feePayer == null) {
            feePayer = signers[0].publicKey
        }
        message.setFeePayer(feePayer)
        serializedMessage = message.serialize()
        for (signer in signers) {
            val signatureProvider = TweetNaclFast.Signature(ByteArray(0), signer.keypair)
            val signature = signatureProvider.detached(serializedMessage)
            val newSignature = Signature(signer.publicKey, Base58.encode(signature))
            signatures.add(newSignature)
        }
    }

    fun serialize(): ByteArray {
        val signaturesSize = signatures.size
        val signaturesLength = ShortvecEncoding.encodeLength(signaturesSize)
        val capacity = signaturesLength.size + signaturesSize * SIGNATURE_LENGTH + serializedMessage.size
        val out = ByteBuffer.allocate(capacity)
        out.put(signaturesLength)

        signatures.forEach { signature ->
            val rawSignature = Base58.decode(signature.signature)
            out.put(rawSignature)
        }

        out.put(serializedMessage)
        return out.array()
    }

    fun findSignature(publicKey: PublicKey): Signature? =
        signatures.find { it.publicKey.equals(publicKey) }

    fun addSignature(signature: Signature) {
        val index = signatures.indexOfFirst { it.publicKey.equals(signature.publicKey) }

        // FIXME: Perhaps we need to throw exception here because there feePayer should exist
        if (index == -1) {
            signatures.add(0, signature)
            return
        }

        signatures[index] = signature
    }

    fun calculateTransactionFee(lamportsPerSignatures: BigInteger): BigInteger {
        message.serialize()
        return BigInteger.valueOf(message.numRequiredSignatures.toLong()).multiply(lamportsPerSignatures)
    }
}
