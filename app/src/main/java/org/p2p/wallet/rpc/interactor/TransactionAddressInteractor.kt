package org.p2p.wallet.rpc.interactor

import timber.log.Timber
import kotlinx.coroutines.CancellationException
import org.p2p.core.token.TokenMetadata
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
        programId: PublicKey = TokenProgram.PROGRAM_ID,
    ): TransactionAddressData {
        val associatedAddress = try {
            Timber.tag(ADDRESS_TAG).i("Searching SPL token address for ${destinationAddress.toBase58()}")
            findSplTokenAddress(
                destinationAddress = destinationAddress,
                mintAddress = mintAddress,
                programId = programId,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: IllegalStateException) {
            Timber.tag(ADDRESS_TAG).i("Searching address failed, address is wrong")
            error("Invalid owner address from findSplTokenAddress")
        }
        Timber.i("ATA address for ${destinationAddress.toBase58()} mint $mintAddress = $associatedAddress")

        /* If account is not found, create one */
        val accountInfo = userAccountRepository.getAccountInfo(
            account = associatedAddress.toBase58(),
            useCache = false
        )
        val value = accountInfo?.value
        val targetProgramId = programId.toBase58()
        val accountExists = value?.owner == targetProgramId && value.data != null
        return TransactionAddressData(
            destinationAddress = associatedAddress,
            shouldCreateAccount = !accountExists
        )
    }

    @Throws(IllegalStateException::class)
    private suspend fun findSplTokenAddress(
        destinationAddress: PublicKey,
        mintAddress: String,
        programId: PublicKey = TokenProgram.PROGRAM_ID,
    ): PublicKey {
        val accountInfo = userAccountRepository.getAccountInfo(
            account = destinationAddress.toBase58(),
            useCache = false
        )

        // detect if it is a direct token address
        val info = TokenTransaction.decodeAccountInfo(accountInfo)
        if (info != null && userLocalRepository.findTokenData(info.mint.toBase58()) != null) {
            Timber.tag(ADDRESS_TAG).i("Token by mint was found. Continuing with direct address")
            return destinationAddress
        }

        // create associated token address
        val value = accountInfo?.value
        if (value == null || value.data?.get(0).isNullOrEmpty()) {
            Timber.tag(ADDRESS_TAG).i("No information found, generating associated token address")
            return TokenTransaction.getAssociatedTokenAddress(
                mint = mintAddress.toPublicKey(),
                owner = destinationAddress,
                programId = programId
            )
        }

        // detect if destination address is already a SPL Token address
        if (info?.mint == destinationAddress) {
            Timber.tag(ADDRESS_TAG).i("Destination address is already an SPL Token address, returning")
            return destinationAddress
        }

        // detect if destination address is a SOL address
        if (info?.owner?.toBase58() == SystemProgram.PROGRAM_ID.toBase58()) {
            Timber.tag(ADDRESS_TAG).i("Destination address is SOL address. Getting the associated token address")

            // create associated token address
            return TokenTransaction.getAssociatedTokenAddress(
                mintAddress.toPublicKey(),
                destinationAddress,
                programId
            )
        }

        throw IllegalStateException("Wallet address is not valid")
    }

    suspend fun getDirectTokenData(address: String): TokenMetadata? {
        val accountInfo = userAccountRepository.getAccountInfo(address, useCache = false)

        // detect if it is a direct token address
        val info = TokenTransaction.decodeAccountInfo(accountInfo) ?: return null
        return userLocalRepository.findTokenData(info.mint.toBase58())
    }
}
