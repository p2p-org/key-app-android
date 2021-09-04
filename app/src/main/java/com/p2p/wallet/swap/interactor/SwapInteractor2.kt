package com.p2p.wallet.swap.interactor

import android.util.Base64
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.TransactionInstruction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import java.math.BigInteger

class SwapInteractor2(
    private val rpcRepository: RpcRepository
) {

    suspend fun sendTransaction(serializedTransaction: String) : String =
        rpcRepository.sendTransaction(serializedTransaction)

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
    private suspend fun prepareSourceAccountAndInstructions(
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
                    newAccountPublikkey = newAccount.publicKey,
                    lamports = amount.toLong() + minBalanceForRentExemption
                ),
                TokenProgram.initializeAccountInstruction(
                    account = newAccount.publicKey,
                    mint = Token.WRAPPED_SOL_MINT.toPublicKey(),
                    owner = payer
                )
            ),
            cleanupInstructions = listOf(
                TokenProgram.closeAccountInstruction(
                    account = newAccount.publicKey,
                    destination = payer,
                    owner = payer
                )
            ),
            signers = listOf(newAccount),
            secretKey = newAccount.secretKey
        )
    }

    private suspend fun prepareDestinationAccountAndInstructions(
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
        val isRegistered = if (info?.value != null && info.value.owner == TokenProgram.PROGRAM_ID.toBase58()) {
            val base64Data = info.value.data!![0]
            val data = Base64.decode(base64Data, Base64.DEFAULT)
            val account = TokenProgram.AccountInfoData.decode(data)
            if (account.owner.toBase58() == TokenProgram.PROGRAM_ID.toBase58()) {
                true
            } else {
                throw IllegalStateException("Associated token account belongs to another user")
            }
        } else throw IllegalStateException("Associated token account belongs to another user")

        // cleanup intructions
        val cleanupInstructions = mutableListOf<TransactionInstruction>()
        if (closeAfterward) {
            cleanupInstructions.add(
                TokenProgram.closeAccountInstruction(
                    account = associatedAddress,
                    destination = owner,
                    owner = owner
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
                    mint = mint,
                    associatedAccount = associatedAddress,
                    owner = owner,
                    payer = feePayer
                )
            ),
            cleanupInstructions = cleanupInstructions,
            newWalletPubkey = associatedAddress.toBase58()
        )
    }
}