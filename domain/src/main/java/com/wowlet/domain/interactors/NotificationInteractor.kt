package com.wowlet.domain.interactors

import com.wowlet.entities.local.EnableNotificationModel
import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem


interface NotificationInteractor {
    suspend fun enableNotification(enableNotofication:EnableNotificationModel)
     fun isEnableNotification(): EnableNotificationModel
}