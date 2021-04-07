package com.p2p.wowlet.domain.interactors

import com.p2p.wowlet.entities.local.EnableNotificationModel

interface NotificationInteractor {
    suspend fun enableNotification(enableNotofication: EnableNotificationModel)
     fun isEnableNotification(): EnableNotificationModel
}