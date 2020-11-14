package com.wowlet.domain.interactors

import com.wowlet.entities.local.UserSecretData


interface RegLoginInteractor {
    suspend fun initUser():UserSecretData
}