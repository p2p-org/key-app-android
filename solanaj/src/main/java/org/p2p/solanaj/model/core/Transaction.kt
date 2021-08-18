package org.p2p.solanaj.model.core

import org.bitcoinj.core.Base58
import org.p2p.solanaj.utils.ShortvecEncoding
import org.p2p.solanaj.utils.TweetNaclFast
import java.nio.ByteBuffer

class Transaction(
    feePayer: PublicKey?,
    recentBlockhash: String,
    instructions: MutableList<TransactionInstruction>
) {

    companion object {
        const val SIGNATURE_LENGTH = 64
    }

    private val message = Message(feePayer, recentBlockhash)

    private val signatures = mutableListOf<String>()
    private var serializedMessage = ByteArray(0)

    init {
        message.addInstructions(instructions)
    }

    fun sign(signers: List<Account>) {
        require(signers.isNotEmpty()) { "No signers" }

        serializedMessage = message.serialize()

        for (signer in signers) {
            val signatureProvider = TweetNaclFast.Signature(ByteArray(0), signer.secretKey)
            val signature = signatureProvider.detached(serializedMessage)
            signatures.add(Base58.encode(signature))
        }
    }

    fun getSignature(): String? = signatures.firstOrNull()

    fun serialize(): ByteArray {
        val signaturesSize = signatures.size

        val signaturesLength = ShortvecEncoding.encodeLength(signaturesSize)
        val out = ByteBuffer.allocate(
            signaturesLength.size + signaturesSize * SIGNATURE_LENGTH + serializedMessage.size
        )

        out.put(signaturesLength)
        for (signature in signatures) {
            val rawSignature = Base58.decode(signature)
            out.put(rawSignature)
        }
        out.put(serializedMessage)
        return out.array()
    }
}