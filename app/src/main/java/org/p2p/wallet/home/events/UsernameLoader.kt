package org.p2p.wallet.home.events

import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.utils.ellipsizeAddress

class UsernameLoader(
    private val homeInteractor: HomeInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) : AppLoader {

    override suspend fun onLoad() {
        val username = homeInteractor.getUsername()
        val userAddress = username?.fullUsername ?: tokenKeyProvider.publicKey.ellipsizeAddress()
        homeInteractor.updateUsername(userAddress)
    }
}
