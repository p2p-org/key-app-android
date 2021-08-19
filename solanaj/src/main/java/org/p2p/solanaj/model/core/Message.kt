package org.p2p.solanaj.model.core

import org.bitcoinj.core.Base58
import org.p2p.solanaj.utils.ShortvecEncoding
import java.nio.ByteBuffer

class Message(
    private val feePayer: PublicKey?,
    private val recentBlockhash: String
) {

    companion object {
        private const val RECENT_BLOCK_HASH_LENGTH = 32
        private const val HEADER_LENGTH = 3
    }

    private val accountKeys: AccountKeysList = AccountKeysList()
    private val programIds = mutableListOf<PublicKey>()
    private val instructions = mutableListOf<TransactionInstruction>()

    fun addInstructions(newInstructions: List<TransactionInstruction>) {
        newInstructions.forEach { instruction ->
            accountKeys.addAccounts(instruction.keys)

            val programId = instruction.programId
            if (programIds.none { it.toBase58() == programId.toBase58() }) {
                programIds.add(0, programId)
            }
        }

        instructions.addAll(newInstructions)

        programIds.forEach {
            accountKeys.addAccount(AccountMeta(it, isSigner = false, isWritable = false))
        }
    }

    fun serialize(): ByteArray {
        require(instructions.size != 0) { "No instructions provided" }

        val messageHeader = MessageHeader()

        val accountMetas = mutableListOf<AccountMeta>()
        accountMetas.addAll(accountKeys.getSortedAccounts())

        feePayer?.let {
            val index = accountMetas.indexOfFirst { it.publicKey.equals(feePayer) }
            if (index != -1) accountMetas.removeAt(index)
            accountMetas.add(0, AccountMeta(feePayer, isSigner = true, isWritable = true))
        }

        val accountKeysSize = accountMetas.size
        val accountAddressesLength = ShortvecEncoding.encodeLength(accountKeysSize)
        var compiledInstructionsLength = 0
        val compiledInstructions = mutableListOf<CompiledInstruction>()

        for (instruction in instructions) {
            val keysSize = instruction.keys.size
            val keyIndices = ByteArray(keysSize)
            for (i in 0 until keysSize) {
                keyIndices[i] = accountMetas.indexOfFirst {
                    it.publicKey.equals(instruction.keys[i].publicKey)
                }.toByte()
            }
            val compiledInstruction = CompiledInstruction()
            compiledInstruction.programIdIndex = accountMetas.indexOfFirst {
                it.publicKey.equals(instruction.programId)
            }.toByte()
            compiledInstruction.keyIndicesCount = ShortvecEncoding.encodeLength(keysSize)
            compiledInstruction.keyIndices = keyIndices
            compiledInstruction.dataLength = ShortvecEncoding.encodeLength(instruction.data.size)
            compiledInstruction.data = instruction.data
            compiledInstructions.add(compiledInstruction)
            compiledInstructionsLength += compiledInstruction.length
        }
        val instructionsLength = ShortvecEncoding.encodeLength(compiledInstructions.size)
        val bufferSize = (
            HEADER_LENGTH + RECENT_BLOCK_HASH_LENGTH + accountAddressesLength.size +
                accountKeysSize * PublicKey.PUBLIC_KEY_LENGTH + instructionsLength.size +
                compiledInstructionsLength
            )
        val out = ByteBuffer.allocate(bufferSize)
        val accountKeysBuff = ByteBuffer.allocate(accountKeysSize * PublicKey.PUBLIC_KEY_LENGTH)
        for (accountMeta in accountMetas) {
            accountKeysBuff.put(accountMeta.publicKey.toByteArray())
            if (accountMeta.isSigner) {
                val requiredSignatures = messageHeader.numRequiredSignatures
                messageHeader.numRequiredSignatures = requiredSignatures.plus(1).toByte()
                if (!accountMeta.isWritable) {
                    val signedAccounts = messageHeader.numReadonlySignedAccounts
                    messageHeader.numReadonlySignedAccounts = signedAccounts.plus(1).toByte()
                }
            } else {
                if (!accountMeta.isWritable) {
                    val unsignedAccounts = messageHeader.numReadonlyUnsignedAccounts
                    messageHeader.numReadonlyUnsignedAccounts = unsignedAccounts.plus(1).toByte()
                }
            }
        }
        out.put(messageHeader.toByteArray())
        out.put(accountAddressesLength)
        out.put(accountKeysBuff.array())
        out.put(Base58.decode(recentBlockhash))
        out.put(instructionsLength)
        for (compiledInstruction in compiledInstructions) {
            out.put(compiledInstruction.programIdIndex)
            out.put(compiledInstruction.keyIndicesCount)
            out.put(compiledInstruction.keyIndices)
            out.put(compiledInstruction.dataLength)
            out.put(compiledInstruction.data)
        }
        return out.array()
    }

    private class MessageHeader {
        var numRequiredSignatures: Byte = 0
        var numReadonlySignedAccounts: Byte = 0
        var numReadonlyUnsignedAccounts: Byte = 0
        fun toByteArray(): ByteArray {
            return byteArrayOf(numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts)
        }
    }

    private class CompiledInstruction {
        var programIdIndex: Byte = 0
        var keyIndicesCount: ByteArray = ByteArray(0)
        var keyIndices: ByteArray = ByteArray(0)
        var dataLength: ByteArray = ByteArray(0)
        var data: ByteArray = ByteArray(0)

        // 1 = programIdIndex length
        val length: Int
            get() = // 1 = programIdIndex length
                1 + keyIndicesCount.size + keyIndices.size + dataLength.size + data.size
    }
}