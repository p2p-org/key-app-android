package org.p2p.wallet.feerelayer.program

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Sysvar
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.utils.ByteUtils
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.utils.toPublicKey

object FeeRelayerProgram {

    fun getProgramId(isMainnet: Boolean) =
        if (isMainnet) {
            PublicKey("12YKFL4mnZz6CBEGePrf293mEzueQM3h8VLPUJsKpGs9")
        } else {
            PublicKey("6xKJFyuM6UHCT8F5SBxnjGt6ZrZYjsVfnAnAeHPU775k")
        }

    fun createRelaySwapInstruction(
        programId: PublicKey,
        transitiveSwap: SwapData.SplTransitive,
        sourceAddressPubkey: PublicKey,
        transitTokenAccount: PublicKey,
        destinationAddressPubkey: PublicKey,
        feePayerPubkey: PublicKey
    ): TransactionInstruction {
        val transferAuthorityPubkey = PublicKey(transitiveSwap.from.transferAuthorityPubkey)
        val swapFromProgramId = PublicKey(transitiveSwap.from.programId)
        val swapFromAccount = PublicKey(transitiveSwap.from.accountPubkey)
        val swapFromAuthority = PublicKey(transitiveSwap.from.authorityPubkey)
        val swapFromSource = PublicKey(transitiveSwap.from.sourcePubkey)
        val swapFromDestination = PublicKey(transitiveSwap.from.destinationPubkey)
        val swapFromTokenMint = PublicKey(transitiveSwap.from.poolTokenMintPubkey)
        val swapFromPoolFeeAccount = PublicKey(transitiveSwap.from.poolFeeAccountPubkey)
        val swapToProgramId = PublicKey(transitiveSwap.to.programId)
        val swapToAccount = PublicKey(transitiveSwap.to.accountPubkey)
        val swapToAuthority = PublicKey(transitiveSwap.to.authorityPubkey)
        val swapToSource = PublicKey(transitiveSwap.to.sourcePubkey)
        val swapToDestination = PublicKey(transitiveSwap.to.destinationPubkey)
        val swapToPoolTokenMint = PublicKey(transitiveSwap.to.poolTokenMintPubkey)
        val swapToPoolFeeAccount = PublicKey(transitiveSwap.to.poolFeeAccountPubkey)
        val amountIn = transitiveSwap.from.amountIn
        val transitMinimumAmount = transitiveSwap.from.minimumAmountOut
        val minimumAmountOut = transitiveSwap.to.minimumAmountOut

        return splSwapTransitiveInstruction(
            programId = programId,
            feePayer = feePayerPubkey,
            userTransferAuthority = transferAuthorityPubkey,
            userSourceTokenAccount = sourceAddressPubkey,
            userTransitTokenAccount = transitTokenAccount,
            userDestinationTokenAccount = destinationAddressPubkey,
            swapFromProgramId = swapFromProgramId,
            swapFromAccount = swapFromAccount,
            swapFromAuthority = swapFromAuthority,
            swapFromSource = swapFromSource,
            swapFromDestination = swapFromDestination,
            swapFromPoolTokenMint = swapFromTokenMint,
            swapFromPoolFeeAccount = swapFromPoolFeeAccount,
            swapToProgramId = swapToProgramId,
            swapToAccount = swapToAccount,
            swapToAuthority = swapToAuthority,
            swapToSource = swapToSource,
            swapToDestination = swapToDestination,
            swapToPoolTokenMint = swapToPoolTokenMint,
            swapToPoolFeeAccount = swapToPoolFeeAccount,
            amountIn = amountIn,
            transitMinimumAmount = transitMinimumAmount,
            minimumAmountOut = minimumAmountOut
        )
    }

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
            AccountMeta(publicKey = WRAPPED_SOL_MINT.toPublicKey(), isSigner = false, isWritable = false),
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

