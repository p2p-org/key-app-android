package org.p2p.wallet.auth.model

sealed interface MetadataLoadStatus {
    object Success : MetadataLoadStatus
    object Canceled : MetadataLoadStatus
    object NoWeb3EthereumPublicKey : MetadataLoadStatus
    data class Failure(
        val cause: Throwable,
        val message: String? = cause.message
    ) : MetadataLoadStatus

    fun throwIfFailure() {
        if (this is Failure) {
            throw this.cause
        }
    }
}
