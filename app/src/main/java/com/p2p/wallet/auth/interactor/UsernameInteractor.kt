package com.p2p.wallet.auth.interactor

import com.p2p.wallet.auth.repository.UsernameRemoteRepository
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.infrastructure.username.UsernameStorageContract

private const val KEY_USERNAME = "KEY_USERNAME"

class UsernameInteractor(
    private val usernameRemoteRepository: UsernameRemoteRepository,
    private val usernameStorage: UsernameStorageContract,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun checkInStorageUsername(): String? {
        return if (usernameStorage.contains(KEY_USERNAME))
            usernameStorage.getString(KEY_USERNAME)
        else
            lookupUsername(tokenKeyProvider.publicKey)
    }

    private suspend fun lookupUsername(owner: String): String? {
        val userName = usernameRemoteRepository.lookup(owner)

        var name = ""
        return if (userName.isNotEmpty()) {
            for (index in userName.indices) {
                name = userName[0].name
            }

            name
        } else {
            null
        }
    }
}