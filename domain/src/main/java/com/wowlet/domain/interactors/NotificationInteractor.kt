package com.wowlet.domain.interactors

import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem


interface NotificationInteractor {
    suspend fun enableNotification(enableNotofication:Boolean)
    suspend fun isEnableNotification():Boolean
}