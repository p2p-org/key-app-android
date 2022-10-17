package org.p2p.wallet.auth.username.repository

import org.p2p.wallet.auth.username.api.RegisterUsernameServiceApi
import org.p2p.wallet.auth.username.api.response.CreateNameResponse
import org.p2p.wallet.auth.username.repository.mapper.RegisterUsernameServiceApiMapper
import org.p2p.wallet.auth.username.repository.model.UsernameDetails
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.utils.Base58String
import timber.log.Timber
import kotlinx.coroutines.withContext

class UsernameRemoteRepository(
    private val usernameService: RegisterUsernameServiceApi,
    private val mapper: RegisterUsernameServiceApiMapper,
    private val dispatchers: CoroutineDispatchers
) : UsernameRepository {

    override suspend fun isUsernameTaken(username: String): Boolean = withContext(dispatchers.io) {
        val request = mapper.toGetUsernameNetwork(username)
        val isUsernameTaken: Boolean = try {
            mapper.fromNetwork(usernameService.getUsername(request))
            false
        } catch (error: Throwable) {
            Timber.i(error)
            true
        }

        isUsernameTaken
    }

    override suspend fun createUsername(
        username: String,
        owner: Base58String,
        ownerPrivateKey: Base58String
    ): CreateNameResponse = withContext(dispatchers.io) {
        val request = mapper.toCreateUsernameNetwork(
            username = username,
            userPublicKey = owner,
            userPrivateKey = ownerPrivateKey.decodeToBytes()
        )
        val response = usernameService.createUsername(request)
        mapper.fromNetwork(response)
    }

    override suspend fun findUsernameDetailsByUsername(
        username: String
    ): List<UsernameDetails> = withContext(dispatchers.io) {
        val request = mapper.toResolveUsernameNetwork(username)
        val response = usernameService.resolveUsername(request)
        mapper.fromNetwork(response)
            .map { UsernameDetails(ownerAddress = it.domainOwnerAddress, fullUsername = it.fullDomainUsername) }
    }

    override suspend fun findUsernameDetailsByAddress(
        ownerAddress: Base58String
    ): List<UsernameDetails> = withContext(dispatchers.io) {
        val request = mapper.toLookupUsernameNetwork(ownerAddress)
        val response = usernameService.lookupUsername(request)
        mapper.fromNetwork(response)
            .map { UsernameDetails(ownerAddress = ownerAddress, fullUsername = it.fullDomainUsername) }
    }
}
