package org.p2p.wallet.debug.pushservice

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentDebugPushServiceBinding
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class DebugPushServiceFragment : BaseFragment(R.layout.fragment_debug_push_service) {

    companion object {
        fun create(): DebugPushServiceFragment = DebugPushServiceFragment()
    }

    private val binding: FragmentDebugPushServiceBinding by viewBinding()

    private val environmentManager: EnvironmentManager by inject()
    private val appRestarter: AppRestarter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            val url = environmentManager.loadNotificationServiceEnvironment().baseUrl
            environmentTextView.text = url

            testUrlButton.setOnClickListener {
                val testUrl = getString(R.string.notificationServiceTestBaseUrl)
                updateEnvironmentAndRestart(testUrl)
            }

            releaseUrlButton.setOnClickListener {
                val releaseUrl = getString(R.string.notificationServiceBaseUrl)
                updateEnvironmentAndRestart(releaseUrl)
            }

            confirmButton.setOnClickListener {
                val newUrl = environmentEditText.text.toString()
                updateEnvironmentAndRestart(newUrl)
            }
        }
    }

    private fun updateEnvironmentAndRestart(newUrl: String) {
        environmentManager.saveNotificationServiceEnvironment(newUrl)
        appRestarter.restartApp()
    }
}
