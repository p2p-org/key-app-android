package org.p2p.token.service.model

sealed class TokenServiceResult<T> {
    data class Success<T>(val data: T) : TokenServiceResult<T>()

    sealed class Error(val cause: Throwable) : TokenServiceResult<Throwable>()
}

fun <T> TokenServiceResult<T>.unwrap(): T? {
    return when (this) {
        is TokenServiceResult.Success<T> -> this.data
        is TokenServiceResult.Error -> throw cause
    }
}
