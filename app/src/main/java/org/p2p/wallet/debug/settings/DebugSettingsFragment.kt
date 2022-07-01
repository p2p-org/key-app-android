package org.p2p.wallet.debug.settings

import androidx.annotation.StringRes
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentDebugSettingsBinding
import org.p2p.wallet.debug.featuretoggles.FeatureTogglesFragment
import org.p2p.wallet.debug.feerelayer.DebugFeeRelayerFragment
import org.p2p.wallet.debug.pushnotifications.PushNotificationsFragment
import org.p2p.wallet.debugdrawer.CustomLogDialog
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.settings.ui.network.SettingsNetworkFragment
import org.p2p.wallet.settings.ui.settings.SettingsAdapter
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.attachAdapter
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
    private val adapter = SettingsAdapter(::onSettingsRowClicked)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            settingsRecyclerView.attachAdapter(adapter)
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            when {
                result.containsKey(BUNDLE_KEY_NEW_NETWORK_NAME) -> {
                    onNetworkChanged(result)
                }
            }
        }
        presenter.loadData()
    }

    override fun showSettings(item: List<SettingsRow>) {
        adapter.setData(item)
    }

    private fun onSettingsRowClicked(@StringRes titleResId: Int) {
        when (titleResId) {
            R.string.debug_settings_notifications_title -> {
                replaceFragment(PushNotificationsFragment.create())
            }
            R.string.settings_network -> {
                addFragment(
                    SettingsNetworkFragment.create(REQUEST_KEY, BUNDLE_KEY_NEW_NETWORK_NAME),
                    enter = 0,
                    exit = 0,
                    popEnter = 0,
                    popExit = 0
                )
            }
            R.string.settings_fee_relayer -> {
                replaceFragment(DebugFeeRelayerFragment.create())
            }
            R.string.debug_settings_logs_title -> {
                CustomLogDialog(requireContext()).show()
            }
            R.string.debug_settings_feature_toggles_title -> {
                replaceFragment(FeatureTogglesFragment.create())
            }
        }
    }

    private fun onNetworkChanged(bundle: Bundle) {
        val networkName = bundle.getString(BUNDLE_KEY_NEW_NETWORK_NAME)
        if (!networkName.isNullOrEmpty()) {
            presenter.onNetworkChanged(newName = networkName)
        }
    }
}
