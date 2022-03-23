package org.p2p.wallet.rpc.interactor

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.R
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.model.AddressValidation
import org.p2p.wallet.swap.model.orca.TransactionAddressData
import org.p2p.wallet.user.repository.UserAccountRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber

private const val ADDRESS_TAG = "Address"

class TransactionAddressInteractor(
    private val userAccountRepository: UserAccountRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun validateAddress(destinationAddress: PublicKey, mintAddress: String): AddressValidation {
        val userPublicKey = tokenKeyProvider.publicKey.toPublicKey()

        if (destinationAddress.equals(userPublicKey)) {
            return AddressValidation.Error(R.string.main_send_to_yourself_error)
        }

        try {
            findSplTokenAddressData(destinationAddress, mintAddress)
        } catch (e: IllegalStateException) {
            return AddressValidation.WrongWallet
        }

        return AddressValidation.Valid
    }

    suspend fun findSplTokenAddressData(
        destinationAddress: PublicKey,
        mintAddress: String
    ): TransactionAddressData {
        val associatedAddress = try {
            Timber.tag(ADDRESS_TAG).d("Searching for SPL token address")
            findSplTokenAddress(destinationAddress, mintAddress)
        } catch (e: IllegalStateException) {
            Timber.tag(ADDRESS_TAG).d("Searching address failed, address is wrong")
            throw IllegalStateException("Invalid owner address")
        }

        /* If account is not found, create one */
        val accountInfo = userAccountRepository.getAccountInfo(associatedAddress.toBase58())
        val value = accountInfo?.value
        val accountExists = value?.owner == TokenProgram.PROGRAM_ID.toString() && value.data != null
        return TransactionAddressData(
            destinationAddress = associatedAddress,
            shouldCreateAccount = !accountExists
        )
    }

    @Throws(IllegalStateException::class)
    private suspend fun findSplTokenAddress(destinationAddress: PublicKey, mintAddress: String): PublicKey {
        val accountInfo = userAccountRepository.getAccountInfo(destinationAddress.toBase58())

        // detect if it is a direct token address
        val info = TokenTransaction.decodeAccountInfo(accountInfo)
        if (info != null && userLocalRepository.findTokenData(info.mint.toBase58()) != null) {
            Timber.tag(ADDRESS_TAG).d("Token by mint was found. Continuing with direct address")
            return destinationAddress
        }

        // create associated token address
        val value = accountInfo?.value
        if (value == null || value.data?.get(0).isNullOrEmpty()) {
            Timber.tag(ADDRESS_TAG).d("No information found, creating associated token address")
            return TokenTransaction.getAssociatedTokenAddress(mintAddress.toPublicKey(), destinationAddress)
        }

        // detect if destination address is already a SPLToken address
        if (info?.mint == destinationAddress) {
            Timber.tag(ADDRESS_TAG).d("Destination address is already an SPL Token address, returning")
            return destinationAddress
        }

        // detect if destination address is a SOL address
        if (info?.owner?.toBase58() == SystemProgram.PROGRAM_ID.toBase58()) {
            Timber.tag(ADDRESS_TAG).d("Destination address is SOL address. Getting the associated token address")

            // create associated token address
            return TokenTransaction.getAssociatedTokenAddress(mintAddress.toPublicKey(), destinationAddress)
        }

        throw IllegalStateException("Wallet address is not valid")
    }

    suspend fun isSolAddress(address: String): Boolean {
        val accountInfo = userAccountRepository.getAccountInfo(address)
        return accountInfo?.value?.owner == SystemProgram.PROGRAM_ID.toBase58()
    }
}
