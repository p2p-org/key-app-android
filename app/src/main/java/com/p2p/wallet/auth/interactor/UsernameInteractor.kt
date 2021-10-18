package com.p2p.wallet.auth.interactor

import com.p2p.wallet.auth.api.LookupUsernameResponse
import com.p2p.wallet.auth.repository.UsernameRemoteRepository
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.infrastructure.username.UsernameStorageContract

private const val KEY_USERNAME = "KEY_USERNAME"

class UsernameInteractor(
    private val usernameRemoteRepository: UsernameRemoteRepository,
    private val usernameStorage: UsernameStorageContract,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun checkInStorageUsername(): String {
        if (usernameStorage.contains(KEY_USERNAME))
            usernameStorage.getString(KEY_USERNAME)
        else {
        }

        return ""
    }

    suspend fun lookupUsername(owner: String): ArrayList<LookupUsernameResponse> {
        return usernameRemoteRepository.lookup(owner)
    }
}