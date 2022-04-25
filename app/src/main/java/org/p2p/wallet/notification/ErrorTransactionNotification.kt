package org.p2p.wallet.notification

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import org.p2p.wallet.R

class ErrorTransactionNotification(
    val message: String,
    val sourceSymbol: String,
    val destinationSymbol: String
) {

    val notificationId
        get() = message.hashCode()

    val sound: Uri
        get() = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    fun buildTitleText(context: Context): String =
        context.getString(R.string.swap_transaction_failed, sourceSymbol, destinationSymbol)

    @Suppress("MagicNumber")
    val vibration
        get() = longArrayOf(0, 500, 0, 0, 0)
}
