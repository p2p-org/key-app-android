package com.p2p.wowlet.dashboard.interactor

import com.p2p.wowlet.infrastructure.persistence.PreferenceService
import com.p2p.wowlet.dashboard.model.local.EnableNotificationModel

class NotificationInteractor(private val preferenceService: PreferenceService) {
    suspend fun enableNotification(enableNotofication: EnableNotificationModel) {
        preferenceService.enableNotification(enableNotofication)
    }

    fun isEnableNotification(): EnableNotificationModel {
        val data = preferenceService.isAllowNotification()
        return data ?: EnableNotificationModel(false, false)
    }
}