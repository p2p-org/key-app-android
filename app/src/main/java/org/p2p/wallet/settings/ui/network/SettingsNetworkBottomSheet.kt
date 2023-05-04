package org.p2p.wallet.settings.ui.network

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogSettingsNetworkBinding
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val ARG_RESULT_KEY = "EXTRA_RESULT_KEY"

class SettingsNetworkBottomSheet :
    BaseMvpBottomSheet<SettingsNetworkContract.View, SettingsNetworkContract.Presenter>(
        R.layout.dialog_settings_network
    ),
    SettingsNetworkContract.View {

    companion object {
        fun show(fm: FragmentManager, requestKey: String, resultKey: String) {
            SettingsNetworkBottomSheet()
                .withArgs(
                    ARG_REQUEST_KEY to requestKey,
                    ARG_RESULT_KEY to resultKey
                )
                .show(fm, SettingsNetworkBottomSheet::javaClass.name)
        }
    }

    override val presenter: SettingsNetworkContract.Presenter by inject()

    private val binding: DialogSettingsNetworkBinding by viewBinding()

    private val resultKey: String by args(ARG_RESULT_KEY)
    private val requestKey: String by args(ARG_REQUEST_KEY)

    private val networkAdapter: SelectNetworkAdapter by lazy {
        SelectNetworkAdapter { presenter.onNewEnvironmentSelected(it) }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            recyclerViewNetworks.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewNetworks.attachAdapter(networkAdapter)

            buttonDone.setOnClickListener { dismissAllowingStateLoss() }
        }
    }

    override fun showEnvironment(
        currentNetwork: NetworkEnvironment,
        availableNetworks: List<NetworkEnvironment>
    ) {
        networkAdapter.selectedItem = currentNetwork
        networkAdapter.setItems(availableNetworks)
    }

    override fun dismissBottomSheet() {
        dismissAllowingStateLoss()
    }

    override fun closeWithResult(newNetworkEnvironment: NetworkEnvironment) {
        setFragmentResult(requestKey, bundleOf(resultKey to newNetworkEnvironment))
        dismissAllowingStateLoss()
    }
}
