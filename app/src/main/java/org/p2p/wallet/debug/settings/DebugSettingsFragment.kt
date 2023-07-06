package org.p2p.wallet.debug.settings

import androidx.annotation.StringRes
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentDebugSettingsBinding
import org.p2p.wallet.debug.featuretoggles.FeatureTogglesFragment
import org.p2p.wallet.debug.feerelayer.DebugFeeRelayerFragment
import org.p2p.wallet.debug.logs.CustomLogDialog
import org.p2p.wallet.debug.publickey.DebugPublicKeyFragment
import org.p2p.wallet.debug.pushnotifications.PushNotificationsFragment
import org.p2p.wallet.debug.pushservice.DebugPushServiceFragment
import org.p2p.wallet.debug.pushservice.DebugWeb3Fragment
import org.p2p.wallet.debug.settings.adapter.DebugSettingsRowAdapter
import org.p2p.wallet.debug.settings.adapter.settingsRowInfoItemDelegate
import org.p2p.wallet.debug.settings.adapter.settingsRowLogoutItemDelegate
import org.p2p.wallet.debug.settings.adapter.settingsRowPopupMenuItemDelegate
import org.p2p.wallet.debug.settings.adapter.settingsRowSectionItemDelegate
import org.p2p.wallet.debug.settings.adapter.settingsRowSwtichItemDelegate
import org.p2p.wallet.debug.settings.adapter.settingsRowTitleItemDelegate
import org.p2p.wallet.debug.torus.DebugTorusFragment
import org.p2p.wallet.debug.uikit.DebugUiKitFragment
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.settings.ui.network.SettingsNetworkBottomSheet
import org.p2p.wallet.utils.getSerializableOrNull
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val BUNDLE_KEY_NEW_NETWORK_NAME = "EXTRA_NETWORK_NAME"

class DebugSettingsFragment :
    BaseMvpFragment<DebugSettingsContract.View, DebugSettingsContract.Presenter>(R.layout.fragment_debug_settings),
    DebugSettingsContract.View {

    companion object {
        fun create(): DebugSettingsFragment = DebugSettingsFragment()
    }

    override val presenter: DebugSettingsContract.Presenter by inject()

    private val binding: FragmentDebugSettingsBinding by viewBinding()

    private val adapterDelegate = DebugSettingsRowAdapter(
        settingsRowTitleItemDelegate(),
        settingsRowSectionItemDelegate(::onSettingsRowClicked),
        settingsRowLogoutItemDelegate(::onSettingsRowClicked),
        settingsRowInfoItemDelegate(::onSettingsRowClicked),
        settingsRowSwtichItemDelegate(::onSettingsSwitchClicked),
        settingsRowPopupMenuItemDelegate(presenter::onSettingsPopupMenuClicked)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            settingsRecyclerView.attachAdapter(adapterDelegate)
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
        }

        childFragmentManager.setFragmentResultListener(REQUEST_KEY, viewLifecycleOwner) { _, result ->
            when {
                result.containsKey(BUNDLE_KEY_NEW_NETWORK_NAME) -> {
                    onNetworkChanged(result)
                }
            }
        }
        presenter.loadData()
    }

    override fun showSettings(items: List<SettingsRow>) {
        requireView().post {
            adapterDelegate.items = items
        }
    }

    private fun onSettingsRowClicked(@StringRes titleResId: Int) {
        when (titleResId) {
            R.string.debug_settings_notifications_title -> {
                replaceFragment(PushNotificationsFragment.create())
            }
            R.string.debug_settings_network -> {
                SettingsNetworkBottomSheet.show(childFragmentManager, REQUEST_KEY, BUNDLE_KEY_NEW_NETWORK_NAME)
            }
            R.string.debug_settings_fee_relayer -> {
                replaceFragment(DebugFeeRelayerFragment.create())
            }
            R.string.debug_settings_notification_service -> {
                replaceFragment(DebugPushServiceFragment.create())
            }
            R.string.debug_settings_torus -> {
                replaceFragment(DebugTorusFragment.create())
            }
            R.string.debug_settings_logs_title -> {
                CustomLogDialog(requireContext()).show()
            }
            R.string.debug_settings_feature_toggles_title -> {
                replaceFragment(FeatureTogglesFragment.create())
            }
            R.string.debug_settings_stub_public_key -> {
                replaceFragment(DebugPublicKeyFragment.create())
            }
            R.string.debug_settings_web3 -> {
                replaceFragment(DebugWeb3Fragment.create())
            }
            R.string.debug_settings_kyc_set_rejected_title -> {
                presenter.onClickSetKycRejected()
            }
            R.string.debug_settings_striga_detach_user_id_title -> {
                presenter.onClickDetachStrigaUser()
            }
            R.string.debug_settings_ui_kit -> {
                replaceFragment(DebugUiKitFragment())
            }
        }
    }

    private fun onSettingsSwitchClicked(@StringRes titleResId: Int, isChecked: Boolean) {
        when (titleResId) {
            R.string.debug_settings_moonpay_sandbox -> {
                presenter.switchMoonpayUrl(isSandboxSelected = isChecked)
            }
            R.string.debug_settings_name_service -> {
                presenter.switchNameServiceUrl(isProdSelected = isChecked)
            }
        }
    }

    private fun onNetworkChanged(bundle: Bundle) {
        bundle.getSerializableOrNull<NetworkEnvironment>(BUNDLE_KEY_NEW_NETWORK_NAME)?.let {
            presenter.onNetworkChanged(it)
        }
    }
}
