package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.NotificationInteractor
import com.wowlet.entities.local.EnableFingerPrintModel
import com.wowlet.entities.local.EnableNotificationModel
import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem

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