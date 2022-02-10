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
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"
class SettingsNetworkFragment :
    BaseMvpFragment<SettingsNetworkContract.View, SettingsNetworkContract.Presenter>(
        R.layout.fragment_settings_network
    ),
    RadioGroup.OnCheckedChangeListener,
    SettingsNetworkContract.View {

    companion object {
        fun create(requestKey: String, resultKey: String) = SettingsNetworkFragment().withArgs(
            EXTRA_REQUEST_KEY to requestKey,
            EXTRA_RESULT_KEY to resultKey
        )
    }

    override val presenter: SettingsNetworkContract.Presenter by inject()

    private val binding: FragmentSettingsNetworkBinding by viewBinding()

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

    override fun onNetworkChanged(newName: String) {
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(Pair(SettingsFragment.BUNDLE_KEY_NETWORK_NAME, newName))
        )
        popBackStack()
    }
}