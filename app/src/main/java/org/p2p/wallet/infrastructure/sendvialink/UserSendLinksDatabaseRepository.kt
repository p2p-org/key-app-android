package org.p2p.wallet.infrastructure.sendvialink

import timber.log.Timber
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.sendvialink.db.UserSendLinkEntity
import org.p2p.wallet.infrastructure.sendvialink.db.UserSendLinksDao
import org.p2p.wallet.infrastructure.sendvialink.model.UserSendLink
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance

class UserSendLinksDatabaseRepository(
    private val userSendLinksDao: UserSendLinksDao,
    private val userRepository: UserLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider
) : UserSendLinksLocalRepository {

    private val userAddress: Base58String
        get() = tokenKeyProvider.publicKey.toBase58Instance()

    override suspend fun getUserLinks(): List<UserSendLink> {
        return userSendLinksDao.getLinks(userAddress.base58Value)
            .mapNotNull { it.toDomain() }
    }

    override suspend fun getUserLinkById(uuid: String): UserSendLink? {
        return userSendLinksDao.getLinkByOrdinal(userAddress.base58Value, uuid)?.toDomain()
    }

    override suspend fun saveUserLink(link: UserSendLink) {
        val entity = link.toEntity(userAddress)
        userSendLinksDao.addLink(entity)
    }

    private fun UserSendLink.toEntity(userAddress: Base58String): UserSendLinkEntity =
        UserSendLinkEntity(
            uuid = uuid,
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
            uuid = uuid,
            link = link,
            token = tokenDetails,
            amount = amount,
            dateCreated = dateCreated
        )
    }

    override suspend fun getUserLinksCount(): Int = getUserLinks().size
}
