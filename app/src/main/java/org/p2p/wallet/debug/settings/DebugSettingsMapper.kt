package org.p2p.wallet.debug.settings

import org.p2p.wallet.R
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.settings.model.SettingsRow

class DebugSettingsMapper(
    private val rpcEnvironment: NetworkEnvironmentManager,
    private val networkUrlProvider: NetworkServicesUrlProvider,
    private val tokenKeyProvider: TokenKeyProvider,
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val seedPhraseProvider: SeedPhraseProvider
) {
    fun mapMainSettings(): List<SettingsRow> = buildList {
        this += SettingsRow.Section(
            titleResId = R.string.debug_settings_notifications_title,
            iconRes = R.drawable.ic_settings_notification
        )
        this += SettingsRow.Section(
            titleResId = R.string.debug_settings_deeplinks_title,
            iconRes = R.drawable.ic_network
        )
        this += SettingsRow.Section(
            titleResId = R.string.debug_settings_feature_toggles_title,
            iconRes = R.drawable.ic_home_settings
        )
        this += SettingsRow.Section(
            titleResId = R.string.debug_settings_logs_title,
            subtitleRes = R.string.debug_settings_logs_subtitle,
            iconRes = R.drawable.ic_settings_cloud
        )
        if (seedPhraseProvider.isWeb3AuthUser) {
            this += SettingsRow.Section(
                titleResId = R.string.debug_settings_web3,
                iconRes = R.drawable.ic_settings_contacts
            )
        }

        this += createEnvironmentSettings()

        val userPublicKey = tokenKeyProvider.publicKey
        if (userPublicKey.isNotBlank()) {
            this += SettingsRow.Section(
                titleResId = R.string.debug_settings_stub_public_key,
                subtitle = userPublicKey,
                iconRes = R.drawable.ic_key,
                isDivider = true
            )
        }

        this += createStrigaSettings()
    }

    private fun createEnvironmentSettings(): List<SettingsRow> = buildList {
        val networkName = rpcEnvironment.loadCurrentEnvironment().name
        this += SettingsRow.Section(
            titleResId = R.string.debug_settings_network,
            subtitle = networkName,
            iconRes = R.drawable.ic_settings_network
        )

        val feeRelayerUrl = networkUrlProvider.loadFeeRelayerEnvironment().baseUrl
        this += SettingsRow.Section(
            titleResId = R.string.debug_settings_fee_relayer,
            subtitle = feeRelayerUrl,
            iconRes = R.drawable.ic_network
        )

        val torusUrl = networkUrlProvider.loadTorusEnvironment().baseUrl
        this += SettingsRow.Section(
            titleResId = R.string.debug_settings_torus,
            subtitle = torusUrl,
            iconRes = R.drawable.ic_network
        )

        val notificationServiceUrl = networkUrlProvider.loadNotificationServiceEnvironment().baseUrl
        this += SettingsRow.Section(
            titleResId = R.string.debug_settings_notification_service,
            subtitle = notificationServiceUrl,
            iconRes = R.drawable.ic_network,
            isDivider = true
        )

        val nameServiceEnvironment = networkUrlProvider.loadNameServiceEnvironment()
        this += SettingsRow.Switcher(
            titleResId = R.string.debug_settings_name_service,
            iconRes = R.drawable.ic_network,
            isDivider = false,
            subtitle = nameServiceEnvironment.baseUrl,
            isSelected = nameServiceEnvironment.isProductionSelected
        )

        val moonpayEnvironment = networkUrlProvider.loadMoonpayEnvironment()
        this += SettingsRow.Switcher(
            titleResId = R.string.debug_settings_moonpay_sandbox,
            iconRes = R.drawable.ic_network,
            isDivider = true,
            subtitle = moonpayEnvironment.baseServerSideUrl,
            isSelected = moonpayEnvironment.isSandboxEnabled
        )
    }

    private fun createStrigaSettings(): List<SettingsRow> = buildList {
        this += SettingsRow.Title(
            titleResId = R.string.debug_settings_striga_title,
        )

        val strigaKycBannerMock = inAppFeatureFlags.strigaKycBannerMockFlag.featureValueString
        val allBanners = StrigaKycStatusBanner.values()
            .map(StrigaKycStatusBanner::name)
            .plus("-")
        this += SettingsRow.PopupMenu(
            titleResId = R.string.debug_settings_kyc_mock_title,
            selectedItem = strigaKycBannerMock ?: "-",
            menuOptions = allBanners
        )

        this += SettingsRow.Section(
            titleResId = R.string.debug_settings_kyc_set_rejected_title,
            subtitle = "Click to send API call to Striga to simulate a REJECTED status",
            iconRes = R.drawable.ic_user
        )

        this += SettingsRow.Section(
            titleResId = R.string.debug_settings_striga_detach_user_id_title,
            subtitle = "Delete striga metadata from web3 metadata (requires app restart)",
            iconRes = R.drawable.ic_user,
        )
    }
}
