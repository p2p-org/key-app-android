package org.p2p.wallet.auth.interactor

sealed class MetadataMethodFailed(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable() {
    class MetadataRequestFailure(cause: Throwable) : MetadataMethodFailed(
        message = "Get onboarding metadata failed to load",
        cause = cause
    )

    class NoAccount : MetadataMethodFailed(
        message = "!!! Account not found for such request !!!"
    )

    class NoSeedPhrase : MetadataMethodFailed(
        message = "User has no seed phrase, trouble of the old versions of the app"
    )
}
