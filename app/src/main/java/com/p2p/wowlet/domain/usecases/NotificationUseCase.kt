package com.p2p.wowlet.domain.usecases

import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.domain.interactors.NotificationInteractor
import com.p2p.wowlet.entities.local.EnableNotificationModel

class NotificationUseCase(private val preferenceService: PreferenceService) :
    NotificationInteractor {
    override suspend fun enableNotification(enableNotofication: EnableNotificationModel) {
        preferenceService.enableNotification(enableNotofication)
    }

    override  fun isEnableNotification(): EnableNotificationModel {
        val data = preferenceService.isAllowNotification()
        return data ?: EnableNotificationModel(false, false)
    }

}