package org.p2p.wallet.notification

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import org.p2p.wallet.R

class SwapTransactionNotification(
    val signature: String,
    val sourceSymbol: String,
    val destinationSymbol: String
) {

    val notificationId
        get() = signature.hashCode()

    val sound: Uri
        get() = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    fun buildShortText(context: Context): String =
        context.getString(R.string.swap_transaction_completed, sourceSymbol, destinationSymbol)

    @Suppress("MagicNumber")
    val vibration
        get() = longArrayOf(0, 500, 0, 0, 0)
}
