package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.NotificationInteractor
import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem

class NotificationUseCase(private val preferenceService: PreferenceService) :
    NotificationInteractor {
    override suspend fun enableNotification(enableNotofication: Boolean) {
        preferenceService.enableNotification(enableNotofication)
    }

    override suspend fun isEnableNotification(): Boolean = preferenceService.isAllowNotification()

}