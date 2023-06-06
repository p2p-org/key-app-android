package org.p2p.wallet.rpc.interactor

import timber.log.Timber
import org.p2p.core.token.TokenData
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.swap.model.orca.TransactionAddressData
import org.p2p.wallet.user.repository.UserAccountRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toPublicKey

private const val ADDRESS_TAG = "Address"

class TransactionAddressInteractor(
    private val userAccountRepository: UserAccountRepository,
    private val userLocalRepository: UserLocalRepository
) {

    suspend fun findSplTokenAddressData(
        destinationAddress: PublicKey,
        mintAddress: String,
        useCache: Boolean = true
    ): TransactionAddressData {
        val associatedAddress = try {
            Timber.tag(ADDRESS_TAG).i("Searching for SPL token address")
            findSplTokenAddress(destinationAddress, mintAddress, useCache)
        } catch (e: IllegalStateException) {
            Timber.tag(ADDRESS_TAG).i("Searching address failed, address is wrong")
            throw IllegalStateException("Invalid owner address")
        }

        /* If account is not found, create one */
        val accountInfo = userAccountRepository.getAccountInfo(associatedAddress.toBase58(), useCache)
        val value = accountInfo?.value
        val accountExists = value?.owner == TokenProgram.PROGRAM_ID.toString() && value.data != null
        return TransactionAddressData(
            destinationAddress = associatedAddress,
            shouldCreateAccount = !accountExists
        )
    }

    @Throws(IllegalStateException::class)
    private suspend fun findSplTokenAddress(
        destinationAddress: PublicKey,
        mintAddress: String,
        useCache: Boolean
    ): PublicKey {
        val accountInfo = userAccountRepository.getAccountInfo(destinationAddress.toBase58(), useCache)

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

    suspend fun getDirectTokenData(address: String, useCache: Boolean = true): TokenData? {
        val accountInfo = userAccountRepository.getAccountInfo(address, useCache)

        // detect if it is a direct token address
        val info = TokenTransaction.decodeAccountInfo(accountInfo) ?: return null
        return userLocalRepository.findTokenData(info.mint.toBase58())
    }

    suspend fun isSolAddress(address: String): Boolean {
        val accountInfo = userAccountRepository.getAccountInfo(address)
        return accountInfo?.value?.owner == SystemProgram.PROGRAM_ID.toBase58()
    }
}
