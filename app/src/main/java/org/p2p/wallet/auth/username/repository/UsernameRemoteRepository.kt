package org.p2p.wallet.auth.username.repository

import timber.log.Timber
import kotlinx.coroutines.withContext
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.auth.username.api.RegisterUsernameServiceApi
import org.p2p.wallet.auth.username.repository.mapper.RegisterUsernameServiceApiMapper
import org.p2p.wallet.auth.username.repository.model.UsernameDetails
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.core.crypto.Base58String

class UsernameRemoteRepository(
    private val usernameService: RegisterUsernameServiceApi,
    private val mapper: RegisterUsernameServiceApiMapper,
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val usernameParser: UsernameParser,
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
            usernameParser.parse(
                ownerAddress = nameResponse.domainOwnerAddress,
                anyUsername = nameResponse.usernameWithDomain
            )
        }
    }

    override suspend fun findUsernameDetailsByAddress(
        ownerAddress: Base58String
    ): List<UsernameDetails> = withContext(dispatchers.io) {
        val request = mapper.toLookupUsernameNetwork(ownerAddress)
        val response = usernameService.lookupUsername(request)
        mapper.fromNetwork(response).map {
            usernameParser.parse(
                ownerAddress = ownerAddress,
                anyUsername = it.usernameWithDomain
            )
        }
    }
}
