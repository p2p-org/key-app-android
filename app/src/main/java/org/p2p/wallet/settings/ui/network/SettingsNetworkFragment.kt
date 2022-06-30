package org.p2p.wallet.settings.ui.network

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import org.koin.android.ext.android.inject
import org.p2p.solanaj.rpc.NetworkEnvironment
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSettingsNetworkBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class SettingsNetworkFragment :
    BaseMvpFragment<SettingsNetworkContract.View, SettingsNetworkContract.Presenter>(
        R.layout.fragment_settings_network
    ),
    SettingsNetworkContract.View {

    companion object {
        fun create(requestKey: String, resultKey: String): SettingsNetworkFragment = SettingsNetworkFragment().withArgs(
            EXTRA_REQUEST_KEY to requestKey,
            EXTRA_RESULT_KEY to resultKey
        )
    }

    override val presenter: SettingsNetworkContract.Presenter by inject()

    private val binding: FragmentSettingsNetworkBinding by viewBinding()
    private val resultKey: String by args(EXTRA_RESULT_KEY)
    private val requestKey: String by args(EXTRA_REQUEST_KEY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attach(this)
        with(binding) {
            networksGroup.setOnCheckedChangeListener(this@SettingsNetworkFragment)

            primaryButton.setOnClickListener {
                presenter.save()
            }

            secondaryButton.setOnClickListener {
                popBackStack()
            }
        }
        presenter.loadData()
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        val environment = when (checkedId) {
            R.id.mainnetButton -> NetworkEnvironment.MAINNET
            R.id.rpcpoolButton -> NetworkEnvironment.RPC_POOL
            R.id.solanaButton -> NetworkEnvironment.SOLANA
            R.id.devnetButton -> NetworkEnvironment.DEVNET
            else -> error("No environment found for this id: $checkedId")
        }
        presenter.setNewEnvironment(environment)
    }

    override fun showEnvironment(
        currentNetwork: NetworkEnvironment,
        availableNetworks: List<NetworkEnvironment>,
        isDevnetEnabled: Boolean
    ) {
        binding.solanaButton.isVisible = NetworkEnvironment.SOLANA in availableNetworks
        binding.mainnetButton.isVisible = NetworkEnvironment.MAINNET in availableNetworks
        binding.rpcpoolButton.isVisible = NetworkEnvironment.RPC_POOL in availableNetworks
        binding.devnetButton.isVisible = NetworkEnvironment.DEVNET in availableNetworks && isDevnetEnabled

        val checkedButtonId = when (currentNetwork) {
            NetworkEnvironment.SOLANA -> R.id.solanaButton
            NetworkEnvironment.MAINNET -> R.id.mainnetButton
            NetworkEnvironment.RPC_POOL -> R.id.rpcpoolButton
            NetworkEnvironment.DEVNET -> R.id.devnetButton
        }
        binding.networksGroup.apply {
            setOnCheckedChangeListener(null)
            check(checkedButtonId)
            setOnCheckedChangeListener(this@SettingsNetworkFragment)
        }
    }

    override fun onNetworkChanged(newName: String) {
        setFragmentResult(requestKey, bundleOf(Pair(resultKey, newName)))
        popBackStack()
    }
}
