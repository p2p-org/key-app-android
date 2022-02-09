package org.p2p.wallet.feerelayer.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.feerelayer.model.RelayInfo
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAmountInteractor
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toPublicKey

class FeeRelayerAccountInteractor(
    private val rpcRepository: RpcRepository,
    private val amountInteractor: TransactionAmountInteractor,
    private val feeRelayerRepository: FeeRelayerRepository,
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    private var relayAccount: RelayAccount? = null

    private var relayInfo: RelayInfo? = null

    suspend fun getRelayInfo(): RelayInfo = withContext(Dispatchers.IO) {
        if (relayInfo == null) {
            // get minimum token account balance
            val minimumTokenAccountBalance = async { amountInteractor.getMinBalanceForRentExemption() }
            // get minimum relay account balance
            val minimumRelayAccountBalance = async { amountInteractor.getMinBalanceForRentExemption(0) }
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

    suspend fun getUserRelayAccount(reuseCache: Boolean = true): RelayAccount = withContext(Dispatchers.IO) {
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

        return@withContext relayAccount!!
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

    fun getUserRelayAddress(owner: PublicKey): PublicKey =
        findAddress(owner, "relay")

    fun getUserTemporaryWsolAccount(owner: PublicKey): PublicKey =
        findAddress(owner, "temporary_wsol")

    private fun findAddress(owner: PublicKey, key: String): PublicKey =
        PublicKey
            .findProgramAddress(
                seeds = listOf(owner.toByteArray(), key.toByteArray()),
                programId = FeeRelayerProgram.getProgramId(isMainnet = true)
            )
            .address

    fun getTransitTokenAccountAddress(owner: PublicKey, mint: PublicKey): PublicKey =
        PublicKey
            .findProgramAddress(
                seeds = listOf(owner.toByteArray(), mint.toByteArray(), "transit".toByteArray()),
                programId = FeeRelayerProgram.getProgramId(isMainnet = true)
            )
            .address
}