package com.p2p.wowlet.domain.usecases

import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.entities.local.EnableNotificationModel

class NotificationInteractor(private val preferenceService: PreferenceService) {
    suspend fun enableNotification(enableNotofication: EnableNotificationModel) {
        preferenceService.enableNotification(enableNotofication)
    }

    fun isEnableNotification(): EnableNotificationModel {
        val data = preferenceService.isAllowNotification()
        return data ?: EnableNotificationModel(false, false)
    }
}