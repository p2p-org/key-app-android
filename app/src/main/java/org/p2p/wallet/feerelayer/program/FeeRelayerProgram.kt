package org.p2p.wallet.feerelayer.program

import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Sysvar
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.utils.ByteUtils
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.utils.toPublicKey
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger

object FeeRelayerProgram {

    fun createRelayTopUpSwapTransitiveInstruction(
        programId: PublicKey,
        feePayer: PublicKey,
        userAuthority: PublicKey,
        userRelayAccount: PublicKey,
        userTransferAuthority: PublicKey,
        userSourceTokenAccount: PublicKey,
        userTransitTokenAccount: PublicKey,
        userDestinationTokenAccount: PublicKey,
        swapFromProgramId: PublicKey,
        swapFromAccount: PublicKey,
        swapFromAuthority: PublicKey,
        swapFromSource: PublicKey,
        swapFromDestination: PublicKey,
        swapFromPoolTokenMint: PublicKey,
        swapFromPoolFeeAccount: PublicKey,
        swapToProgramId: PublicKey,
        swapToAccount: PublicKey,
        swapToAuthority: PublicKey,
        swapToSource: PublicKey,
        swapToDestination: PublicKey,
        swapToPoolTokenMint: PublicKey,
        swapToPoolFeeAccount: PublicKey,
        amountIn: BigInteger,
        transitMinimumAmount: BigInteger,
        minimumAmountOut: BigInteger,
    ): TransactionInstruction {
        val keys = listOf(
            AccountMeta(publicKey = Token.WRAPPED_SOL_MINT.toPublicKey(), isSigner = false, isWritable = false),
            AccountMeta(publicKey = feePayer, isSigner = true, isWritable = true),
            AccountMeta(publicKey = userAuthority, isSigner = true, isWritable = false),
            AccountMeta(publicKey = userRelayAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = TokenProgram.PROGRAM_ID, isSigner = false, isWritable = false),
            AccountMeta(publicKey = userTransferAuthority, isSigner = true, isWritable = false),
            AccountMeta(publicKey = userSourceTokenAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = userTransitTokenAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = userDestinationTokenAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = swapFromProgramId, isSigner = false, isWritable = false),
            AccountMeta(publicKey = swapFromAccount, isSigner = false, isWritable = false),
            AccountMeta(publicKey = swapFromAuthority, isSigner = false, isWritable = false),
            AccountMeta(publicKey = swapFromSource, isSigner = false, isWritable = true),
            AccountMeta(publicKey = swapFromDestination, isSigner = false, isWritable = true),
            AccountMeta(publicKey = swapFromPoolTokenMint, isSigner = false, isWritable = true),
            AccountMeta(publicKey = swapFromPoolFeeAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = swapToProgramId, isSigner = false, isWritable = false),
            AccountMeta(publicKey = swapToAccount, isSigner = false, isWritable = false),
            AccountMeta(publicKey = swapToAuthority, isSigner = false, isWritable = false),
            AccountMeta(publicKey = swapToSource, isSigner = false, isWritable = true),
            AccountMeta(publicKey = swapToDestination, isSigner = false, isWritable = true),
            AccountMeta(publicKey = swapToPoolTokenMint, isSigner = false, isWritable = true),
            AccountMeta(publicKey = swapToPoolFeeAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = Sysvar.SYSVAR_RENT_ADDRESS, isSigner = false, isWritable = false),
            AccountMeta(publicKey = SystemProgram.PROGRAM_ID, isSigner = false, isWritable = false),
        )

        val bos = ByteArrayOutputStream()
        bos.write(1)

        try {
            ByteUtils.uint64ToByteStreamLE(amountIn, bos)
            ByteUtils.uint64ToByteStreamLE(transitMinimumAmount, bos)
            ByteUtils.uint64ToByteStreamLE(minimumAmountOut, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return TransactionInstruction(programId, keys, bos.toByteArray())
    }

    fun topUpSwapDirectInstruction(
        programId: PublicKey,
        feePayer: PublicKey,
        userAuthority: PublicKey,
        userRelayAccount: PublicKey,
        userTransferAuthority: PublicKey,
        userSourceTokenAccount: PublicKey,
        userTemporaryWsolAccount: PublicKey,
        swapProgramId: PublicKey,
        swapAccount: PublicKey,
        swapAuthority: PublicKey,
        swapSource: PublicKey,
        swapDestination: PublicKey,
        poolTokenMint: PublicKey,
        poolFeeAccount: PublicKey,
        amountIn: BigInteger,
        minimumAmountOut: BigInteger,
    ): TransactionInstruction {

        val keys = listOf(
            AccountMeta(publicKey = Token.WRAPPED_SOL_MINT.toPublicKey(), isSigner = false, isWritable = false),
            AccountMeta(publicKey = feePayer, isSigner = true, isWritable = true),
            AccountMeta(publicKey = userAuthority, isSigner = true, isWritable = false),
            AccountMeta(publicKey = userRelayAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = TokenProgram.PROGRAM_ID, isSigner = false, isWritable = false),
            AccountMeta(publicKey = swapProgramId, isSigner = false, isWritable = false),
            AccountMeta(publicKey = swapAccount, isSigner = false, isWritable = false),
            AccountMeta(publicKey = swapAuthority, isSigner = false, isWritable = false),
            AccountMeta(publicKey = userTransferAuthority, isSigner = true, isWritable = false),
            AccountMeta(publicKey = userSourceTokenAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = userTemporaryWsolAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = swapSource, isSigner = false, isWritable = true),
            AccountMeta(publicKey = swapDestination, isSigner = false, isWritable = true),
            AccountMeta(publicKey = poolTokenMint, isSigner = false, isWritable = true),
            AccountMeta(publicKey = poolFeeAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = Sysvar.SYSVAR_RENT_ADDRESS, isSigner = false, isWritable = false),
            AccountMeta(publicKey = SystemProgram.PROGRAM_ID, isSigner = false, isWritable = false),
        )

        val bos = ByteArrayOutputStream()
        bos.write(0)

        try {
            ByteUtils.uint64ToByteStreamLE(amountIn, bos)
            ByteUtils.uint64ToByteStreamLE(minimumAmountOut, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return TransactionInstruction(programId, keys, bos.toByteArray())
    }

    fun createRelayTransferSolInstruction(
        programId: PublicKey,
        userAuthority: PublicKey,
        userRelayAccount: PublicKey,
        recipient: PublicKey,
        amount: BigInteger,
    ): TransactionInstruction {
        val keys = listOf(
            AccountMeta(publicKey = userAuthority, isSigner = true, isWritable = false),
            AccountMeta(publicKey = userRelayAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = recipient, isSigner = false, isWritable = true),
            AccountMeta(publicKey = SystemProgram.PROGRAM_ID, isSigner = false, isWritable = false),
        )

        val bos = ByteArrayOutputStream()
        bos.write(2)

        try {
            ByteUtils.uint64ToByteStreamLE(amount, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return TransactionInstruction(programId, keys, bos.toByteArray())
    }

    fun createTransitTokenAccount(
        programId: PublicKey,
        feePayer: PublicKey,
        userAuthority: PublicKey,
        transitTokenAccount: PublicKey,
        transitTokenMint: PublicKey,
    ): TransactionInstruction {
        val keys = listOf(
            AccountMeta(publicKey = transitTokenAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = transitTokenMint, isSigner = false, isWritable = false),
            AccountMeta(publicKey = userAuthority, isSigner = true, isWritable = true),
            AccountMeta(publicKey = feePayer, isSigner = true, isWritable = false),
            AccountMeta(publicKey = TokenProgram.PROGRAM_ID, isSigner = false, isWritable = false),
            AccountMeta(publicKey = Sysvar.SYSVAR_RENT_ADDRESS, isSigner = false, isWritable = false),
            AccountMeta(publicKey = SystemProgram.PROGRAM_ID, isSigner = false, isWritable = false),
        )

        val bos = ByteArrayOutputStream()
        bos.write(3)

        return TransactionInstruction(programId, keys, bos.toByteArray())
    }
}