package org.p2p.solanaj.programs

import org.p2p.solanaj.model.core.AbstractData
import org.p2p.solanaj.model.core.AccountMeta
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.Sysvar.SYSVAR_RENT_ADDRESS
import org.p2p.solanaj.model.core.TransactionInstruction
import org.p2p.solanaj.utils.ByteUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.util.ArrayList

object TokenProgram {
    val PROGRAM_ID = PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")
    val ASSOCIATED_TOKEN_PROGRAM_ID = PublicKey("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL")
    private const val INSTRUCTION_INDEX_INITIALIZE_MINT = 0
    private const val INSTRUCTION_INDEX_INITIALIZE_ACCOUNT = 1
    private const val INSTRUCTION_INDEX_TRANSFER = 3
    private const val INSTRUCTION_INDEX_APPROVE = 4
    private const val INSTRUCTION_INDEX_MINT_TO = 7
    private const val INSTRUCTION_INDEX_CLOSE_ACCOUNT = 9
    private const val INSTRUCTION_INDEX_TRANSFER_CHECKED = 12

    fun createAssociatedTokenAccountInstruction(
        mint: PublicKey,
        associatedAccount: PublicKey,
        owner: PublicKey,
        payer: PublicKey
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(payer, isSigner = true, isWritable = true))
        keys.add(AccountMeta(associatedAccount, isSigner = false, isWritable = true))
        keys.add(AccountMeta(owner, isSigner = false, isWritable = false))
        keys.add(AccountMeta(mint, isSigner = false, isWritable = false))
        keys.add(AccountMeta(SystemProgram.PROGRAM_ID, isSigner = false, isWritable = false))
        keys.add(AccountMeta(PROGRAM_ID, isSigner = false, isWritable = false))
        keys.add(AccountMeta(SYSVAR_RENT_ADDRESS, isSigner = false, isWritable = false))
        val data = ByteArray(0)
        return TransactionInstruction(ASSOCIATED_TOKEN_PROGRAM_ID, keys, data)
    }

    fun initializeMintInstruction(
        tokenProgramId: PublicKey,
        mint: PublicKey,
        decimals: Int,
        authority: PublicKey,
        freezeAuthority: PublicKey?
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(mint, isSigner = false, isWritable = true))
        keys.add(AccountMeta(SYSVAR_RENT_ADDRESS, isSigner = false, isWritable = false))
        val bos = ByteArrayOutputStream()
        bos.write(INSTRUCTION_INDEX_INITIALIZE_MINT)
        bos.write(decimals)
        // bos.writeBytes(authority.toByteArray());
        bos.write(authority.toByteArray(), 0, authority.toByteArray().size)
        bos.write(if (freezeAuthority == null) 0 else 1)
        // bos.writeBytes(freezeAuthority != null ? freezeAuthority.toByteArray() : new byte[PublicKey.PUBLIC_KEY_LENGTH]);
        if (freezeAuthority != null) {
            bos.write(freezeAuthority.toByteArray(), 0, freezeAuthority.toByteArray().size)
        } else {
            val publicKeyArray = ByteArray(PublicKey.PUBLIC_KEY_LENGTH)
            bos.write(publicKeyArray, 0, publicKeyArray.size)
        }
        return TransactionInstruction(tokenProgramId, keys, bos.toByteArray())
    }

    fun createTransferCheckedInstruction(
        tokenProgramId: PublicKey,
        source: PublicKey,
        mint: PublicKey,
        destination: PublicKey,
        owner: PublicKey,
        amount: BigInteger,
        decimals: Int
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(source, isSigner = false, isWritable = true))
        keys.add(AccountMeta(mint, isSigner = false, isWritable = false))
        keys.add(AccountMeta(destination, isSigner = false, isWritable = true))
        keys.add(AccountMeta(owner, isSigner = true, isWritable = false))
        val bos = ByteArrayOutputStream()
        bos.write(INSTRUCTION_INDEX_TRANSFER_CHECKED)
        try {
            ByteUtils.uint64ToByteStreamLE(amount, bos)
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        }
        bos.write(decimals)
        return TransactionInstruction(tokenProgramId, keys, bos.toByteArray())
    }

    fun initializeAccountInstruction(
        tokenProgramId: PublicKey = PROGRAM_ID,
        account: PublicKey,
        mint: PublicKey,
        owner: PublicKey
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(account, isSigner = false, isWritable = true))
        keys.add(AccountMeta(mint, isSigner = false, isWritable = false))
        keys.add(AccountMeta(owner, isSigner = false, isWritable = false))
        keys.add(AccountMeta(SYSVAR_RENT_ADDRESS, isSigner = false, isWritable = false))
        val data = byteArrayOf(INSTRUCTION_INDEX_INITIALIZE_ACCOUNT.toByte())
        return TransactionInstruction(tokenProgramId, keys, data)
    }

    fun transferInstruction(
        tokenProgramId: PublicKey,
        source: PublicKey,
        destination: PublicKey,
        owner: PublicKey,
        amount: BigInteger?
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(source, isSigner = false, isWritable = true))
        keys.add(AccountMeta(destination, isSigner = false, isWritable = true))
        keys.add(AccountMeta(owner, isSigner = true, isWritable = true))
        val bos = ByteArrayOutputStream()
        bos.write(INSTRUCTION_INDEX_TRANSFER)
        try {
            ByteUtils.uint64ToByteStreamLE(amount, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return TransactionInstruction(tokenProgramId, keys, bos.toByteArray())
    }

    fun approveInstruction(
        tokenProgramId: PublicKey,
        account: PublicKey?,
        delegate: PublicKey,
        owner: PublicKey,
        amount: BigInteger
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(account!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(delegate, isSigner = false, isWritable = false))
        keys.add(AccountMeta(owner, isSigner = true, isWritable = false))
        val bos = ByteArrayOutputStream()
        bos.write(INSTRUCTION_INDEX_APPROVE)
        try {
            ByteUtils.uint64ToByteStreamLE(amount, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return TransactionInstruction(tokenProgramId, keys, bos.toByteArray())
    }

    fun mintToInstruction(
        tokenProgramId: PublicKey,
        mint: PublicKey,
        destination: PublicKey,
        authority: PublicKey,
        amount: BigInteger
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(mint, isSigner = false, isWritable = true))
        keys.add(AccountMeta(destination, isSigner = false, isWritable = true))
        keys.add(AccountMeta(authority, isSigner = true, isWritable = true))
        val bos = ByteArrayOutputStream()
        bos.write(INSTRUCTION_INDEX_MINT_TO)
        try {
            ByteUtils.uint64ToByteStreamLE(amount, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return TransactionInstruction(tokenProgramId, keys, bos.toByteArray())
    }

    fun closeAccountInstruction(
        tokenProgramId: PublicKey = PROGRAM_ID,
        account: PublicKey,
        destination: PublicKey,
        owner: PublicKey
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(account, isSigner = false, isWritable = true))
        keys.add(AccountMeta(destination, isSigner = false, isWritable = true))
        keys.add(AccountMeta(owner, isSigner = false, isWritable = false))
        val data = byteArrayOf(INSTRUCTION_INDEX_CLOSE_ACCOUNT.toByte())
        return TransactionInstruction(tokenProgramId, keys, data)
    }

    class MintData private constructor(data: ByteArray) : AbstractData(data, MINT_DATA_LENGTH) {

        private val mintAuthorityOption: Long = readUint32()

        var mintAuthority: String?
        val supply: BigInteger
        val decimals: Int
        val isInitialized: Boolean
        private val freezeAuthorityOption: Long
        var freezeAuthority: PublicKey?

        val mintAuthorityPublicKey: PublicKey
            get() = PublicKey(mintAuthority!!)

        companion object {
            const val MINT_DATA_LENGTH = (
                ByteUtils.UINT_32_LENGTH + PublicKey.PUBLIC_KEY_LENGTH +
                    ByteUtils.UINT_64_LENGTH + 1 + 1 + ByteUtils.UINT_32_LENGTH + PublicKey.PUBLIC_KEY_LENGTH
                )

            fun decode(data: ByteArray): MintData {
                return MintData(data)
            }
        }

        init {
            mintAuthority = readPublicKey().toBase58()
            supply = readUint64()
            decimals = readByte().toInt()
            isInitialized = readByte().toInt() != 0
            freezeAuthorityOption = readUint32()
            freezeAuthority = readPublicKey()
            if (mintAuthorityOption == 0L) {
                mintAuthority = null
            }
            if (freezeAuthorityOption == 0L) {
                freezeAuthority = null
            }
        }
    }

    class AccountInfoData private constructor(data: ByteArray) : AbstractData(data, ACCOUNT_INFO_DATA_LENGTH) {
        val mint: PublicKey = readPublicKey()
        val owner: PublicKey = readPublicKey()
        val amount: BigInteger = readUint64()
        private val delegateOption: Long = readUint32()

        companion object {

            const val ACCOUNT_INFO_DATA_LENGTH = (
                PublicKey.PUBLIC_KEY_LENGTH + PublicKey.PUBLIC_KEY_LENGTH +
                    ByteUtils.UINT_64_LENGTH + ByteUtils.UINT_32_LENGTH + PublicKey.PUBLIC_KEY_LENGTH + 1 +
                    ByteUtils.UINT_32_LENGTH + ByteUtils.UINT_64_LENGTH + ByteUtils.UINT_64_LENGTH +
                    ByteUtils.UINT_32_LENGTH + PublicKey.PUBLIC_KEY_LENGTH
                )

            fun decode(data: ByteArray): AccountInfoData {
                return AccountInfoData(data)
            }
        }

        var delegate: PublicKey?
        val isInitialized: Boolean
        val isFrozen: Boolean
        private val state: Int
        private val isNativeOption: Long
        var rentExemptReserve: BigInteger? = null
        private val isNativeRaw: BigInteger
        var isNative = false
        var delegatedAmount: BigInteger
        private val closeAuthorityOption: Long
        var closeAuthority: PublicKey?

        init {
            delegate = readPublicKey()
            state = readByte().toInt()
            isNativeOption = readUint32()
            isNativeRaw = readUint64()
            delegatedAmount = readUint64()
            closeAuthorityOption = readUint32()
            closeAuthority = readPublicKey()
            if (delegateOption == 0L) {
                delegate = null
                delegatedAmount = BigInteger.valueOf(0)
            }
            isInitialized = state != 0
            isFrozen = state == 2
            if (isNativeOption == 1L) {
                rentExemptReserve = isNativeRaw
                isNative = true
            } else {
                rentExemptReserve = null
                isNative = false
            }
            if (closeAuthorityOption == 0L) {
                closeAuthority = null
            }
        }
    }
}