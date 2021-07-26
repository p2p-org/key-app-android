package com.p2p.wallet.settings.ui.network

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentNetworkBinding
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject
import org.p2p.solanaj.rpc.Environment

class NetworkFragment :
    BaseMvpFragment<NetworkContract.View, NetworkContract.Presenter>(R.layout.fragment_network),
    NetworkContract.View,
    RadioGroup.OnCheckedChangeListener {

    companion object {
        fun create() = NetworkFragment()
    }

    override val presenter: NetworkContract.Presenter by inject()

    private val binding: FragmentNetworkBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            networksGroup.setOnCheckedChangeListener(this@NetworkFragment)

            mainnetButton.text = getString(R.string.node_mainnet)
            solanaButton.text = getString(R.string.node_solana)
            devnetButton.text = getString(R.string.node_devnet)
            testnetButton.text = getString(R.string.node_testnet)
        }

        presenter.loadData()
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        val environment = when (checkedId) {
            R.id.mainnetButton -> Environment.MAINNET
            R.id.solanaButton -> Environment.SOLANA
            R.id.devnetButton -> Environment.DEVNET
            else -> Environment.TESTNET
        }
        presenter.setNewEnvironment(environment)
    }

    override fun showEnvironment(environment: Environment) {
        val checkedId = when (environment) {
            Environment.SOLANA -> R.id.solanaButton
            Environment.MAINNET -> R.id.mainnetButton
            Environment.DEVNET -> R.id.devnetButton
            Environment.TESTNET -> R.id.testnetButton
        }

        binding.networksGroup.setOnCheckedChangeListener(null)
        binding.networksGroup.check(checkedId)
        binding.networksGroup.setOnCheckedChangeListener(this)
    }
}