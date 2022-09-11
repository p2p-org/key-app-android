package org.p2p.wallet.auth.interactor

import org.p2p.wallet.auth.model.OnboardingFlow
import timber.log.Timber

class OnboardingInteractor {

    var currentFlow: OnboardingFlow = OnboardingFlow.CreateWallet
        set(value) {
            field = value
            Timber.i("Current onboarding flow switched to $field")
        }
}
