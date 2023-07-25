package org.p2p.wallet.striga.wallet.repository.impl

import timber.log.Timber
import kotlin.reflect.KMutableProperty0
import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.common.model.toSuccessResult
import org.p2p.wallet.striga.user.repository.StrigaUserRepository
import org.p2p.wallet.striga.wallet.models.StrigaCryptoAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository

internal class StrigaWalletCachedRepository(
    private val remoteRepository: StrigaWalletRemoteRepository,
    private val cache: StrigaWalletInMemoryRepository,
    private val strigaUserRepository: StrigaUserRepository,
) : StrigaWalletRepository {

    private val timber: Timber.Tree = Timber.tag("StrigaWalletCachedRepository")

    override suspend fun getUserWallet(): StrigaDataLayerResult<StrigaUserWallet> {
        return withCache(cache::userWallet) {
            remoteRepository.getUserWallet()
        }
    }

    override suspend fun getFiatAccountDetails(
        accountId: StrigaAccountId
    ): StrigaDataLayerResult<StrigaFiatAccountDetails> {
        return withCache(cache::fiatAccountDetails) {
            remoteRepository.getFiatAccountDetails(accountId)
        }
    }

    override suspend fun getCryptoAccountDetails(
        accountId: StrigaAccountId
    ): StrigaDataLayerResult<StrigaCryptoAccountDetails> {
        return withCache(cache::cryptoAccountDetails) {
            remoteRepository.getCryptoAccountDetails(accountId)
        }
    }

    override suspend fun getUserBankingDetails(
        accountId: StrigaAccountId,
    ): StrigaDataLayerResult<StrigaUserBankingDetails> {
        return withCache(cache::userEurBankingDetails) {
            val strigaUserFullName = strigaUserRepository.getUserDetails().unwrap()
                .userInfo
                .fullName
            remoteRepository.getUserBankingDetails(accountId, strigaUserFullName)
        }
    }

    override suspend fun saveUserEurBankingDetails(userBic: String, userIban: String) {
        val strigaUserFullName = strigaUserRepository.getUserDetails().unwrap()
            .userInfo
            .fullName
        cache.userEurBankingDetails = StrigaUserBankingDetails(
            bankingBic = userBic,
            bankingIban = userIban,
            bankingFullName = strigaUserFullName
        )
    }

    private suspend fun <T> withCache(
        cache: KMutableProperty0<T?>? = null,
        remote: suspend () -> T,
    ): StrigaDataLayerResult<T> {
        return try {
            val result = cache?.invoke()?.also {
                timber.i("Getting data from cache")
            } ?: remote().also {
                timber.i("Update cache from remote")
                cache?.set(it)
            }
            result.toSuccessResult()
        } catch (e: Throwable) {
            StrigaDataLayerError.from(
                error = e,
                default = StrigaDataLayerError.InternalError(e)
            )
        }
    }
}
