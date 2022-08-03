package org.p2p.wallet.debug.torus

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentDebugFeeRelayerBinding
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class DebugTorusFragment : BaseFragment(R.layout.fragment_debug_torus) {

    companion object {
        fun create(): DebugTorusFragment = DebugTorusFragment()
    }

    private val binding: FragmentDebugFeeRelayerBinding by viewBinding()

    private val networkServicesUrlProvider: NetworkServicesUrlProvider by inject()
    private val appRestarter: AppRestarter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            val url = networkServicesUrlProvider.loadTorusEnvironment().baseUrl
            environmentTextView.text = url

            testUrlButton.setOnClickListener {
                val testUrl = getString(R.string.torusBaseUrl)
                updateEnvironmentAndRestart(testUrl)
            }

            releaseUrlButton.setOnClickListener {
                val releaseUrl = getString(R.string.torusBaseUrl)
                // TODO add real torus release url when we will have it
                updateEnvironmentAndRestart(releaseUrl)
            }

            confirmButton.setOnClickListener {
                val newUrl = environmentEditText.text.toString()
                updateEnvironmentAndRestart(newUrl)
            }
        }
    }

    private fun updateEnvironmentAndRestart(newUrl: String) {
        networkServicesUrlProvider.saveTorusEnvironment(newUrl)
        appRestarter.restartApp()
    }
}
