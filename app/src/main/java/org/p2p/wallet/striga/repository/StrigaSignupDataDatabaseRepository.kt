package org.p2p.wallet.striga.repository

import android.content.res.Resources
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaSignupData
import org.p2p.wallet.striga.model.StrigaSignupDataType
import org.p2p.wallet.striga.repository.dao.StrigaSignupDataDao
import org.p2p.wallet.striga.repository.dao.StrigaSignupDataEntity
import org.p2p.wallet.striga.repository.mapper.StrigaSignupDataMapper
import org.p2p.wallet.utils.Base58String

private const val TAG = "StrigaSignupDataDatabaseRepository"

class StrigaSignupDataDatabaseRepository(
    private val dao: StrigaSignupDataDao,
    private val tokenKeyProvider: TokenKeyProvider,
    private val resources: Resources,
    private val mapper: StrigaSignupDataMapper
) : StrigaSignupDataLocalRepository {

    private val currentUserPublicKey: Base58String
        get() = tokenKeyProvider.publicKeyBase58

    override suspend fun getUserSignupData(): StrigaDataLayerResult<List<StrigaSignupData>> = try {
        dao.getSignupDataForUser(currentUserPublicKey.base58Value)
            .map(mapper::fromEntity)
            .toSuccessResult()
    } catch (error: Throwable) {
        StrigaDataLayerError.DatabaseError(error).toFailureResult()
    }

    override suspend fun createUserSignupData(): StrigaDataLayerResult<Unit> = try {
        val isUserHasNoSavedData: Boolean = dao.countSignupDataForUser(tokenKeyProvider.publicKey) == 0
        if (isUserHasNoSavedData) {
            prefillDataForUser()
        }
        success()
    } catch (error: Throwable) {
        StrigaDataLayerError.DatabaseError(error).toFailureResult()
    }

    private suspend fun prefillDataForUser() {
        StrigaSignupDataType.cachedValues.map {
            StrigaSignupDataEntity(
                type = resources.getString(it.tag),
                value = null,
                ownerPublicKey = currentUserPublicKey
            )
        }
            .also { dao.updateOrInsertData(it) }
    }

    override suspend fun updateSignupData(newData: StrigaSignupData): StrigaDataLayerResult<Unit> = try {
        val entity = mapper.toEntity(newData, currentUserPublicKey)
        dao.updateOrInsertData(entity)
        success()
    } catch (error: Throwable) {
        handleError(error)
    }

    private fun <T> handleError(error: Throwable): StrigaDataLayerResult.Failure<T> =
        if (error is StrigaDataLayerError) {
            error
        } else {
            StrigaDataLayerError.InternalError(error)
        }.toFailureResult()

    private fun <T, E : StrigaDataLayerError> E.toFailureResult(): StrigaDataLayerResult.Failure<T> =
        StrigaDataLayerResult.Failure(this)

    private fun <T> T.toSuccessResult(): StrigaDataLayerResult.Success<T> =
        StrigaDataLayerResult.Success(this)

    private fun success(): StrigaDataLayerResult.Success<Unit> =
        StrigaDataLayerResult.Success(Unit)
}
