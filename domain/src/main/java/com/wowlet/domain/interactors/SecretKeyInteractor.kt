package com.wowlet.domain.interactors

import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem

interface SecretKeyInteractor {
    fun checkCurrentSelected(id: Int): SecretKeyCombinationSuccess
    suspend fun resetPhrase(inputPhrase: String): Boolean
    fun currentPhrase(): String
}