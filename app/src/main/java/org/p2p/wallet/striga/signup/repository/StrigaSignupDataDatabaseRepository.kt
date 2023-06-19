package org.p2p.wallet.striga.signup.repository

import android.content.res.Resources
import timber.log.Timber
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.signup.dao.StrigaSignupDataDao
import org.p2p.wallet.striga.signup.dao.StrigaSignupDataEntity
import org.p2p.wallet.striga.signup.repository.mapper.StrigaSignupDataMapper
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
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
        StrigaDataLayerError.from(
            error = error,
            default = StrigaDataLayerError.InternalError(error)
        )
    }

    override suspend fun getUserSignupDataAsMap(): StrigaDataLayerResult<Map<StrigaSignupDataType, StrigaSignupData>> =
        try {
            dao.getSignupDataForUser(currentUserPublicKey.base58Value)
                .map(mapper::fromEntity)
                .associateBy(StrigaSignupData::type)
                .toSuccessResult()
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }

    override suspend fun getUserSignupDataByType(
        type: StrigaSignupDataType
    ): StrigaDataLayerResult<StrigaSignupData> = try {
        dao.getSignupDataForUser(currentUserPublicKey.base58Value)
            .map(mapper::fromEntity)
            .first { it.type == type }
            .toSuccessResult()
    } catch (error: Throwable) {
        StrigaDataLayerError.from(
            error = error,
            default = StrigaDataLayerError.InternalError(error)
        )
    }

    override suspend fun createUserSignupData(): StrigaDataLayerResult<Unit> = try {
        val isUserHasNoSavedData: Boolean = dao.countSignupDataForUser(tokenKeyProvider.publicKey) == 0
        if (isUserHasNoSavedData) {
            prefillDataForUser()
        }
        success()
    } catch (error: Throwable) {
        StrigaDataLayerError.from(
            error = error,
            default = StrigaDataLayerError.InternalError(error)
        )
    }

    private suspend fun prefillDataForUser() {
        StrigaSignupDataType.values().map {
            StrigaSignupDataEntity(
                type = resources.getString(it.tag),
                value = null,
                ownerPublicKey = currentUserPublicKey
            )
        }
            .also { dao.updateOrInsertData(it) }
    }

    override suspend fun updateSignupData(newData: StrigaSignupData): StrigaDataLayerResult<Unit> = try {
        Timber.tag(TAG).d("updateSignupData: ${newData.type} ${newData.value}")
        val entity = mapper.toEntity(newData, currentUserPublicKey)
        dao.updateOrInsertData(entity)
        success()
    } catch (error: Throwable) {
        StrigaDataLayerError.from(
            error = error,
            default = StrigaDataLayerError.InternalError(error)
        )
    }

    override suspend fun updateSignupData(newData: Collection<StrigaSignupData>): StrigaDataLayerResult<Unit> = try {
        Timber.tag(TAG).d("updateSignupDataList: $newData")
        val entities = newData.map { mapper.toEntity(it, currentUserPublicKey) }
        dao.updateOrInsertData(entities)
        success()
    } catch (error: Throwable) {
        StrigaDataLayerError.from(
            error = error,
            default = StrigaDataLayerError.InternalError(error)
        )
    }

    private fun success(): StrigaDataLayerResult.Success<Unit> =
        StrigaDataLayerResult.Success(Unit)
}
