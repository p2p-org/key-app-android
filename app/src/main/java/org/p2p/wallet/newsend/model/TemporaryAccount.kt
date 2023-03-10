package org.p2p.wallet.newsend.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.utils.toPublicKey

@Parcelize
data class TemporaryAccount(
    val symbols: String,
    val address: String,
    val keypair: String
) : Parcelable {

    companion object {
        private const val SEND_LINK_FORMAT = "key.app/transfer/"
    }

    @IgnoredOnParcel
    val publicKey: PublicKey
        get() = address.toPublicKey()

    fun generateFormattedLink(): String =
        "$SEND_LINK_FORMAT$symbols"
}
