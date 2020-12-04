package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.extentions.transferInfoToActivityItem
import com.wowlet.domain.interactors.DetailActivityInteractor
import com.wowlet.entities.local.ActivityItem


class DetailActivityUseCase(
    private val wowletApiCallRepository: WowletApiCallRepository,
    private val preferenceService: PreferenceService
) : DetailActivityInteractor {

    override suspend fun getActivityList(publicKey: String, icon: String, tokenName: String): List<ActivityItem> {
        val walletsList = wowletApiCallRepository.getDetailActivityData(publicKey).map {
            it.transferInfoToActivityItem(publicKey,icon,tokenName)
        }
        return walletsList
    }

}
