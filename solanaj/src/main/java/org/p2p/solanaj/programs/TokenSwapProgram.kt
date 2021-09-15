package org.p2p.solanaj.programs

import org.p2p.solanaj.core.AbstractData
import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.utils.ByteUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.util.ArrayList

object TokenSwapProgram {
    const val INSTRUCTION_INDEX_INITIALIZE_SWAP = 0
    const val INSTRUCTION_INDEX_SWAP = 1
    const val INSTRUCTION_INDEX_DEPOSIT = 2
    const val INSTRUCTION_INDEX_WITHDRAW = 3

    fun initializeSwapInstruction(
        tokenSwapAccount: PublicKey?,
        authority: PublicKey?,
        tokenAccountA: PublicKey?,
        tokenAccountB: PublicKey?,
        tokenPool: PublicKey?,
        feeAccount: PublicKey?,
        tokenAccountPool: PublicKey?,
        tokenProgramId: PublicKey?,
        swapProgramId: PublicKey?,
        nonce: Int,
        curveType: Int,
        tradeFeeNumerator: BigInteger?,
        tradeFeeDenominator: BigInteger?,
        ownerTradeFeeNumerator: BigInteger?,
        ownerTradeFeeDenominator: BigInteger?,
        ownerWithdrawFeeNumerator: BigInteger?,
        ownerWithdrawFeeDenominator: BigInteger?,
        hostFeeNumerator: BigInteger?,
        hostFeeDenominator: BigInteger?
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(tokenSwapAccount!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(authority!!, isSigner = false, isWritable = false))
        keys.add(AccountMeta(tokenAccountA!!, isSigner = false, isWritable = false))
        keys.add(AccountMeta(tokenAccountB!!, isSigner = false, isWritable = false))
        keys.add(AccountMeta(tokenPool!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(feeAccount!!, isSigner = false, isWritable = false))
        keys.add(AccountMeta(tokenAccountPool!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(tokenProgramId!!, isSigner = false, isWritable = false))
        val bos = ByteArrayOutputStream()
        bos.write(INSTRUCTION_INDEX_INITIALIZE_SWAP)
        bos.write(nonce)
        try {
            ByteUtils.uint64ToByteStreamLE(tradeFeeNumerator, bos)
            ByteUtils.uint64ToByteStreamLE(tradeFeeDenominator, bos)
            ByteUtils.uint64ToByteStreamLE(ownerTradeFeeNumerator, bos)
            ByteUtils.uint64ToByteStreamLE(ownerTradeFeeDenominator, bos)
            ByteUtils.uint64ToByteStreamLE(ownerWithdrawFeeNumerator, bos)
            ByteUtils.uint64ToByteStreamLE(ownerWithdrawFeeDenominator, bos)
            ByteUtils.uint64ToByteStreamLE(hostFeeNumerator, bos)
            ByteUtils.uint64ToByteStreamLE(hostFeeDenominator, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        bos.write(curveType)
        //  bos.writeBytes(new byte[32]);
        val byteArray = ByteArray(32)
        bos.write(byteArray, 0, byteArray.size)
        return TransactionInstruction(swapProgramId!!, keys, bos.toByteArray())
    }

    fun swapInstruction(
        tokenSwapAccount: PublicKey?,
        authority: PublicKey?,
        userTransferAuthority: PublicKey?,
        userSource: PublicKey?,
        poolSource: PublicKey?,
        poolDestination: PublicKey?,
        userDestination: PublicKey?,
        poolMint: PublicKey?,
        feeAccount: PublicKey?,
        hostFeeAccount: PublicKey?,
        tokenProgramId: PublicKey?,
        swapProgramId: PublicKey?,
        amountIn: BigInteger?,
        minimumAmountOut: BigInteger?
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(tokenSwapAccount!!, false, false))
        keys.add(AccountMeta(authority!!, false, false))
        keys.add(AccountMeta(userTransferAuthority!!, true, false))
        keys.add(AccountMeta(userSource!!, false, true))
        keys.add(AccountMeta(poolSource!!, false, true))
        keys.add(AccountMeta(poolDestination!!, false, true))
        keys.add(AccountMeta(userDestination!!, false, true))
        keys.add(AccountMeta(poolMint!!, false, true))
        keys.add(AccountMeta(feeAccount!!, false, true))
        keys.add(AccountMeta(tokenProgramId!!, false, false))
        if (hostFeeAccount != null) {
            keys.add(AccountMeta(hostFeeAccount, false, true))
        }
        val bos = ByteArrayOutputStream()
        bos.write(INSTRUCTION_INDEX_SWAP)
        try {
            ByteUtils.uint64ToByteStreamLE(amountIn, bos)
            ByteUtils.uint64ToByteStreamLE(minimumAmountOut, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return TransactionInstruction(swapProgramId!!, keys, bos.toByteArray())
    }

    fun depositInstruction(
        tokenSwap: PublicKey?,
        authority: PublicKey?,
        sourceA: PublicKey?,
        sourceB: PublicKey?,
        intoA: PublicKey?,
        intoB: PublicKey?,
        poolToken: PublicKey?,
        poolAccount: PublicKey?,
        tokenProgramId: PublicKey?,
        swapProgramId: PublicKey?,
        poolTokenAmount: BigInteger?,
        maximumTokenA: BigInteger?,
        maximumTokenB: BigInteger?
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(tokenSwap!!, isSigner = false, isWritable = false))
        keys.add(AccountMeta(authority!!, isSigner = false, isWritable = false))
        keys.add(AccountMeta(sourceA!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(sourceB!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(intoA!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(intoB!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(poolToken!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(poolAccount!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(tokenProgramId!!, isSigner = false, isWritable = true))
        val bos = ByteArrayOutputStream()
        bos.write(INSTRUCTION_INDEX_DEPOSIT)
        try {
            ByteUtils.uint64ToByteStreamLE(poolTokenAmount, bos)
            ByteUtils.uint64ToByteStreamLE(maximumTokenA, bos)
            ByteUtils.uint64ToByteStreamLE(maximumTokenB, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return TransactionInstruction(swapProgramId!!, keys, bos.toByteArray())
    }

    fun withdrawInstruction(
        tokenSwap: PublicKey?,
        authority: PublicKey?,
        poolMint: PublicKey?,
        feeAccount: PublicKey?,
        sourcePoolAccount: PublicKey?,
        fromA: PublicKey?,
        fromB: PublicKey?,
        userAccountA: PublicKey?,
        userAccountB: PublicKey?,
        swapProgramId: PublicKey?,
        tokenProgramId: PublicKey?,
        poolTokenAmount: BigInteger?,
        minimumTokenA: BigInteger?,
        minimumTokenB: BigInteger?
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()
        keys.add(AccountMeta(tokenSwap!!, isSigner = false, isWritable = false))
        keys.add(AccountMeta(authority!!, isSigner = false, isWritable = false))
        keys.add(AccountMeta(poolMint!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(sourcePoolAccount!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(fromA!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(fromB!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(userAccountA!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(userAccountB!!, isSigner = false, isWritable = true))
        keys.add(AccountMeta(feeAccount!!, isSigner = false, isWritable = false))
        keys.add(AccountMeta(tokenProgramId!!, isSigner = false, isWritable = false))
        val bos = ByteArrayOutputStream()
        bos.write(INSTRUCTION_INDEX_WITHDRAW)
        try {
            ByteUtils.uint64ToByteStreamLE(poolTokenAmount, bos)
            ByteUtils.uint64ToByteStreamLE(minimumTokenA, bos)
            ByteUtils.uint64ToByteStreamLE(minimumTokenB, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return TransactionInstruction(swapProgramId!!, keys, bos.toByteArray())
    }

    class TokenSwapData private constructor(data: ByteArray) : AbstractData(data, TOKEN_SWAP_DATA_LENGTH) {
        val version: Int
        val isInitialized: Boolean
        val nonce: Int
        val tokenProgramId: PublicKey
        var tokenAccountA: PublicKey
            private set
        var tokenAccountB: PublicKey
            private set
        val tokenPool: PublicKey
        var mintA: PublicKey
            private set
        var mintB: PublicKey
            private set
        val feeAccount: PublicKey
        val tradeFeeNumerator: BigInteger
        val tradeFeeDenominator: BigInteger
        val ownerTradeFeeNumerator: BigInteger
        val ownerTradeFeeDenominator: BigInteger
        val ownerWithdrawFeeNumerator: BigInteger
        val ownerWithdrawFeeDenominator: BigInteger
        val hostFeeNumerator: BigInteger
        val hostFeeDenominator: BigInteger
        val curveType: Int

        fun swapMintData(): TokenSwapData {
            val mintAOld = mintA
            mintA = mintB
            mintB = mintAOld
            return this
        }

        fun swapTokenAccount(): TokenSwapData {
            val tokenAccountAOld = tokenAccountA
            tokenAccountA = tokenAccountB
            tokenAccountB = tokenAccountAOld
            return this
        }

        companion object {
            const val TOKEN_SWAP_DATA_LENGTH =
                1 + 1 + 1 + 7 * PublicKey.PUBLIC_KEY_LENGTH + 8 * ByteUtils.UINT_64_LENGTH + 1 + 32

            fun decode(data: ByteArray): TokenSwapData {
                return TokenSwapData(data)
            }
        }

        // private byte[] curveParameters;
        init {
            version = readByte().toInt()
            isInitialized = readByte().toInt() == 1
            nonce = readByte().toInt()
            tokenProgramId = readPublicKey()
            tokenAccountA = readPublicKey()
            tokenAccountB = readPublicKey()
            tokenPool = readPublicKey()
            mintA = readPublicKey()
            mintB = readPublicKey()
            feeAccount = readPublicKey()
            tradeFeeNumerator = readUint64()
            tradeFeeDenominator = readUint64()
            ownerTradeFeeNumerator = readUint64()
            ownerTradeFeeDenominator = readUint64()
            ownerWithdrawFeeNumerator = readUint64()
            ownerWithdrawFeeDenominator = readUint64()
            hostFeeNumerator = readUint64()
            hostFeeDenominator = readUint64()
            curveType = readByte().toInt()
            // curveParameters = new byte[32];
        }
    }
}