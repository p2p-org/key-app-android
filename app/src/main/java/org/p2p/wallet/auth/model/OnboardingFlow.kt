package org.p2p.wallet.auth.model

sealed interface OnboardingFlow {
    object CreateWallet : OnboardingFlow

    open class RestoreWallet : OnboardingFlow {
        object SocialPlusCustomShare : RestoreWallet()

        object DevicePlusCustomShare : RestoreWallet()
    }
}
