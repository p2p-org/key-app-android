package org.p2p.wallet.auth.model

sealed interface MetadataLoadStatus {
    object Success : MetadataLoadStatus
    object Canceled : MetadataLoadStatus
    object NoEthereumPublicKey : MetadataLoadStatus
    data class Failure(
        val cause: Throwable,
        val message: String? = cause.message
    ) : MetadataLoadStatus
}
