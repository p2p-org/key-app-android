package com.p2p.wowlet.domain.interactors

import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.local.SecretKeyItem

interface ManualSecretKeyInteractor {
    suspend fun sortingSecretKey(secretKeyItem: SecretKeyItem): Result<Boolean>
}