package org.p2p.wallet.debugdrawer

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import io.palaima.debugdrawer.base.DebugModuleAdapter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.databinding.ViewDebugDrawerConfigureEnvironmentBinding
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager

const val KEY_POLLING_ENABLED = "KEY_POLLING_ENABLED"
const val KEY_IS_PROD = "KEY_IS_PROD"

class ConfigurationModule : DebugModuleAdapter(), KoinComponent {

    private val environmentManager: EnvironmentManager by inject()
    private val sharedPreferences: SharedPreferences by inject()

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup): View {
        val binding = ViewDebugDrawerConfigureEnvironmentBinding.inflate(inflater, parent, false)

        val environment = environmentManager.loadEnvironment()
        with(binding) {
            urlTextView.text = environment.endpoint
            enablePollingSwitch.isChecked = sharedPreferences.getBoolean(KEY_POLLING_ENABLED, true)
            enablePollingSwitch.setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit { putBoolean(KEY_POLLING_ENABLED, isChecked) }
            }

            enableProdEnvSwitch.isChecked = sharedPreferences.getBoolean(KEY_IS_PROD, BuildConfig.IS_PROD)
            enableProdEnvSwitch.setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit { putBoolean(KEY_IS_PROD, isChecked) }
            }
        }

        return binding.root
    }
}
