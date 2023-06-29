package org.p2p.wallet.auth.username.repository.mapper

import org.near.borshj.BorshBuffer
import org.p2p.wallet.auth.username.api.request.CreateNameRequest
import org.p2p.wallet.auth.username.api.request.CreateNameRequestCredentials
import org.p2p.wallet.auth.username.api.request.GetNameRequest
import org.p2p.wallet.auth.username.api.request.LookupNameRequest
import org.p2p.wallet.auth.username.api.request.RegisterUsernameServiceJsonRpcMethod
import org.p2p.wallet.auth.username.api.request.RegisterUsernameServiceRequest
import org.p2p.wallet.auth.username.api.request.ResolveNameRequest
import org.p2p.wallet.auth.username.api.response.RegisterUsernameServiceListResponse
import org.p2p.wallet.auth.username.api.response.RegisterUsernameServiceResponse
import org.p2p.wallet.auth.username.repository.model.UsernameServiceError
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.p2p.solanaj.utils.TweetNaclFast

private const val TIMESTAMP_PATTERN_USERNAME_SERVICE = "yyyy-MM-dd HH:mm:ssXXX"

class RegisterUsernameServiceApiMapper(
    private val errorMapper: UsernameErrorMapper
) {
    @Throws(UsernameServiceError::class)
    fun <Body> fromNetwork(response: RegisterUsernameServiceResponse<Body>): Body {
        if (response.errorBody != null) {
            throw errorMapper.fromNetwork(response.errorBody)
        }
        return response.result!!
    }

    @Throws(UsernameServiceError::class)
    fun <Body> fromNetwork(response: RegisterUsernameServiceListResponse<Body>): List<Body> {
        if (response.errorBody != null) {
            throw errorMapper.fromNetwork(response.errorBody)
        }
        return response.result!!
    }

    fun toGetUsernameNetwork(username: String): RegisterUsernameServiceRequest<GetNameRequest> {
        return RegisterUsernameServiceRequest(
            params = GetNameRequest(username),
            methodName = RegisterUsernameServiceJsonRpcMethod.GET_NAME
        )
    }

    fun toCreateUsernameNetwork(
        username: String,
        userPublicKey: Base58String,
        userPrivateKey: ByteArray
    ): RegisterUsernameServiceRequest<CreateNameRequest> {
        val credentials = createCredentialsField(userPublicKey, userPrivateKey)
        return RegisterUsernameServiceRequest(
            params = CreateNameRequest(name = username, owner = userPublicKey, credentials = credentials),
            methodName = RegisterUsernameServiceJsonRpcMethod.CREATE_NAME
        )
    }

    private fun createCredentialsField(
        userPublicKey: Base58String,
        userPrivateKey: ByteArray
    ): CreateNameRequestCredentials = try {
        val requestDate = Date()
        val serializedCredentials: ByteArray = createSerializedCredentials(userPublicKey.base58Value, requestDate)
        val signature = TweetNaclFast.Signature(byteArrayOf(), userPrivateKey.copyOf())
            .detached(serializedCredentials)
            .toBase58Instance()

        CreateNameRequestCredentials.Web3AuthCredentials(
            signature = signature,
            timestamp = formatTimestampField(requestDate)
        )
    } catch (creatingFailure: Throwable) {
        Timber.i(creatingFailure)
        throw UsernameServiceError.RequestCreationFailure(
            message = "Failed to create credentials field",
            cause = creatingFailure
        )
    }

    private fun createSerializedCredentials(userPublicKey: String, requestDate: Date) =
        BorshBuffer.allocate(1024).run {
            // order matters
            write(userPublicKey)
            write(requestDate.time.div(1000)) // in seconds
            toByteArray()
        }

    private fun formatTimestampField(timestamp: Date): String {
        return SimpleDateFormat(TIMESTAMP_PATTERN_USERNAME_SERVICE, Locale.getDefault()).format(timestamp)
    }

    fun toResolveUsernameNetwork(username: String): RegisterUsernameServiceRequest<ResolveNameRequest> {
        return RegisterUsernameServiceRequest(
            params = ResolveNameRequest(username),
            methodName = RegisterUsernameServiceJsonRpcMethod.RESOLVE_NAME
        )
    }

    fun toLookupUsernameNetwork(usernameOwnerAddress: Base58String): RegisterUsernameServiceRequest<LookupNameRequest> {
        return RegisterUsernameServiceRequest(
            params = LookupNameRequest(usernameOwnerAddress),
            methodName = RegisterUsernameServiceJsonRpcMethod.LOOKUP_NAME
        )
    }
}
