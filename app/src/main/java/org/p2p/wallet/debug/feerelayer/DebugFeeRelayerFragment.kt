package org.p2p.wallet.debug.feerelayer

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentDebugFeeRelayerBinding
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class DebugFeeRelayerFragment : BaseFragment(R.layout.fragment_debug_fee_relayer) {

    companion object {
        fun create(): DebugFeeRelayerFragment = DebugFeeRelayerFragment()
    }

    private val binding: FragmentDebugFeeRelayerBinding by viewBinding()

    private val environmentManager: EnvironmentManager by inject()
    private val appRestarter: AppRestarter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            val url = environmentManager.loadFeeRelayerEnvironment().baseFeeRelayerUrl
            environmentTextView.text = url

            testUrlButton.setOnClickListener {
                val testUrl = getString(R.string.feeRelayerTestBaseUrl)
                updateEnvironmentAndRestart(testUrl)
            }

            releaseUrlButton.setOnClickListener {
                val releaseUrl = getString(R.string.feeRelayerBaseUrl)
                updateEnvironmentAndRestart(releaseUrl)
            }

            confirmButton.setOnClickListener {
                val newUrl = environmentEditText.text.toString()
                updateEnvironmentAndRestart(newUrl)
            }
        }
    }

    private fun updateEnvironmentAndRestart(newUrl: String) {
        environmentManager.saveFeeRelayerEnvironment(newUrl)
        appRestarter.restartApp()
    }
}
