package org.p2p.wallet.auth.username.repository

import timber.log.Timber
import kotlinx.coroutines.withContext
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.auth.username.api.RegisterUsernameServiceApi
import org.p2p.wallet.auth.username.repository.mapper.RegisterUsernameServiceApiMapper
import org.p2p.wallet.auth.username.repository.model.UsernameDetails
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.isDot

class UsernameRemoteRepository(
    private val usernameService: RegisterUsernameServiceApi,
    private val mapper: RegisterUsernameServiceApiMapper,
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle,
    private val dispatchers: CoroutineDispatchers
) : UsernameRepository {

    override suspend fun isUsernameTaken(username: String): Boolean = withContext(dispatchers.io) {
        val request = mapper.toGetUsernameNetwork(username)
        val isUsernameTaken: Boolean = try {
            mapper.fromNetwork(usernameService.getUsername(request))
            true
        } catch (error: Throwable) {
            Timber.i(error)
            false
        }

        isUsernameTaken
    }

    override suspend fun createUsername(
        username: String,
        ownerPublicKey: Base58String,
        ownerPrivateKey: Base58String
    ) {
        withContext(dispatchers.io) {
            val request = mapper.toCreateUsernameNetwork(
                username = username,
                userPublicKey = ownerPublicKey,
                userPrivateKey = ownerPrivateKey.decodeToBytes()
            )
            val response = usernameService.createUsername(request)
            val createNameTransaction = mapper.fromNetwork(response).serializedSignedCreateNameTransaction
            val resultSignature = rpcSolanaRepository.sendSerializedTransaction(createNameTransaction, Encoding.BASE64)
            Timber.i("Create name transaction is sent: $resultSignature")
        }
    }

    override suspend fun findUsernameDetailsByUsername(
        username: String
    ): List<UsernameDetails> = withContext(dispatchers.io) {
        val request = mapper.toResolveUsernameNetwork(username)
        val response = usernameService.resolveUsername(request)
        mapper.fromNetwork(response).map { nameResponse ->
            val username = Username(
                value = nameResponse.usernameWithDomain.dropDomain(),
                domainPrefix = usernameDomainFeatureToggle.value,
                fullUsername = nameResponse.usernameWithDomain
            )
            UsernameDetails(
                ownerAddress = nameResponse.domainOwnerAddress,
                username = username
            )
        }
    }

    override suspend fun findUsernameDetailsByAddress(
        ownerAddress: Base58String
    ): List<UsernameDetails> = withContext(dispatchers.io) {
        val request = mapper.toLookupUsernameNetwork(ownerAddress)
        val response = usernameService.lookupUsername(request)
        mapper.fromNetwork(response).map {
            UsernameDetails(
                ownerAddress = ownerAddress,
                username = Username(
                    value = it.usernameWithDomain.dropDomain(),
                    domainPrefix = usernameDomainFeatureToggle.value,
                    fullUsername = it.usernameWithDomain
                )
            )
        }
    }
    private fun String.dropDomain(): String {
        return dropLastWhile { !it.isDot() }
    }
}
