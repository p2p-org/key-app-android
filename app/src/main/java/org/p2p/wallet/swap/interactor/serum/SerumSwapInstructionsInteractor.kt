package org.p2p.wallet.swap.interactor.serum

import org.p2p.wallet.main.model.Token
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import java.math.BigInteger

class SerumSwapInstructionsInteractor(
    private val rpcRepository: RpcRepository
) {

    suspend fun prepareValidAccountAndInstructions(
        myAccount: PublicKey,
        address: PublicKey?,
        mint: PublicKey,
        initAmount: BigInteger,
        feePayer: PublicKey,
        closeAfterward: Boolean
    ): AccountInstructions {
        if (mint.toBase58() == Token.WRAPPED_SOL_MINT) {
            val accountInstructions = prepareSourceAccountAndInstructions(
                myNativeWallet = myAccount,
                source = address ?: myAccount,
                sourceMint = mint,
                amount = BigInteger.ZERO,
                feePayer = feePayer
            )

            val isCreatingWSOL = myAccount.toBase58() == (address ?: myAccount).toBase58() &&
                accountInstructions.instructions.size > 1

            // transfer
            if (isCreatingWSOL && initAmount.compareTo(BigInteger.ZERO) != 0) {
                val transferInstruction = SystemProgram.transfer(
                    fromPublicKey = myAccount,
                    toPublickKey = accountInstructions.account,
                    lamports = initAmount.toLong()
                )

                accountInstructions.instructions.add(1, transferInstruction)
            }

            return accountInstructions
        }

        return prepareDestinationAccountAndInstructions(
            myAccount = myAccount,
            destination = address,
            destinationMint = mint,
            feePayer = feePayer,
            closeAfterward = closeAfterward
        )
    }

    // MARK: - Account and instructions
    suspend fun prepareSourceAccountAndInstructions(
        myNativeWallet: PublicKey,
        source: PublicKey,
        sourceMint: PublicKey,
        amount: BigInteger,
        feePayer: PublicKey
    ): AccountInstructions {
        // if token is non-native
        if (source.toBase58() != myNativeWallet.toBase58()) {
            return AccountInstructions(source)
        }

        // if token is native
        return prepareForCreatingTempAccountAndClose(
            source = source,
            amount = amount,
            payer = feePayer
        )
    }

    private suspend fun prepareForCreatingTempAccountAndClose(
        source: PublicKey,
        amount: BigInteger,
        payer: PublicKey
    ): AccountInstructions {
        val minBalanceForRentExemption = rpcRepository.getMinimumBalanceForRentExemption(
            TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH.toLong()
        )

        // create new account
        val newAccount = Account()

        return AccountInstructions(
            account = newAccount.publicKey,
            instructions = mutableListOf(
                SystemProgram.createAccount(
                    fromPublicKey = source,
                    newAccountPublicKey = newAccount.publicKey,
                    lamports = amount.toLong() + minBalanceForRentExemption
                ),
                TokenProgram.initializeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    newAccount.publicKey,
                    Token.WRAPPED_SOL_MINT.toPublicKey(),
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
            secretKey = newAccount.secretKey
        )
    }

    suspend fun prepareDestinationAccountAndInstructions(
        myAccount: PublicKey,
        destination: PublicKey?,
        destinationMint: PublicKey,
        feePayer: PublicKey,
        closeAfterward: Boolean
    ): AccountInstructions {
        // if destination is a registered non-native token account
        if (destination != null && destination.toBase58() != myAccount.toBase58()) {
            return AccountInstructions(destination)
        }

        // if destination is a native account or is null
        return prepareForCreatingAssociatedTokenAccount(
            owner = myAccount,
            mint = destinationMint,
            feePayer = feePayer,
            closeAfterward = closeAfterward
        )
    }

    private suspend fun prepareForCreatingAssociatedTokenAccount(
        owner: PublicKey,
        mint: PublicKey,
        feePayer: PublicKey,
        closeAfterward: Boolean
    ): AccountInstructions {
        val associatedAddress = TokenTransaction.getAssociatedTokenAddress(mint, owner)

        val info = rpcRepository.getAccountInfo(associatedAddress)
        // check if associated address is registered
        val accountInfo = TokenTransaction.parseAccountInfoData(info, TokenProgram.PROGRAM_ID)

        val isRegistered = if (accountInfo != null) true else {
            throw IllegalStateException("Associated token account belongs to another user")
        }

        // cleanup instructions
        val cleanupInstructions = mutableListOf<TransactionInstruction>()
        if (closeAfterward) {
            cleanupInstructions.add(
                TokenProgram.closeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    associatedAddress,
                    owner,
                    owner
                )
            )
        }

        // if associated address is registered, there is no need to creating it again
        if (isRegistered) {
            return AccountInstructions(
                account = associatedAddress,
                cleanupInstructions = cleanupInstructions
            )
        }

        // create associated address
        return AccountInstructions(
            account = associatedAddress,
            instructions = mutableListOf(
                TokenProgram.createAssociatedTokenAccountInstruction(
                    TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                    TokenProgram.PROGRAM_ID,
                    mint,
                    associatedAddress,
                    owner,
                    feePayer
                )
            ),
            cleanupInstructions = cleanupInstructions,
            newWalletPubkey = associatedAddress.toBase58()
        )
    }
}