package org.p2p.wallet.swap.interactor.orca

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.programs.TokenSwapProgram
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.swap.model.OrcaInstructionsData
import org.p2p.wallet.swap.model.orca.OrcaPool
import java.math.BigInteger

class OrcaInstructionsInteractor(
    private val orcaAddressInteractor: TransactionAddressInteractor,
) {

    suspend fun buildDestinationInstructions(
        owner: PublicKey,
        destination: PublicKey?,
        destinationMint: PublicKey,
        feePayer: PublicKey,
        closeAfterward: Boolean
    ): OrcaInstructionsData {
        val instructions = mutableListOf<TransactionInstruction>()

        // if destination is a registered non-native token account
        if (destination != null && !destination.equals(owner)) {
            return OrcaInstructionsData(destination, instructions)
        }

        // if destination is a native account or is null
        val addressData = orcaAddressInteractor.findSplTokenAddressData(owner, destinationMint.toBase58())

        if (addressData.shouldCreateAccount) {
            val createAccount = TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                destinationMint,
                addressData.destinationAddress,
                feePayer,
                feePayer
            )

            instructions.add(createAccount)
        }

        val closeInstructions = mutableListOf<TransactionInstruction>()
        if (closeAfterward) {
            closeInstructions += TokenProgram.closeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                addressData.destinationAddress,
                owner,
                owner
            )
        }

        return OrcaInstructionsData(addressData.destinationAddress, instructions)
    }

    fun createSwapInstruction(
        pool: OrcaPool,
        userTransferAuthorityPubkey: PublicKey,
        sourceTokenAddress: PublicKey,
        destinationTokenAddress: PublicKey,
        amountIn: BigInteger,
        minAmountOut: BigInteger
    ): TransactionInstruction =
        TokenSwapProgram.swapInstruction(
            pool.account,
            pool.authority,
            userTransferAuthorityPubkey,
            sourceTokenAddress,
            pool.tokenAccountA,
            pool.tokenAccountB,
            destinationTokenAddress,
            pool.poolTokenMint,
            pool.feeAccount,
            pool.hostFeeAccount,
            TokenProgram.PROGRAM_ID,
            pool.swapProgramId,
            amountIn,
            minAmountOut
        )
}
