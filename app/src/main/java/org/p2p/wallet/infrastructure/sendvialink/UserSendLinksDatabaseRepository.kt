package org.p2p.wallet.infrastructure.sendvialink

import timber.log.Timber
import org.p2p.wallet.infrastructure.sendvialink.db.UserSendLinkEntity
import org.p2p.wallet.infrastructure.sendvialink.db.UserSendLinksDao
import org.p2p.wallet.infrastructure.sendvialink.model.UserSendLink
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

class UserSendLinksDatabaseRepository(
    private val userSendLinksDao: UserSendLinksDao,
    private val userRepository: UserLocalRepository
) : UserSendLinksLocalRepository {

    override suspend fun getUserLinks(userAddress: Base58String): List<UserSendLink> =
        userSendLinksDao.getLinks(userAddress.base58Value)
            .mapNotNull { it.toDomain() }

    override suspend fun saveUserLink(currentUserAddress: Base58String, link: UserSendLink) {
        val entity = link.toEntity(currentUserAddress)
        userSendLinksDao.addLink(entity)
    }

    private fun UserSendLink.toEntity(userAddress: Base58String): UserSendLinkEntity =
        UserSendLinkEntity(
            link = link,
            amount = amount,
            dateCreated = dateCreated,
            tokenMint = token.mintAddress.toBase58Instance(),
            linkOwnerAddress = userAddress
        )

    private fun UserSendLinkEntity.toDomain(): UserSendLink? {
        val tokenDetails = userRepository.findTokenByMint(tokenMint.base58Value)
        if (tokenDetails == null) {
            Timber.i("Can't find any token data for saved send-via-link token: ${tokenMint.base58Value}")
            return null
        }

        return UserSendLink(
            link = link,
            token = tokenDetails,
            amount = amount,
            dateCreated = dateCreated
        )
    }
}
