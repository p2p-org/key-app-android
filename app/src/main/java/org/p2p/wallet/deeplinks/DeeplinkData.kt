package org.p2p.wallet.deeplinks

import android.content.Intent

/**
 * Container for deeplink information, such as target screen, extra paths and arguments
 * @param target @see deeplink target
 * @param pathSegments [List] of string (the same as in Uri)
 * @param args extra arguments
 * @param intent original intent, useful for getting extras from notification
 */
data class DeeplinkData(
    val target: DeeplinkTarget,
    val pathSegments: List<String> = emptyList(),
    val args: Map<String, String?> = emptyMap(),
    val intent: Intent? = null
) {
    fun hasArgNotNull(key: String): Boolean = args[key] != null
}
