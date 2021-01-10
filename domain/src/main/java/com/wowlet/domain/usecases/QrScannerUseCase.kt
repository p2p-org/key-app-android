package com.wowlet.domain.usecases

import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.interactors.QrScannerInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants
import com.wowlet.entities.Result
import org.p2p.solanaj.core.PublicKey
import java.lang.Exception


class QrScannerUseCase(
    private val wowletApiCallRepository: WowletApiCallRepository,
) : QrScannerInteractor {

    override suspend fun getAccountInfo(publicKey: String): Result<Boolean> {
        return try {
            val accountInfo = wowletApiCallRepository.getAccountInfo(PublicKey(publicKey))
            if (accountInfo.value != null) {
                Result.Success(true)
            } else {
                Result.Success(false)
            }
        } catch (e: Exception) {
            Result.Error(CallException(Constants.REQUEST_EXACTION, e.message))
        }
    }
}
