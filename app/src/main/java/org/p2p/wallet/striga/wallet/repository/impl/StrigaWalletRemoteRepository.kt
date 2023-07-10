package org.p2p.wallet.striga.wallet.repository.impl

import timber.log.Timber
import java.util.Calendar
import org.p2p.core.utils.MillisSinceEpoch
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.api.request.StrigaEnrichAccountRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaUserWalletsRequest
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWalletMapper

private const val TAG = "StrigaWalletRepository"

class StrigaWalletRemoteRepository(
    private val api: StrigaWalletApi,
    private val mapper: StrigaWalletMapper,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val cache: StrigaWalletInMemoryRepository
) : StrigaWalletRepository {
    private val timber: Timber.Tree = Timber.tag(TAG)

    private val usersFilterStartDate: MillisSinceEpoch = Calendar.getInstance().run {
        set(2023, 5 /*month is zero based*/, 15)
        timeInMillis
    }

    override suspend fun getFiatAccountDetails(
        accountId: StrigaAccountId
    ): StrigaDataLayerResult<StrigaFiatAccountDetails> {
        return try {
            timber.i("getFiatAccountDetails started")

            if (cache.fiatAccountDetails != null) {
                return cache.fiatAccountDetails!!.toSuccessResult()
            }

            val request = StrigaEnrichAccountRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                accountId = accountId.value
            )
            val response = api.enrichFiatAccount(request)
            mapper.fromNetwork(response)
                .also { cache.fiatAccountDetails = it }
                .toSuccessResult()
        } catch (error: Throwable) {
            timber.i(error, "getFiatAccountDetails failed")
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }

    override suspend fun getUserWallet(): StrigaDataLayerResult<StrigaUserWallet> {
        return try {
            if (cache.userWallet != null) {
                return cache.userWallet!!.toSuccessResult()
            }

            timber.i("getUserWallet started; startDate=$usersFilterStartDate endDate=${System.currentTimeMillis()}")
            val request = StrigaUserWalletsRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                startDate = usersFilterStartDate,
                endDate = System.currentTimeMillis(),
                page = 1
            )
            val response = api.getUserWallets(request)

            mapper.fromNetwork(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                response = response
            )
                .also { cache.userWallet = it }
                .toSuccessResult()
        } catch (error: Throwable) {
            timber.i(error, "getUserWallet failed")
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }
}
