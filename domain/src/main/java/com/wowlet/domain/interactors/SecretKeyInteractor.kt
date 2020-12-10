package com.wowlet.domain.interactors

import com.wowlet.entities.Result
import com.wowlet.entities.local.SecretKeyCombinationSuccess

interface SecretKeyInteractor {
    fun checkCurrentSelected(id: Int): SecretKeyCombinationSuccess
    suspend fun resetPhrase(inputPhrase: String): Result<Boolean>
    fun currentPhrase(): String
}