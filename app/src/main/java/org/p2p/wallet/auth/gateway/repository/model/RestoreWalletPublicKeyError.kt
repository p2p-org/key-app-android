package org.p2p.wallet.auth.gateway.repository.model

class RestoreWalletPublicKeyError(
    expectedPublicKey: String?,
    actualPublicKey: String?,
    override val message: String =
        "confirm_restore_wallet is called with different pubkey: " +
            "expected: $expectedPublicKey; actual:$actualPublicKey. " +
            "Regenerate pubkey and call restore_wallet again."
) : Throwable()
