package org.p2p.wallet.rpc.interactor

import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.model.orca.TransactionAddressData
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.TokenProgram
import timber.log.Timber

private const val ADDRESS_TAG = "Address"

class TransactionAddressInteractor(
    private val rpcRepository: RpcRepository,
    private val userLocalRepository: UserLocalRepository
) {

    suspend fun findAssociatedAddress(
        ownerAddress: PublicKey,
        destinationMint: String
    ): TransactionAddressData {
        val associatedAddress = try {
            Timber.tag(ADDRESS_TAG).d("Searching for SPL token address")
            findSplTokenAddress(destinationMint, ownerAddress)
        } catch (e: IllegalStateException) {
            Timber.tag(ADDRESS_TAG).d("Searching address failed, address is wrong")
            throw IllegalStateException("Invalid owner address")
        }

        /* If account is not found, create one */
        val accountInfo = rpcRepository.getAccountInfo(associatedAddress.toBase58())
        val value = accountInfo?.value
        val associatedNotNeeded = value?.owner == TokenProgram.PROGRAM_ID.toString() && value.data != null
        return TransactionAddressData(
            associatedAddress = associatedAddress,
            shouldCreateAssociatedInstruction = !associatedNotNeeded
        )
    }

    @Throws(IllegalStateException::class)
    suspend fun findSplTokenAddress(mintAddress: String, destinationAddress: PublicKey): PublicKey {
        val accountInfo = rpcRepository.getAccountInfo(destinationAddress.toBase58())

        // detect if it is a direct token address
        val info = TokenTransaction.parseAccountInfoData(accountInfo, TokenProgram.PROGRAM_ID)
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
        if (info?.owner?.toBase58() == TokenProgram.PROGRAM_ID.toBase58()) {
            Timber.tag(ADDRESS_TAG).d("Destination address is SOL address. Getting the associated token address")

            // create associated token address
            return TokenTransaction.getAssociatedTokenAddress(mintAddress.toPublicKey(), destinationAddress)
        }

        throw IllegalStateException("Wallet address is not valid")
    }
}