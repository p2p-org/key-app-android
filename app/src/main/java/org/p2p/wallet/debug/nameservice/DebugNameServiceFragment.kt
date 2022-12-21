package org.p2p.wallet.debug.nameservice

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentDebugNameServiceBinding
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class DebugNameServiceFragment : BaseFragment(R.layout.fragment_debug_name_service) {

    companion object {
        fun create(): DebugNameServiceFragment = DebugNameServiceFragment()
    }

    private val binding: FragmentDebugNameServiceBinding by viewBinding()

    private val networkServicesUrlProvider: NetworkServicesUrlProvider by inject()
    private val appRestarter: AppRestarter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            val url = networkServicesUrlProvider.loadNameServiceEnvironment().baseUrl
            environmentTextView.text = url

            testUrlButton.setOnClickListener {
                val testUrl = getString(R.string.registerUsernameServiceTestUrl)
                updateEnvironmentAndRestart(testUrl)
            }

            releaseUrlButton.setOnClickListener {
                val releaseUrl = getString(R.string.registerUsernameServiceProductionUrl)
                updateEnvironmentAndRestart(releaseUrl)
            }
        }
    }

    private fun updateEnvironmentAndRestart(newUrl: String) {
        networkServicesUrlProvider.saveNameServiceEnvironment(newUrl)
        appRestarter.restartApp()
    }
}
