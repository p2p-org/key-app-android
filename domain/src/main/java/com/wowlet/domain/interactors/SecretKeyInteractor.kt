package com.wowlet.domain.interactors

import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem

interface SecretKeyInteractor {
    suspend fun getSecretData(): List<SecretKeyItem>
    fun checkCurrentSelected(id: Int): SecretKeyCombinationSuccess
}