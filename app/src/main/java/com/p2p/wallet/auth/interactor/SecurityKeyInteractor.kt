package com.p2p.wallet.auth.interactor

import com.p2p.wallet.dashboard.repository.WowletApiCallRepository
import com.p2p.wallet.infrastructure.persistence.PreferenceService

class SecurityKeyInteractor(
    val preferenceService: PreferenceService,
    val wowletApiCallRepository: WowletApiCallRepository
) {
    // var phraseList = listOf<String>()

    var phraseList = mutableListOf<String>()
    suspend fun initUser() {
        val userData = wowletApiCallRepository.initAccount(phraseList)
        preferenceService.setSingleWalletData(userData)
    }

    suspend fun generateKeys(): List<String> =
        wowletApiCallRepository.generatePhrase()
}