package org.p2p.wallet.swap.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class SwapInstructionsInteractor(
    private val rpcAmountRepository: RpcAmountRepository,
    private val orcaAddressInteractor: TransactionAddressInteractor
) {

    suspend fun prepareCreatingWSOLAccountAndCloseWhenDone(
        from: PublicKey,
        amount: BigInteger,
        payer: PublicKey
    ): AccountInstructions {
        val minBalanceForRentExemption = rpcAmountRepository.getMinBalanceForRentExemption(ACCOUNT_INFO_DATA_LENGTH)

        // create new account
        val newAccount = Account()

        return AccountInstructions(
            account = newAccount.publicKey,
            instructions = mutableListOf(
                SystemProgram.createAccount(
                    fromPublicKey = from,
                    newAccountPublicKey = newAccount.publicKey,
                    lamports = amount.toLong() + minBalanceForRentExemption.toLong()
                ),
                TokenProgram.initializeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    newAccount.publicKey,
                    WRAPPED_SOL_MINT.toPublicKey(),
                    payer
                )
            ),
            cleanupInstructions = listOf(
                TokenProgram.closeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    newAccount.publicKey,
                    payer,
                    payer
                )
            ),
            signers = listOf(newAccount),
            secretKey = newAccount.keypair
        )
    }

    suspend fun prepareForCreatingAssociatedTokenAccount(
        owner: PublicKey,
        mint: PublicKey,
        feePayer: PublicKey,
        closeAfterward: Boolean
    ): AccountInstructions {
        val addressData = orcaAddressInteractor.findSplTokenAddressData(owner, mint.toBase58())

        // cleanup instructions
        val cleanupInstructions = mutableListOf<TransactionInstruction>()
        if (closeAfterward) {
            cleanupInstructions.add(
                TokenProgram.closeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    addressData.destinationAddress,
                    owner,
                    owner
                )
            )
        }

        // if associated address is registered, there is no need to creating it again
        if (!addressData.shouldCreateAccount) {
            return AccountInstructions(
                account = addressData.destinationAddress,
                cleanupInstructions = cleanupInstructions
            )
        }

        // create associated address
        return AccountInstructions(
            account = addressData.destinationAddress,
            instructions = mutableListOf(
                TokenProgram.createAssociatedTokenAccountInstruction(
                    TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                    TokenProgram.PROGRAM_ID,
                    mint,
                    addressData.destinationAddress,
                    owner,
                    feePayer
                )
            ),
            cleanupInstructions = cleanupInstructions,
            newWalletPubkey = addressData.destinationAddress.toBase58()
        )
    }
}
