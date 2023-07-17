package org.p2p.token.service.model

sealed class TokenServiceResult<T> {
    data class Success<T>(val data: T) : TokenServiceResult<T>()

    data class Error<T>(val cause: Throwable) : TokenServiceResult<T>()
}

fun <T> TokenServiceResult<T>.unwrap(): T? {
    return when (this) {
        is TokenServiceResult.Success<T> -> this.data
        is TokenServiceResult.Error<T> -> throw this.cause
    }
}

fun <T> TokenServiceResult<T>.successOrNull(): T? {
    return when (this) {
        is TokenServiceResult.Success<T> -> this.data
        is TokenServiceResult.Error<T> -> null
    }
}
