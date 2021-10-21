package com.p2p.wallet.swap.interactor.orca

import com.p2p.wallet.swap.model.orca.OrcaInstructionsData
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.programs.TokenProgram

class OrcaInstructionsInteractor(
    private val orcaAddressInteractor: OrcaAddressInteractor
) {

    suspend fun buildDestinationInstructions(
        owner: PublicKey,
        destination: PublicKey?,
        destinationMint: PublicKey,
        feePayer: PublicKey
    ): OrcaInstructionsData {
        val transactions = mutableListOf<TransactionInstruction>()

        // if destination is a registered non-native token account
        if (destination != null && !destination.equals(owner)) {
            return OrcaInstructionsData(destination, transactions)
        }

        // if destination is a native account or is nil
        val addressData = orcaAddressInteractor.findAssociatedAddress(owner, destinationMint.toBase58())

        if (addressData.shouldCreateAssociatedInstruction) {
            val createAccount = TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                destinationMint,
                addressData.associatedAddress,
                feePayer,
                feePayer
            )

            transactions.add(createAccount)
        }

        //todo: close account instruction if needed
        return OrcaInstructionsData(addressData.associatedAddress, transactions)
    }
}