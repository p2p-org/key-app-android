package com.p2p.wowlet.domain.interactors

interface CreateWalletInteractor {
    suspend fun initUser()
    fun generatePhrase(): List<String>
}