package org.p2p.solanaj.utils


sealed class Result<out S> {
    data class Success<S>(val data: S?) : Result<S>()
    data class Error<E>(val errors: E) : Result<E>()
}