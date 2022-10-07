package org.p2p.wallet.auth.interactor

import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.PhoneNumber
import timber.log.Timber

class OnboardingInteractor {

    var currentFlow: OnboardingFlow = OnboardingFlow.CreateWallet
        set(value) {
            field = value
            Timber.i("Current onboarding flow switched to $field")
        }

    // Use this variable when user tried to submit phone number on RESTORE or CREATE
    // But not submitted it successfully yet,
    var temporaryPhoneNumber: PhoneNumber? = null
        set(value) {
            field = value
            Timber.i("Current temporaryPhoneNumber switched to ${field?.formattedValue?.length}")
        }
}
