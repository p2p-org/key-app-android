package com.p2p.wallet.auth.interactor

import com.p2p.wallet.auth.api.LookupUsernameResponse
import com.p2p.wallet.auth.repository.UsernameRemoteRepository

class CheckUsernameInteractor(
    private val usernameRemoteRepository: UsernameRemoteRepository,
) {

    suspend fun lookupUsername(owner: String): ArrayList<LookupUsernameResponse> {
        return usernameRemoteRepository.lookup(owner)
    }
}