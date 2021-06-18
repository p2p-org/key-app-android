package org.p2p.solanaj.model.core

import org.bitcoinj.core.Base58
import org.p2p.solanaj.utils.ShortvecEncoding
import org.p2p.solanaj.utils.TweetNaclFast
import java.nio.ByteBuffer
import java.util.ArrayList

class TransactionRequest {
    companion object {
        const val SIGNATURE_LENGTH = 64
    }
    private val message: Message = Message()
    private val signatures: MutableList<String>

    private var serializedMessage: ByteArray = ByteArray(0)

    init {
        signatures = ArrayList()
    }

    fun addInstruction(instruction: TransactionInstruction?): TransactionRequest {
        message.addInstruction(instruction!!)
        return this
    }

    fun setRecentBlockHash(recentBlockhash: String?) {
        message.setRecentBlockHash(recentBlockhash)
    }

    fun sign(signer: Account) {
        sign(listOf(signer))
    }

    fun sign(signers: List<Account>) {
        require(signers.isNotEmpty()) { "No signers" }
        val feePayer = signers[0]
        message.setFeePayer(feePayer)
        serializedMessage = message.serialize()
        for (signer in signers) {
            val signatureProvider = TweetNaclFast.Signature(ByteArray(0), signer.secretKey)
            val signature = signatureProvider.detached(serializedMessage)
            signatures.add(Base58.encode(signature))
        }
    }

    fun serialize(): ByteArray {
        val signaturesSize = signatures.size
        val signaturesLength = ShortvecEncoding.encodeLength(signaturesSize)
        val out = ByteBuffer
            .allocate(signaturesLength.size + signaturesSize * SIGNATURE_LENGTH + serializedMessage.size)
        out.put(signaturesLength)
        for (signature in signatures) {
            val rawSignature = Base58.decode(signature)
            out.put(rawSignature)
        }
        out.put(serializedMessage)
        return out.array()
    }
}