package org.p2p.wallet.infrastructure.sendvialink

import org.p2p.wallet.infrastructure.sendvialink.model.UserSendLink

interface UserSendLinksLocalRepository {
    suspend fun getUserLinks(): List<UserSendLink>
    suspend fun saveUserLink(link: UserSendLink)
    suspend fun getUserLinkById(uuid: String): UserSendLink?
}
