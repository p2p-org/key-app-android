package org.p2p.wallet.auth.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.utils.DateTimeUtils

private const val KEY_CHANGES_LOG = "KEY_CHANGES_LOG"

class MetadataChangesLogger(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {
    val logs: Set<String>
        get() = sharedPreferences.getStringSet(KEY_CHANGES_LOG, emptySet()).orEmpty()

    fun logChange(
        metadataOld: GatewayOnboardingMetadata?,
        metadataNew: GatewayOnboardingMetadata
    ) {
        if (BuildConfig.DEBUG) {
            val metadataLogs =
                sharedPreferences.getStringSet(KEY_CHANGES_LOG, emptySet()) ?: mutableSetOf()

            val logEntry = buildString {
                append(DateTimeUtils.getFormattedDateAndTime(System.currentTimeMillis()))
                append(":")
                appendLine()
                append("before=${gson.toJson(metadataOld)}")
                appendLine()
                append("after=${gson.toJson(metadataNew)}")
                appendLine()
            }
            val newMetadataLogs = HashSet(metadataLogs).plus(logEntry)
            sharedPreferences.edit { putStringSet(KEY_CHANGES_LOG, newMetadataLogs) }
        }
    }
}
