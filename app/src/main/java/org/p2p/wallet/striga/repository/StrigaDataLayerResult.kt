package org.p2p.wallet.striga.repository

import org.p2p.wallet.striga.model.StrigaDataLayerError

sealed interface StrigaDataLayerResult<out T> {
    data class Success<T>(val value: T) : StrigaDataLayerResult<T>
    class Failure<T>(val error: StrigaDataLayerError) : StrigaDataLayerResult<T>

    @Throws(StrigaDataLayerError::class)
    fun unwrap(): T = when (this) {
        is Success -> value
        is Failure -> throw error
    }
}
