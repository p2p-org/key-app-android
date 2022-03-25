package org.p2p.wallet.debugdrawer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.palaima.debugdrawer.base.DebugModuleAdapter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.wallet.common.AppFeatureFlags
import org.p2p.wallet.databinding.ViewDebugDrawerConfigureEnvironmentBinding
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager

class ConfigurationModule : DebugModuleAdapter(), KoinComponent {

    private val environmentManager: EnvironmentManager by inject()
    private val appFeatureFlags: AppFeatureFlags by inject()

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup): View {
        val binding = ViewDebugDrawerConfigureEnvironmentBinding.inflate(inflater, parent, false)

        val environment = environmentManager.loadEnvironment()
        with(binding) {
            urlTextView.text = environment.endpoint
            enablePollingSwitch.isChecked = appFeatureFlags.isPollingEnabled
            enablePollingSwitch.setOnCheckedChangeListener { _, isChecked ->
                appFeatureFlags.setPollingEnabled(isChecked)
            }

            enableProdEnvSwitch.isChecked = appFeatureFlags.isDevnetEnabled
            enableProdEnvSwitch.setOnCheckedChangeListener { _, isChecked ->
                appFeatureFlags.setIsDevnetEnabled(isChecked)
            }
        }

        return binding.root
    }
}
