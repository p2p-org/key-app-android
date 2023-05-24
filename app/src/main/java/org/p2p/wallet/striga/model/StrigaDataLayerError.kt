package org.p2p.wallet.striga.model

sealed class StrigaDataLayerError(override val message: String) : Throwable() {
    class DatabaseError(
        override val cause: Throwable,
    ) : StrigaDataLayerError("Error while working with database: ${cause.message}")

    class MappingFailed(
        message: String
    ) : StrigaDataLayerError(message)

    class InternalError(
        override val cause: Throwable
    ) : StrigaDataLayerError(cause.message.orEmpty())
}
