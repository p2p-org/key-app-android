package org.p2p.solanaj.core

import org.p2p.solanaj.core.Transaction.DEFAULT_SIGNATURE
import org.p2p.solanaj.core.Transaction.SIGNATURE_LENGTH
import org.p2p.solanaj.utils.ShortvecEncoding
import org.p2p.solanaj.utils.crypto.Base58Utils

@Suppress("Unused")
object TransactionParser {

    fun fromByteArray(byteArray: ByteArray): Transaction {
        val signatures = mutableListOf<String>()
        val signatureLength = ShortvecEncoding.decodeLength(byteArray)

        var updatedByteArray = byteArray

        for (i in 0 until signatureLength) {
            val signatureData: ByteArray = updatedByteArray.copyOf(SIGNATURE_LENGTH)
            updatedByteArray = updatedByteArray.drop(SIGNATURE_LENGTH).toByteArray()
            signatures += Base58Utils.encode(signatureData)
        }

        return parse(updatedByteArray, signatures)
    }

    private fun parse(updatedByteArray: ByteArray, signatures: MutableList<String>): Transaction {
        val transaction = Transaction()

        val message = Message.deserialize(updatedByteArray)

        transaction.recentBlockHash = message.recentBlockHash

        if (message.numRequiredSignatures > 0) {
            transaction.feePayer = message.accountKeys.first().publicKey
        }

        signatures.forEachIndexed { index, signature ->
            val formattedSignature = if (Base58Utils.encode(DEFAULT_SIGNATURE) == signature) {
                null
            } else {
                Base58Utils.decode(signature)
            }

            val signaturePubkeyPair = Signature(
                publicKey = message.accountKeys[index].publicKey,
                signature = formattedSignature
            )

            transaction.allSignatures.add(signaturePubkeyPair)
        }

        message.instructions.forEach { instruction ->
            val keys = instruction.keys.mapIndexedNotNull { index, account ->
                val pubkey = message.accountKeys.find {
                    it.publicKey.equals(account.publicKey)
                } ?: return@mapIndexedNotNull null

                val isSigner = transaction.allSignatures.any { it.publicKey.equals(pubkey.publicKey) }
                val isAccountSigner = message.isAccountSigner(index)
                AccountMeta(
                    publicKey = pubkey.publicKey,
                    isSigner = isSigner || isAccountSigner,
                    isWritable = message.isAccountWritable(index)
                )
            }

            val account = message.accountKeys.find { it.publicKey.equals(instruction.programId) }
            if (account != null) {
                val transactionInstruction = TransactionInstruction(
                    keys = keys,
                    programId = account.publicKey,
                    data = instruction.data
                )
                transaction.addInstruction(transactionInstruction)
            }
        }

        return transaction
    }
}
