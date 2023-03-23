package org.p2p.wallet.infrastructure.sendvialink

import org.p2p.wallet.infrastructure.sendvialink.model.UserSendLink
import org.p2p.wallet.utils.Base58String

interface UserSendLinksLocalRepository {
    suspend fun getUserLinks(userAddress: Base58String): List<UserSendLink>
    suspend fun saveUserLink(currentUserAddress: Base58String, link: UserSendLink)
}
