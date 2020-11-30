package com.wowlet.domain.interactors

interface CreateWalletInteractor {
    suspend fun initUser()
    fun generatePhrase(): String
}