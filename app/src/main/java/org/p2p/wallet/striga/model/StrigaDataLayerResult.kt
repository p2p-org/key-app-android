package org.p2p.wallet.striga.model

sealed class StrigaDataLayerResult<out T> {
    data class Success<T>(val value: T) : StrigaDataLayerResult<T>()
    class Failure<T>(val error: StrigaDataLayerError) : StrigaDataLayerResult<T>()

    @Throws(StrigaDataLayerError::class)
    fun unwrap(): T = when (this) {
        is Success -> value
        is Failure -> throw error
    }

    @Throws(StrigaDataLayerError::class)
    fun successOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    fun onSuccess(action: (T) -> Unit): StrigaDataLayerResult<T> {
        successOrNull()?.also(action)
        return this
    }

    fun onFailure(action: (StrigaDataLayerError) -> Unit): StrigaDataLayerResult<T> {
        if (this is Failure) {
            error.also(action)
        }
        return this
    }

    inline fun <reified E : StrigaDataLayerError> onTypedFailure(
        action: (E) -> Unit
    ): StrigaDataLayerResult<T> {
        if (this is Failure && this.error is E) {
            error.also(action)
        }
        return this
    }
}

fun <T, E : StrigaDataLayerError> E.toFailureResult(): StrigaDataLayerResult.Failure<T> =
    StrigaDataLayerResult.Failure(this)

fun <T> T.toSuccessResult(): StrigaDataLayerResult.Success<T> =
    StrigaDataLayerResult.Success(this)

inline fun <T, R> StrigaDataLayerResult<T>.map(transform: (T) -> R): StrigaDataLayerResult<R> {
    return when (this) {
        is StrigaDataLayerResult.Success -> StrigaDataLayerResult.Success(transform(value))
        is StrigaDataLayerResult.Failure -> StrigaDataLayerResult.Failure(error)
    }
}
