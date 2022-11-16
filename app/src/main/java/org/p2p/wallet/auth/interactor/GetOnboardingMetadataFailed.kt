package org.p2p.wallet.auth.interactor

class GetOnboardingMetadataFailed(
    cause: Throwable
) : Throwable(message = "Get onboarding metadata failed to load", cause)
