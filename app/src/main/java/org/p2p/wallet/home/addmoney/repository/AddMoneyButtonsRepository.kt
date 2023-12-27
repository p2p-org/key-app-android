package org.p2p.wallet.home.addmoney.repository

import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.home.addmoney.model.AddMoneyButton
import org.p2p.wallet.home.addmoney.model.AddMoneyButtonType
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupInteractor

class AddMoneyButtonsRepository(
    private val settingsInteractor: SettingsInteractor,
    private val strigaSignupFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val appFeatureFlags: InAppFeatureFlags,
    private val strigaSignupInteractor: StrigaSignupInteractor,
    private val seedPhraseProvider: SeedPhraseProvider
) {
    private val isUserAuthByWeb3: Boolean
        get() = seedPhraseProvider.isWeb3AuthUser || appFeatureFlags.strigaSimulateWeb3Flag.featureValue

    private val isStrigaEnabled: Boolean
        get() = strigaSignupFeatureToggle.isFeatureEnabled && isUserAuthByWeb3

    private val isStrigaSupportedForCurrentCountry: Boolean
        get() {
            return settingsInteractor.userCountryCode
                ?.let(strigaSignupInteractor::checkCountryIsSupported)
                ?: false
        }

    fun getButtons(): List<AddMoneyButton> = buildList {
        // show only if striga is disabled or striga is enabled and supported for chosen country
        val showBankTransferItem = !isStrigaEnabled || isStrigaSupportedForCurrentCountry

        // bank transfer
        if (showBankTransferItem) {
            this += if (isStrigaEnabled) {
                AddMoneyButton(
                    type = AddMoneyButtonType.BANK_TRANSFER_STRIGA,
                )
            } else {
                AddMoneyButton(
                    type = AddMoneyButtonType.BANK_TRANSFER_MOONPAY,
                )
            }
        }

        // bank card
        this += AddMoneyButton(
            type = AddMoneyButtonType.BANK_CARD,
        )

        // crypto
        this += AddMoneyButton(
            type = AddMoneyButtonType.CRYPTO,
        )
    }
}
