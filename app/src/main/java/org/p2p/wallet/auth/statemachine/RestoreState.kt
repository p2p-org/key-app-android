package org.p2p.wallet.auth.statemachine

import org.p2p.wallet.auth.model.OnboardingFlow

abstract class RestoreState {
    abstract fun onCustomFlow(): OnboardingFlow.RestoreWallet
    abstract fun onSocialFlow(): OnboardingFlow.RestoreWallet

    class CommonRestoreScreenState(private val isDeviceShareSaved: Boolean) : RestoreState() {

        override fun onCustomFlow(): OnboardingFlow.RestoreWallet {
            return if (isDeviceShareSaved) {
                OnboardingFlow.RestoreWallet.DevicePlusCustomShare
            } else {
                OnboardingFlow.RestoreWallet.SocialPlusCustomShare
            }
        }

        override fun onSocialFlow(): OnboardingFlow.RestoreWallet {
            return if (isDeviceShareSaved) {
                OnboardingFlow.RestoreWallet.DevicePlusSocialShare
            } else {
                OnboardingFlow.RestoreWallet.SocialPlusCustomShare
            }
        }
    }

    class DevicePlusCustomShareNotMatchState : RestoreState() {

        override fun onCustomFlow(): OnboardingFlow.RestoreWallet {
            return OnboardingFlow.RestoreWallet.DevicePlusCustomShare
        }

        override fun onSocialFlow(): OnboardingFlow.RestoreWallet {
            return OnboardingFlow.RestoreWallet.DevicePlusSocialOrSocialPlusCustom
        }
    }

    class DeviceCustomShareNotFound : RestoreState() {

        override fun onCustomFlow(): OnboardingFlow.RestoreWallet {
            return OnboardingFlow.RestoreWallet.DevicePlusCustomShare
        }

        override fun onSocialFlow(): OnboardingFlow.RestoreWallet {
            return OnboardingFlow.RestoreWallet.DevicePlusSocialOrSocialPlusCustom
        }
    }

    class DevicePlusSocialShareNotMatchState : RestoreState() {

        override fun onCustomFlow(): OnboardingFlow.RestoreWallet {
            return OnboardingFlow.RestoreWallet.DevicePlusCustomOrSocialPlusCustom
        }

        override fun onSocialFlow(): OnboardingFlow.RestoreWallet {
            return OnboardingFlow.RestoreWallet.DevicePlusSocialShare
        }
    }

    class DeviceSocialShareNotFoundState : RestoreState() {

        override fun onCustomFlow(): OnboardingFlow.RestoreWallet {
            return OnboardingFlow.RestoreWallet.DevicePlusCustomOrSocialPlusCustom
        }

        override fun onSocialFlow(): OnboardingFlow.RestoreWallet {
            return OnboardingFlow.RestoreWallet.DevicePlusSocialShare
        }
    }
}
