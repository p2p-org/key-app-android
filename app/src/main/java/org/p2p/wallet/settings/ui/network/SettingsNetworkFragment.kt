package org.p2p.wallet.settings.ui.network

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import org.p2p.wallet.R
import org.koin.android.ext.android.inject
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSettingsNetworkBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class SettingsNetworkFragment :
    BaseMvpFragment<SettingsNetworkContract.View, SettingsNetworkContract.Presenter>(
        R.layout.fragment_settings_network
    ),
    RadioGroup.OnCheckedChangeListener,
    SettingsNetworkContract.View {

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY_NETWORK_DIALOG"
        const val BUNDLE_KEY_IS_NETWORK_CHANGED = "BUNDLE_KEY_IS_NETWORK_CHANGED"

        fun create() = SettingsNetworkFragment()
    }

    override val presenter: SettingsNetworkContract.Presenter by inject()

    private val binding: FragmentSettingsNetworkBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attach(this)
        with(binding) {
            networksGroup.setOnCheckedChangeListener(this@SettingsNetworkFragment)
            primaryButton.setOnClickListener {
                setFragmentResult(REQUEST_KEY, bundleOf(Pair(BUNDLE_KEY_IS_NETWORK_CHANGED, true)))
                popBackStack()
            }
            secondaryButton.setOnClickListener {
                popBackStack()
            }
        }
        presenter.loadData()
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        val environment = when (checkedId) {
            R.id.mainnetButton -> Environment.MAINNET
            R.id.rpcpoolButton -> Environment.RPC_POOL
            R.id.solanaButton -> Environment.SOLANA
            R.id.devnetButton -> Environment.DEVNET
            else -> throw IllegalStateException("No environment found for this id: $checkedId")
        }
        presenter.setNewEnvironment(environment)
    }

    override fun showEnvironment(environment: Environment) {
        val checkedId = when (environment) {
            Environment.SOLANA -> R.id.solanaButton
            Environment.MAINNET -> R.id.mainnetButton
            Environment.RPC_POOL -> R.id.rpcpoolButton
            Environment.DEVNET -> R.id.devnetButton
        }

        binding.networksGroup.setOnCheckedChangeListener(null)
        binding.networksGroup.check(checkedId)
        binding.networksGroup.setOnCheckedChangeListener(this)
    }

    override fun showLoading(isLoading: Boolean) {
        // binding.progressView.isVisible = isLoading
    }
}