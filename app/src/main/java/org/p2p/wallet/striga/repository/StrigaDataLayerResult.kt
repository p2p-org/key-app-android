package org.p2p.wallet.striga.repository

import org.p2p.wallet.striga.model.StrigaDataLayerError

sealed interface StrigaDataLayerResult<out T, out E : StrigaDataLayerError> {
    data class Success<T, E : StrigaDataLayerError>(val value: T) : StrigaDataLayerResult<T, E>
    class Failure<T, E : StrigaDataLayerError>(val error: E) : StrigaDataLayerResult<T, E>
}
