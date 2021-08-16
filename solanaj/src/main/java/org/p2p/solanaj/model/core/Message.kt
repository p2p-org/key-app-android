package org.p2p.solanaj.model.core

import org.bitcoinj.core.Base58
import org.p2p.solanaj.utils.ShortvecEncoding
import java.nio.ByteBuffer
import java.util.ArrayList

const val HEADER_LENGTH = 3

class Message {
    private inner class MessageHeader {
        var numRequiredSignatures: Byte = 0
        var numReadonlySignedAccounts: Byte = 0
        var numReadonlyUnsignedAccounts: Byte = 0
        fun toByteArray(): ByteArray {
            return byteArrayOf(numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts)
        }
    }

    private inner class CompiledInstruction {
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

    private var messageHeader: MessageHeader? = null
    private var recentBlockhash: String? = null
    private val accountKeys: AccountKeysList = AccountKeysList()
    private val instructions: MutableList<TransactionInstruction>
    private var feePayer: PublicKey? = null

    init {
        instructions = ArrayList()
    }

    fun addInstruction(instruction: TransactionInstruction): Message {
        val metas = instruction.keys.toMutableList()
        val account = AccountMeta(instruction.programId, isSigner = false, isWritable = false)
        metas.add(account)
        accountKeys.addMetas(metas)
        instructions.add(instruction)
        return this
    }

    fun setRecentBlockHash(recentBlockhash: String?) {
        this.recentBlockhash = recentBlockhash
    }

    fun setFeePayer(feePayer: PublicKey) {
        this.feePayer = feePayer
    }

    fun serialize(): ByteArray {
        requireNotNull(recentBlockhash) { "recentBlockhash required" }
        require(instructions.size != 0) { "No instructions provided" }

        messageHeader = MessageHeader()

        val keysList = getAccountKeys()
        val accountKeysSize = keysList.size
        val accountAddressesLength = ShortvecEncoding.encodeLength(accountKeysSize)
        var compiledInstructionsLength = 0
        val compiledInstructions: MutableList<CompiledInstruction> = ArrayList()
        for (instruction in instructions) {
            val keysSize = instruction.keys.size
            val keyIndices = ByteArray(keysSize)
            for (i in 0 until keysSize) {
                keyIndices[i] = keysList.indexOfFirst { it.publicKey.equals(instruction.keys[i].publicKey) }.toByte()
            }
            val compiledInstruction = CompiledInstruction()
            compiledInstruction.programIdIndex = keysList.indexOfFirst {
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
        for (accountMeta in keysList) {
            accountKeysBuff.put(accountMeta.publicKey.toByteArray())
            if (accountMeta.isSigner) {
                val requiredSignatures = messageHeader!!.numRequiredSignatures
                messageHeader!!.numRequiredSignatures = requiredSignatures.plus(1).toByte()
                if (!accountMeta.isWritable) {
                    val signedAccounts = messageHeader!!.numReadonlySignedAccounts
                    messageHeader!!.numReadonlySignedAccounts = signedAccounts.plus(1).toByte()
                }
            } else {
                if (!accountMeta.isWritable) {
                    val unsignedAccounts = messageHeader!!.numReadonlyUnsignedAccounts
                    messageHeader!!.numReadonlyUnsignedAccounts = unsignedAccounts.plus(1).toByte()
                }
            }
        }
        out.put(messageHeader!!.toByteArray())
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

    private fun getAccountKeys(): List<AccountMeta> {
        val keysList: MutableList<AccountMeta> = accountKeys.getSortedAccounts()

        val feePayerIndex = keysList.indexOfFirst { it.publicKey.equals(feePayer!!) }
        if (feePayerIndex != -1) {
            keysList.removeAt(feePayerIndex)
        }

        keysList.add(0, AccountMeta(feePayer!!, isSigner = true, isWritable = true))

        return keysList
    }

    companion object {
        private const val RECENT_BLOCK_HASH_LENGTH = 32
    }
}