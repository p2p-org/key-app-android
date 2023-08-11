package org.p2p.wallet.home.addmoney.repository

import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.countrycode.ExternalCountryCodeLoadError
import org.p2p.wallet.countrycode.repository.ExternalCountryCodeRepository
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
    private val seedPhraseProvider: SeedPhraseProvider,
    private val externalCountryCodeRepository: ExternalCountryCodeRepository,
) {
    private val isUserAuthByWeb3: Boolean
        get() = seedPhraseProvider.isWeb3AuthUser || appFeatureFlags.strigaSimulateWeb3Flag.featureValue

    private val isStrigaEnabled: Boolean
        get() = strigaSignupFeatureToggle.isFeatureEnabled && isUserAuthByWeb3

    /**
     * Exception might be thrown if can't check whether striga supports current country
     */
    @Throws(ExternalCountryCodeLoadError::class, IllegalStateException::class)
    suspend fun getButtons(): List<AddMoneyButton> = buildList {
        // show only if striga is disabled or striga is enabled and supported for chosen country
        val showBankTransferItem = !isStrigaEnabled || isStrigaSupportedForCurrentCountry()
        val isMoonpayAvailable = isMoonpaySupportedForCurrentCountry()

        // bank transfer
        if (showBankTransferItem) {
            if (isStrigaEnabled) {
                this += AddMoneyButton(
                    type = AddMoneyButtonType.BANK_TRANSFER_STRIGA,
                )
            } else if (isMoonpayAvailable) {
                this += AddMoneyButton(
                    type = AddMoneyButtonType.BANK_TRANSFER_MOONPAY,
                )
            }
        }

        // bank card
        if (isMoonpayAvailable) {
            this += AddMoneyButton(
                type = AddMoneyButtonType.BANK_CARD,
            )
        }

        // crypto
        this += AddMoneyButton(
            type = AddMoneyButtonType.CRYPTO,
        )
    }

    @Throws(ExternalCountryCodeLoadError::class, IllegalStateException::class)
    private suspend fun isStrigaSupportedForCurrentCountry(): Boolean {
        return settingsInteractor.userCountryCode
            ?.let { strigaSignupInteractor.checkCountryIsSupported(it) }
            ?: false
    }

    @Throws(ExternalCountryCodeLoadError::class, IllegalStateException::class)
    private suspend fun isMoonpaySupportedForCurrentCountry(): Boolean {
        return settingsInteractor.userCountryCode
            ?.let { externalCountryCodeRepository.isMoonpaySupported(it) }
            ?: false
    }
}
