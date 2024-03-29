package org.p2p.wallet.debug.settings

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import org.koin.android.ext.android.inject
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentDebugSettingsBinding
import org.p2p.wallet.debug.featuretoggles.FeatureTogglesFragment
import org.p2p.wallet.debug.logs.CustomLogDialog
import org.p2p.wallet.debug.publickey.DebugPublicKeyFragment
import org.p2p.wallet.debug.pushnotifications.PushNotificationsFragment
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
import org.p2p.wallet.utils.toDp
import org.p2p.wallet.utils.viewbinding.context
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
        settingsRowSwtichItemDelegate(presenter::onSettingsSwitchClicked),
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
                SettingsNetworkBottomSheet.show(
                    fm = childFragmentManager,
                    requestKey = REQUEST_KEY,
                    resultKey = BUNDLE_KEY_NEW_NETWORK_NAME
                )
            }
            R.string.debug_settings_torus -> {
                replaceFragment(DebugTorusFragment.create())
            }
            R.string.debug_settings_swap -> {
                showChangeSwapUrlDialog()
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
            R.string.debug_settings_reset_user_country_title -> {
                presenter.onClickResetUserCountry()
            }
        }
    }

    private fun onNetworkChanged(bundle: Bundle) {
        bundle.getSerializableOrNull<NetworkEnvironment>(BUNDLE_KEY_NEW_NETWORK_NAME)?.let {
            presenter.onNetworkChanged(it)
        }
    }

    private fun showChangeSwapUrlDialog() {
        val editText = EditText(binding.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(20.toDp(), 20.toDp(), 20.toDp(), 0) }
            setText("")
            setTextAppearance(R.style.UiKit_TextAppearance_Regular_Text1)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        AlertDialog.Builder(binding.context).apply {
            setView(editText)
            setPositiveButton("Change") { dialog, _ ->
                presenter.onSwapUrlChanged(urlValue = editText.text.toString(),)
                dialog.dismiss()
            }
            setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
        }
            .show()
    }
}
