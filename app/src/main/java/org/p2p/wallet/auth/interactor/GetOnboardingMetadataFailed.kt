package org.p2p.wallet.auth.interactor

sealed class GetOnboardingMetadataFailed(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable() {
    class OnboardingMetadataRequestFailure(cause: Throwable) : GetOnboardingMetadataFailed(
        message = "Get onboarding metadata failed to load",
        cause = cause
    )

    class GetOnboardingMetadataNoAccount : GetOnboardingMetadataFailed(
        message = "!!! Account not found for such request !!!"
    )

    class GetOnboardingMetadataNoSeedPhrase : GetOnboardingMetadataFailed(
        message = "User has no seed phrase, trouble of the old versions of the app"
    )
}
