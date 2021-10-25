package org.p2p.wallet.swap.interactor.orca

import org.p2p.wallet.main.model.Token
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.model.orca.OrcaInstructionsData
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import java.math.BigInteger

class OrcaInstructionsInteractor(
    private val rpcRepository: RpcRepository,
    private val orcaAddressInteractor: OrcaAddressInteractor,
    private val userInteractor: UserInteractor
) {

    suspend fun buildSourceInstructions(
        source: PublicKey,
        pool: OrcaPool,
        amount: BigInteger,
        sourceMint: PublicKey,
        destinationMint: PublicKey,
        feePayer: PublicKey
    ): OrcaInstructionsData {
        val sourceTokenAccount = pool.tokenAccountA

        val accountInfo = rpcRepository.getAccountInfo(sourceTokenAccount)
        val sourceAccountInfo = TokenTransaction.getAccountInfoData(accountInfo, TokenProgram.PROGRAM_ID)
        val space = TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH.toLong()
        val balanceNeeded = rpcRepository.getMinimumBalanceForRentExemption(space)

        val wrappedSolAccount = Token.WRAPPED_SOL_MINT.toPublicKey()

        val instructions = mutableListOf<TransactionInstruction>()
        val signers = mutableListOf<Account>()

        val newAccount = Account()
        val fromAccount: PublicKey = if (sourceAccountInfo.isNative) {
            val newAccountPublicKey = newAccount.publicKey
            val createAccountInstruction = SystemProgram.createAccount(
                fromPublicKey = source,
                newAccountPublicKey = newAccountPublicKey,
                lamports = amount.toLong() + balanceNeeded,
                space = TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH.toLong(),
                programId = TokenProgram.PROGRAM_ID
            )
            val initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                newAccountPublicKey,
                wrappedSolAccount,
                feePayer
            )
            instructions.add(createAccountInstruction)
            instructions.add(initializeAccountInstruction)
            signers.add(newAccount)
            newAccountPublicKey
        } else {
            userInteractor.findAccountAddress(sourceMint.toBase58())?.publicKey?.toPublicKey()
                ?: throw IllegalStateException("Source token is not in user's tokens list")
        }

        val isWrappedSol = destinationMint.equals(wrappedSolAccount)
        val isNeedCloseAccount = sourceAccountInfo.isNative || isWrappedSol
        val closeAccountPublicKey = if (sourceAccountInfo.isNative) fromAccount else null

        val cleanupInstructions = mutableListOf<TransactionInstruction>()
        if (isNeedCloseAccount && closeAccountPublicKey != null) {
            val closeAccountInstruction = TokenProgram.closeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                closeAccountPublicKey,
                feePayer,
                feePayer
            )
            cleanupInstructions.add(closeAccountInstruction)
        }

        return OrcaInstructionsData(fromAccount, instructions, cleanupInstructions, signers)
    }

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

            instructions.add(createAccount)
        }

        val closeInstructions = mutableListOf<TransactionInstruction>()
        if (closeAfterward) {
            closeInstructions += TokenProgram.closeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                addressData.associatedAddress,
                owner,
                owner
            )
        }

        return OrcaInstructionsData(addressData.associatedAddress, instructions)
    }
}