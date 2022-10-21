package org.p2p.wallet.auth.username.repository.mapper

import org.p2p.wallet.auth.username.api.response.RegisterUsernameServiceErrorResponse
import org.p2p.wallet.auth.username.repository.model.UsernameServiceError

class UsernameErrorMapper {
    fun fromNetwork(
        errorResponse: RegisterUsernameServiceErrorResponse
    ): UsernameServiceError = when (errorResponse.errorCode) {
        -32603 -> UsernameServiceError.InternalServiceError(errorResponse.errorMessage)
        -32001 -> UsernameServiceError.UsernameAlreadyExists(errorResponse.errorMessage)
        -32002 -> UsernameServiceError.InvalidUsername(errorResponse.errorMessage)
        -32003 -> UsernameServiceError.InvalidAuth(errorResponse.errorMessage)
        -32700 -> UsernameServiceError.InvalidJsonRequest(errorResponse.errorMessage)
        -32004 -> UsernameServiceError.UsernameNotFound(errorResponse.errorMessage)
        else -> UsernameServiceError.UnknownError(errorResponse.errorMessage, errorResponse.errorCode)
    }
}
