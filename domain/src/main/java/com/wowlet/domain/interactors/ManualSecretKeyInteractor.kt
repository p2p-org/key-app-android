package com.wowlet.domain.interactors

import com.wowlet.entities.Result
import com.wowlet.entities.local.SecretKeyItem

interface ManualSecretKeyInteractor {
    suspend fun sortingSecretKey(secretKeyItem: SecretKeyItem): Result<Boolean>
}