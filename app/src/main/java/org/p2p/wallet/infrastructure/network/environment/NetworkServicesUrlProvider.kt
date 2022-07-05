package org.p2p.wallet.infrastructure.network.environment

import androidx.core.content.edit
import android.content.Context
import android.content.SharedPreferences
import org.p2p.wallet.R

private const val KEY_NOTIFICATION_SERVICE_BASE_URL = "KEY_NOTIFICATION_SERVICE_BASE_URL"
private const val KEY_FEE_RELAYER_BASE_URL = "KEY_FEE_RELAYER_BASE_URL"

class NetworkServicesUrlProvider(
    context: Context,
    private val sharedPreferences: SharedPreferences
) {

    private val defaultFeeRelayerUrl =
        FeeRelayerEnvironment(context.getString(R.string.feeRelayerBaseUrl))
    private val defaultNotificationServiceUrl =
        NotificationServiceEnvironment(context.getString(R.string.notificationServiceBaseUrl))

    fun loadFeeRelayerEnvironment(): FeeRelayerEnvironment {
        val url = sharedPreferences.getString(
            KEY_FEE_RELAYER_BASE_URL,
            defaultFeeRelayerUrl.baseUrl
        ).orEmpty()

        return FeeRelayerEnvironment(url)
    }

    fun saveFeeRelayerEnvironment(newUrl: String) {
        sharedPreferences.edit { putString(KEY_FEE_RELAYER_BASE_URL, newUrl) }
    }

    fun loadNotificationServiceEnvironment(): NotificationServiceEnvironment {
        val url = sharedPreferences.getString(
            KEY_NOTIFICATION_SERVICE_BASE_URL,
            defaultNotificationServiceUrl.baseUrl
        ).orEmpty()

        return NotificationServiceEnvironment(url)
    }

    fun saveNotificationServiceEnvironment(newUrl: String) {
        sharedPreferences.edit { putString(KEY_NOTIFICATION_SERVICE_BASE_URL, newUrl) }
    }
}
