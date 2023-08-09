package org.p2p.wallet.home.addmoney.repository

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.R
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.home.addmoney.mapper.AddMoneyCellMapper
import org.p2p.wallet.home.addmoney.model.AddMoneyItemType
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupInteractor

class AddMoneyCellsRepository(
    private val settingsInteractor: SettingsInteractor,
    private val strigaSignupFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val appFeatureFlags: InAppFeatureFlags,
    private val strigaSignupInteractor: StrigaSignupInteractor,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val addMoneyCellMapper: AddMoneyCellMapper,
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

    fun getCells(): List<AnyCellItem> = buildList {
        // show only if striga is disabled or striga is enabled and supported for chosen country
        val showBankTransferItem = !isStrigaEnabled || isStrigaSupportedForCurrentCountry

        // bank transfer
        if (showBankTransferItem) {
            val bankTransferSubtitleRes = if (isStrigaEnabled) {
                R.string.bank_transfer_subtitle_zero_fees
            } else {
                R.string.bank_transfer_subtitle_one_percent_fees
            }

            this += addMoneyCellMapper.getFinanceBlock(
                titleResId = R.string.bank_transfer_title,
                subtitleRes = bankTransferSubtitleRes,
                iconResId = R.drawable.ic_bank_transfer,
                backgroundTintId = R.color.light_grass,
                showRightProgress = false,
                payload = AddMoneyItemType.BankTransfer
            )
        }

        // bank card
        this += addMoneyCellMapper.getFinanceBlock(
            titleResId = R.string.bank_card_title,
            subtitleRes = R.string.bank_card_subtitle,
            iconResId = R.drawable.ic_bank_card,
            backgroundTintId = R.color.light_sea,
            payload = AddMoneyItemType.BankCard,
        )

        // crypto
        this += addMoneyCellMapper.getFinanceBlock(
            titleResId = R.string.crypto_title,
            subtitleRes = R.string.crypto_subtitle,
            iconResId = R.drawable.ic_crypto,
            backgroundTintId = R.color.light_sun,
            payload = AddMoneyItemType.Crypto,
        )
    }
}
