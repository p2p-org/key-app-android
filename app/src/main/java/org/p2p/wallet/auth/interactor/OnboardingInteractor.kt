package org.p2p.wallet.auth.interactor

import timber.log.Timber

class OnboardingInteractor {

    enum class OnboardingFlow {
        CREATE_WALLET,
        RESTORE_WALLET
    }

    var currentFlow: OnboardingFlow = OnboardingFlow.CREATE_WALLET
        set(value) {
            field = value
            Timber.i("Current onboarding flow switched to ${field.name}")
        }
}
