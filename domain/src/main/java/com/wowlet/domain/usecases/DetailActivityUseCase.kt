package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.extentions.transferInfoToActivityItem
import com.wowlet.domain.interactors.DetailActivityInteractor
import com.wowlet.domain.utils.secondToDate
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants
import com.wowlet.entities.Result
import com.wowlet.entities.local.ActivityItem
import java.lang.Exception


class DetailActivityUseCase(
    private val wowletApiCallRepository: WowletApiCallRepository,
    private val preferenceService: PreferenceService
) : DetailActivityInteractor {

    override suspend fun getActivityList(
        publicKey: String,
        icon: String,
        tokenName: String
    ): List<ActivityItem> {
        val walletsList = wowletApiCallRepository.getDetailActivityData(publicKey).map {
            it.transferInfoToActivityItem(publicKey, icon, tokenName)
        }
        return walletsList
    }

    override suspend fun blockTime(slot: Long): Result<String> {
        return try {
            val time = wowletApiCallRepository.getBlockTime(slot)
            val stringDate=time.secondToDate()
            Result.Success(stringDate)
        } catch (e: Exception) {
            Result.Error(CallException(Constants.REQUEST_EXACTION,e.message))
        }
    }
}