    fun topUpSwapInstruction(
        feeRelayerProgramId: PublicKey,
        userRelayAddress: PublicKey,
        userTemporarilyWSOLAddress: PublicKey,
        topUpSwap: SwapData,
        userAuthorityAddress: PublicKey,
        userSourceTokenAccountAddress: PublicKey,
        feePayerAddress: PublicKey
    ): TransactionInstruction {
        return when (topUpSwap) {
            is SwapData.Direct ->
                topUpSwapDirectInstruction(
                    feeRelayerProgramId = feeRelayerProgramId,
                    feePayer = feePayerAddress,
                    userAuthority = userAuthorityAddress,
                    userRelayAccount = userRelayAddress,
                    userTransferAuthority = PublicKey(topUpSwap.transferAuthorityPubkey),
                    userSourceTokenAccount = userSourceTokenAccountAddress,
                    userTemporaryWsolAccount = userTemporarilyWSOLAddress,
                    swapProgramId = PublicKey(topUpSwap.programId),
                    swapAccount = PublicKey(topUpSwap.accountPubkey),
                    swapAuthority = PublicKey(topUpSwap.authorityPubkey),
                    swapSource = PublicKey(topUpSwap.sourcePubkey),
                    swapDestination = PublicKey(topUpSwap.destinationPubkey),
                    poolTokenMint = PublicKey(topUpSwap.poolTokenMintPubkey),
                    poolFeeAccount = PublicKey(topUpSwap.poolFeeAccountPubkey),
                    amountIn = topUpSwap.amountIn,
                    minimumAmountOut = topUpSwap.minimumAmountOut,
                )
            is SwapData.SplTransitive ->
                topUpWithSPLSwapTransitiveInstruction(
                    feeRelayerProgramId = feeRelayerProgramId,
                    feePayer = feePayerAddress,
                    userAuthority = userAuthorityAddress,
                    userRelayAccount = userRelayAddress,
                    userTransferAuthority = PublicKey(topUpSwap.from.transferAuthorityPubkey),
                    userSourceTokenAccount = userSourceTokenAccountAddress,
                    userDestinationTokenAccount = userTemporarilyWSOLAddress,
                    transitTokenAccountAddress = topUpSwap.transitTokenAccountAddress,
                    swapFromProgramId = PublicKey(topUpSwap.from.programId),
                    swapFromAccount = PublicKey(topUpSwap.from.accountPubkey),
                    swapFromAuthority = PublicKey(topUpSwap.from.authorityPubkey),
                    swapFromSource = PublicKey(topUpSwap.from.sourcePubkey),
                    swapFromDestination = PublicKey(topUpSwap.from.destinationPubkey),
                    swapFromPoolTokenMint = PublicKey(topUpSwap.from.poolTokenMintPubkey),
                    swapFromPoolFeeAccount = PublicKey(topUpSwap.from.poolFeeAccountPubkey),
                    swapToProgramId = PublicKey(topUpSwap.to.programId),
                    swapToAccount = PublicKey(topUpSwap.to.accountPubkey),
                    swapToAuthority = PublicKey(topUpSwap.to.authorityPubkey),
                    swapToSource = PublicKey(topUpSwap.to.sourcePubkey),
                    swapToDestination = PublicKey(topUpSwap.to.destinationPubkey),
                    swapToPoolTokenMint = PublicKey(topUpSwap.to.poolTokenMintPubkey),
                    swapToPoolFeeAccount = PublicKey(topUpSwap.to.poolFeeAccountPubkey),
                    amountIn = topUpSwap.from.amountIn,
                    transitMinimumAmount = topUpSwap.from.minimumAmountOut,
                    minimumAmountOut = topUpSwap.to.minimumAmountOut
                )
        }
    }

    fun topUpSwapDirectInstruction(
        feeRelayerProgramId: PublicKey,
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
            AccountMeta(publicKey = WRAPPED_SOL_MINT.toPublicKey(), isSigner = false, isWritable = false),
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

        return TransactionInstruction(feeRelayerProgramId, keys, bos.toByteArray())
    }

    fun topUpWithSPLSwapTransitiveInstruction(
        feeRelayerProgramId: PublicKey,
        feePayer: PublicKey,
        userAuthority: PublicKey,
        userRelayAccount: PublicKey,
        userTransferAuthority: PublicKey,
        userSourceTokenAccount: PublicKey,
        userDestinationTokenAccount: PublicKey,
        transitTokenAccountAddress: PublicKey,
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
            AccountMeta(publicKey = WRAPPED_SOL_MINT.toPublicKey(), isSigner = false, isWritable = false),
            AccountMeta(publicKey = feePayer, isSigner = true, isWritable = true),
            AccountMeta(publicKey = userAuthority, isSigner = true, isWritable = false),
            AccountMeta(publicKey = userRelayAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = TokenProgram.PROGRAM_ID, isSigner = false, isWritable = false),
            AccountMeta(publicKey = userTransferAuthority, isSigner = true, isWritable = false),
            AccountMeta(publicKey = userSourceTokenAccount, isSigner = false, isWritable = true),
            AccountMeta(publicKey = transitTokenAccountAddress, isSigner = false, isWritable = true),
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
            AccountMeta(publicKey = SystemProgram.PROGRAM_ID, isSigner = false, isWritable = false)
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

        return TransactionInstruction(feeRelayerProgramId, keys, bos.toByteArray())
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

    fun createTransitTokenAccountInstruction(
        feeRelayerProgramId: PublicKey,
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

        return TransactionInstruction(feeRelayerProgramId, keys, bos.toByteArray())
    }

    private fun splSwapTransitiveInstruction(
        programId: PublicKey,
        feePayer: PublicKey,
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
        minimumAmountOut: BigInteger
    ): TransactionInstruction {
        val keys = listOf(
            AccountMeta(publicKey = feePayer, isSigner = true, isWritable = true),
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
        )

        val bos = ByteArrayOutputStream()
        bos.write(4)

        try {
            ByteUtils.uint64ToByteStreamLE(amountIn, bos)
            ByteUtils.uint64ToByteStreamLE(transitMinimumAmount, bos)
            ByteUtils.uint64ToByteStreamLE(minimumAmountOut, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return TransactionInstruction(programId, keys, bos.toByteArray())
    }
}
