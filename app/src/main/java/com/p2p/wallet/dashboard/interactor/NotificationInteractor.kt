package com.p2p.wallet.dashboard.interactor

import com.p2p.wallet.infrastructure.persistence.PreferenceService
import com.p2p.wallet.dashboard.model.local.EnableNotificationModel

class NotificationInteractor(private val preferenceService: PreferenceService) {
    suspend fun enableNotification(enableNotofication: EnableNotificationModel) {
        preferenceService.enableNotification(enableNotofication)
    }

    fun isEnableNotification(): EnableNotificationModel {
        val data = preferenceService.isAllowNotification()
        return data ?: EnableNotificationModel(false, false)
    }
}