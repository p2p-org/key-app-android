package org.p2p.wallet.auth.username.repository.model

sealed class UsernameServiceError(
    override val message: String,
    val errorCode: Int
) : Throwable(message) {

    class RequestCreationFailure(
        override val message: String,
        override val cause: Throwable?
    ) : UsernameServiceError(message, -1)

    class InternalServiceError(
        message: String
    ) : UsernameServiceError(message, -32603)

    class UsernameAlreadyExists(
        message: String
    ) : UsernameServiceError(message, -32001)

    class InvalidUsername(
        message: String
    ) : UsernameServiceError(message, -32002)

    class InvalidAuth(
        message: String
    ) : UsernameServiceError(message, -32003)

    class InvalidJsonRequest(
        message: String
    ) : UsernameServiceError(message, -32700)

    class UsernameNotFound(
        message: String
    ) : UsernameServiceError(message, -32004)

    class UnknownError(
        message: String,
        errorCode: Int
    ) : UsernameServiceError(message, errorCode)
}
