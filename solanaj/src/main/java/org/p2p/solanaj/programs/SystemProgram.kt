package org.p2p.solanaj.programs

import org.bitcoinj.core.Utils
import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import java.math.BigInteger

object SystemProgram {
    val PROGRAM_ID = PublicKey("11111111111111111111111111111111")
    val SPL_TOKEN_PROGRAM_ID = PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")
    private val SYSVAR_RENT_ADDRESS = PublicKey("SysvarRent111111111111111111111111111111111")
    private const val PROGRAM_INDEX_CREATE_ACCOUNT = 0
    private const val PROGRAM_INDEX_TRANSFER = 2

    fun transfer(
        fromPublicKey: PublicKey,
        toPublicKey: PublicKey,
        lamports: BigInteger
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(fromPublicKey, isSigner = true, isWritable = true))
        keys.add(AccountMeta(toPublicKey, isSigner = false, isWritable = true))

        // 4 byte instruction index + 8 bytes lamports
        val data = ByteArray(4 + 8)
        Utils.uint32ToByteArrayLE(PROGRAM_INDEX_TRANSFER.toLong(), data, 0)
        Utils.uint32ToByteArrayLE(lamports.toLong(), data, 4)
        return TransactionInstruction(PROGRAM_ID, keys, data)
    }

    fun createAccount(
        fromPublicKey: PublicKey,
        newAccountPublicKey: PublicKey,
        lamports: Long,
        space: Long = TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH.toLong(),
        programId: PublicKey = TokenProgram.PROGRAM_ID
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(fromPublicKey, isSigner = true, isWritable = true))
        keys.add(AccountMeta(newAccountPublicKey, isSigner = true, isWritable = true))
        val data = ByteArray(4 + 8 + 8 + 32)
        Utils.uint32ToByteArrayLE(PROGRAM_INDEX_CREATE_ACCOUNT.toLong(), data, 0)
        Utils.int64ToByteArrayLE(lamports, data, 4)
        Utils.int64ToByteArrayLE(space, data, 12)
        System.arraycopy(programId.asByteArray(), 0, data, 20, 32)
        return TransactionInstruction(PROGRAM_ID, keys, data)
    }

    fun initializeAccountInstruction(
        account: PublicKey,
        mint: PublicKey,
        owner: PublicKey
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(account, isSigner = false, isWritable = true))
        keys.add(AccountMeta(mint, isSigner = false, isWritable = false))
        keys.add(AccountMeta(owner, isSigner = false, isWritable = false))
        keys.add(AccountMeta(SYSVAR_RENT_ADDRESS, isSigner = false, isWritable = false))
        val data = byteArrayOf(1)
        return TransactionInstruction(SPL_TOKEN_PROGRAM_ID, keys, data)
    }
}
