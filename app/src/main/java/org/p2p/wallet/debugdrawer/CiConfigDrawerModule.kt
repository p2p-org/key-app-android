package org.p2p.wallet.debugdrawer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.palaima.debugdrawer.base.DebugModuleAdapter
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.databinding.ViewDebugDrawerCiConfigBinding
import org.p2p.wallet.utils.appendBreakLine
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import java.lang.StringBuilder

class CiConfigDrawerModule : DebugModuleAdapter() {
    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup): View {
        return parent.inflateViewBinding<ViewDebugDrawerCiConfigBinding>(attachToRoot = false).apply {
            val ciValues = buildString {
                createApiKeyRecord("amplitudeKey", BuildConfig.amplitudeKey)
                createApiKeyRecord("comparePublicKey", BuildConfig.comparePublicKey)
                createApiKeyRecord("intercomApiKey", BuildConfig.intercomApiKey)
                createApiKeyRecord("intercomAppId", BuildConfig.intercomAppId)
                createApiKeyRecord("moonpayKey", BuildConfig.moonpayKey)
                createApiKeyRecord("rpcPoolApiKey", BuildConfig.rpcPoolApiKey)

                appendBreakLine()

                createFlagRecord("AMPLITUDE_ENABLED", BuildConfig.AMPLITUDE_ENABLED)
                createFlagRecord("CRASHLYTICS_ENABLED", BuildConfig.CRASHLYTICS_ENABLED)
                createFlagRecord("KEY_DEV_NET_ENABLED", BuildConfig.KEY_DEV_NET_ENABLED)
            }
            tvCiValues.text = ciValues
        }.root
    }

    private fun StringBuilder.createApiKeyRecord(apiKeyName: String, apiKey: String) {
        append("$apiKeyName = ")
        append("***")
        append(apiKey.removeRange(startIndex = 0, endIndex = apiKey.length - 3))
        appendBreakLine()
    }

    private fun StringBuilder.createFlagRecord(flagName: String, flagValue: Boolean) {
        append("$flagName = $flagValue")
        appendBreakLine()
    }
}
