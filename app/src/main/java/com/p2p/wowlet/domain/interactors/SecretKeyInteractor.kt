package com.p2p.wowlet.domain.interactors

import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.local.SecretKeyCombinationSuccess

interface SecretKeyInteractor {
    fun checkCurrentSelected(id: Int): SecretKeyCombinationSuccess
    suspend fun resetPhrase(inputPhrase: String): Result<Boolean>
    fun currentPhrase(): String
    fun currentListPhrase(): List<String>
}