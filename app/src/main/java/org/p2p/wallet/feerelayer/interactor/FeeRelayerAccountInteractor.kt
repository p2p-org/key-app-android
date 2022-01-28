package org.p2p.wallet.feerelayer.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.feerelayer.model.RelayInfo
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.interactor.TransactionAmountInteractor
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerAccountInteractor(
    private val rpcRepository: RpcRepository,
    private val amountInteractor: TransactionAmountInteractor,
    private val feeRelayerRepository: FeeRelayerRepository,
    private val userInteractor: UserInteractor,
    private val addressInteractor: TransactionAddressInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    companion object {
        const val RELAY_PROGRAM_ID = "12YKFL4mnZz6CBEGePrf293mEzueQM3h8VLPUJsKpGs9"
        val RELAY_ACCOUNT_RENT_EXEMPTION = BigInteger.valueOf(890880L)
    }

    private var relayAccount: RelayAccount? = null

    private var relayInfo: RelayInfo? = null

    suspend fun getRelayInfo(): RelayInfo = withContext(Dispatchers.IO) {
        if (relayInfo == null) {
            // get minimum token account balance
            val minimumTokenAccountBalance = async { amountInteractor.getAccountMinForRentExemption() }
            // get minimum relay account balance
            val minimumRelayAccountBalance = async { amountInteractor.getAccountMinForRentExemption(0) }
            // get fee payer address
            val feePayerAddress = async { feeRelayerRepository.getFeePayerPublicKey() }
            // get lamportsPerSignature
            val lamportsPerSignature = async { amountInteractor.getLamportsPerSignature() }

            relayInfo = RelayInfo(
                minimumTokenAccountBalance = minimumTokenAccountBalance.await(),
                minimumRelayAccountBalance = minimumRelayAccountBalance.await(),
                feePayerAddress = feePayerAddress.await(),
                lamportsPerSignature = lamportsPerSignature.await()
            )
        }

        return@withContext relayInfo!!
    }

    suspend fun getUserRelayAccount(reuseCache: Boolean = true): RelayAccount {
        if (relayAccount == null || !reuseCache) {
            val userPublicKey = tokenKeyProvider.publicKey.toPublicKey()
            val userRelayAddress = getUserRelayAddress(userPublicKey)
            val account = rpcRepository.getAccountInfo(userRelayAddress.toBase58())
            val value = account?.value
            relayAccount = RelayAccount(
                publicKey = userRelayAddress,
                isCreated = value != null,
                balance = value?.lamports?.toBigInteger()
            )
        }

        return relayAccount!!
    }

    suspend fun getFeeTokenAccounts(
        fromPublicKey: String
    ): List<Token.Active> {
        val userTokens = userInteractor.getUserTokens()
        val feeTokenAccounts = mutableListOf<Token.Active>()

        val sol = userTokens.firstOrNull { it.isSOL }
        if (sol != null) {
            feeTokenAccounts += sol
        }

        val fromTokenAccount = userTokens.firstOrNull { it.publicKey == fromPublicKey }
        if (fromTokenAccount != null && !fromTokenAccount.isSOL) {
            feeTokenAccounts += fromTokenAccount
        }

        return feeTokenAccounts
    }

    suspend fun getAccountCreationFee(address: String?, tokenMint: String?): BigInteger {
        val isRelayAccountNeeded: Boolean = true // todo: get relay account
        var accountCreationFee = BigInteger.ZERO

        if (isRelayAccountNeeded) {
            accountCreationFee += RELAY_ACCOUNT_RENT_EXEMPTION
        }

        if (address.isNullOrEmpty() || tokenMint.isNullOrEmpty()) {
            return accountCreationFee
        }

        val associatedAccount = addressInteractor.findAssociatedAddress(address.toPublicKey(), tokenMint)

        if (associatedAccount.shouldCreateAssociatedInstruction) {
            accountCreationFee += amountInteractor.getAccountMinForRentExemption()
            accountCreationFee += amountInteractor.getLamportsPerSignature()
        }

        return accountCreationFee
    }

    fun getUserRelayAddress(owner: PublicKey): PublicKey =
        findAddress(owner, "relay")

    fun getUserTemporaryWsolAccount(owner: PublicKey): PublicKey =
        findAddress(owner, "temporary_wsol")

    private fun findAddress(owner: PublicKey, key: String): PublicKey =
        PublicKey
            .findProgramAddress(
                seeds = listOf(owner.toByteArray(), key.toByteArray()),
                programId = RELAY_PROGRAM_ID.toPublicKey()
            )
            .address

    fun getTransitTokenAccountAddress(owner: PublicKey, mint: PublicKey): PublicKey =
        PublicKey
            .findProgramAddress(
                seeds = listOf(owner.toByteArray(), mint.toByteArray(), "transit".toByteArray()),
                programId = RELAY_PROGRAM_ID.toPublicKey()
            )
            .address
}