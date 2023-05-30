package org.p2p.wallet.auth.interactor

sealed class MetadataFailed(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable() {
    class OnboardingMetadataRequestFailure(cause: Throwable) : MetadataFailed(
        message = "Get onboarding metadata failed to load",
        cause = cause
    )

    class MetadataNoAccount : MetadataFailed(
        message = "!!! Account not found for such request !!!"
    )

    class MetadataNoSeedPhrase : MetadataFailed(
        message = "User has no seed phrase, trouble of the old versions of the app"
    )
}
